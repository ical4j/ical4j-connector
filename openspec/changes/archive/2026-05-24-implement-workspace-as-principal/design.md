## Context

The `complete-dav-module-parity` change adopted a `DEFAULT_WORKSPACE` contract on the DAV stores: every `*(...,String workspace)` method rejects anything other than `null` or `ObjectStore.DEFAULT_WORKSPACE`. That matched the Google connector's design and brought consistency across providers, but it was explicitly a placeholder — DAV has a real concept that the `workspace` argument can express, namely the principal (user) under whose home-set a collection lives.

The existing `PathResolver` already takes a workspace argument and threads it into the format strings:
- `RADICALE` calendar path: `/%2$s/%1$s` — `%2$s` is the workspace/principal
- `BAIKAL` calendar path: `/calendars/%2$s/%1$s` — same
- `BAIKAL` card path: `/addressbooks/%2$s/%1$s` — same

And `DavSessionConfiguration.workspace` is set at connect time and used as the principal name for things like `pathResolver.getPrincipalPath(getSessionConfiguration().getUser())`. So path-level routing already supports per-principal addressing. What the stores haven't done is *route* through it on the workspace-parameterised methods — they reject instead. This change closes that gap.

`CalDavCalendarStore.getCollections()` (no-arg) does its work in two steps:
1. `findCalendarHomeSet()` — propfind the session-user principal path for the `calendar-home-set` href.
2. `getCollectionsForHomeSet(...)` — propfind that home-set path for child collections.

To support a workspace arg meaning "a different principal," we need a per-principal variant of step 1 (`findCalendarHomeSet(String principal)`) and the wiring to pass that principal through. The session-user variant becomes a delegating wrapper. Same shape on the CardDav side.

We also bundle a small same-family fix to `EventQuery`: it emits `<C:getetag/>` (CalDAV namespace) where RFC 4791 §9.5 requires `<D:getetag/>` (DAV namespace). The previous change fixed the identical bug in `CalendarMultiget` and noted EventQuery as a follow-up.

## Goals / Non-Goals

**Goals:**
- Workspace methods on both stores accept arbitrary principal names and route path resolution accordingly.
- `null` and `DEFAULT_WORKSPACE` continue to mean "the session user" (unchanged behaviour for cross-provider callers passing the sentinel).
- Server-side authorization errors (403 on a non-delegated principal) propagate as `ObjectStoreException` with the existing message shape.
- `EventQuery`'s `getetag` element is in the correct DAV namespace per RFC 4791 §9.5.
- Integration tests cover the positive (session-user equivalence) and negative (foreign-principal authorization error) paths on Baikal and Radicale.

**Non-Goals:**
- Provisioning delegation in Baikal/Radicale test fixtures (e.g. configuring user A's read/write rights on user B's calendar). That's real ops setup and out of scope; the negative path is the only cross-principal test surface we'll exercise.
- `listWorkspaceIds()` returning server-discovered principals. The current `[DEFAULT_WORKSPACE]` semantic is preserved; principal discovery is its own future change.
- `connect(workspace)` or per-call session re-auth — the session user / credentials remain set at connect time. A workspace value names which principal's home-set to address; authentication is unchanged.
- Sharing/unsharing primitives (e.g. POST `<C:share/>`); those are CalendarServer/iCloud-specific protocols and out of scope.

## Decisions

### Decision 1: Workspace value is a literal principal name

**Choice**: When a method receives a non-null, non-DEFAULT_WORKSPACE workspace value, treat it directly as the principal name passed to `pathResolver.getCalendarPath(id, workspace)` / `getCardPath(id, workspace)` / `getPrincipalPath(workspace)`. No translation, no lookup.

**Why**: The `PathResolver` already documents the workspace argument as participating in path formatting. The user-facing semantic is "I want to operate on `<workspace>`'s collections" — and the principal name is exactly what the user knows.

**Alternatives considered**:
- *Workspace == href*: pass a full DAV path through. More general, but mixes abstractions (the workspace becomes a wire-format identifier rather than a logical user). Rejected.
- *Workspace == display name + a lookup step*: friendly but requires a discovery propfind on every call. Rejected.

### Decision 2: New `findCalendarHomeSet(String principal)` overload; session-user version becomes a wrapper

**Choice**:
```java
private String findCalendarHomeSet() throws ... {
    return findCalendarHomeSet(getSessionConfiguration().getUser());
}

private String findCalendarHomeSet(String principal) throws IOException {
    var propfindPath = pathResolver.getPrincipalPath(principal);
    return getClient().propFind(propfindPath, PropertyNameSets.PROPFIND_CALENDAR_HOME,
            new GetPropertyValue<>(CalDavPropertyName.CALENDAR_HOME_SET));
}
```
Same shape for `CardDavStore.findAddressBookHomeSet(String principal)`.

**Why**: Minimal surface area, internal-only, mirrors the no-arg version's existing implementation. The principal-aware version is the one that actually does the work; the no-arg version is a convenience for the session-user case.

### Decision 3: Workspace-method routing replaces `assertDefaultWorkspace` with a two-branch dispatch

**Choice**: Each workspace-parameterised method becomes:
```java
public X method(..., String workspace) throws ObjectStoreException {
    if (workspace == null || ObjectStore.DEFAULT_WORKSPACE.equals(workspace)) {
        return method(...);  // delegate to session-user variant
    }
    return methodForPrincipal(..., workspace);  // new principal-targeted code path
}
```
Where `methodForPrincipal(...)` resolves the home-set / path using `workspace` as the principal.

The private `assertDefaultWorkspace` helper is deleted from both stores.

**Why**: Same delegation pattern as today for the null/DEFAULT_WORKSPACE case (no regression). The new branch is opt-in; passing a principal name is the explicit "I know what I'm doing" route.

### Decision 4: Path-resolver workspace argument == principal name

**Choice**: For the new code paths, pass `workspace` as the second argument to `pathResolver.getCalendarPath(id, workspace)` / `getCardPath(id, workspace)`. This is the same string the existing session-user code path passes via `getSessionConfiguration().getWorkspace()` (which today is conventionally set to the session user name at connect time).

**Why**: The PathResolver contract already documents the second arg as the workspace (effectively, the principal in DAV terms). No new abstraction needed.

**Caveat**: Some PathResolver enum values format-strings don't use `%2$s` (`CHANDLER`, `CGP`, `KMS`, `ZIMBRA`, `ICAL_SERVER`, `CALENDAR_SERVER`, `GCAL`, `SOGO`, `DAVICAL`, `BEDEWORK`, `ORACLE_CS`, `GENERIC`). For those servers, the workspace argument has no effect on path construction — the change is effectively a no-op there. That's acceptable for V1: those servers either don't expose per-principal pathing or use a different convention (e.g. `BEDEWORK` uses `%s` once, meaning the workspace is silently dropped). A future change can introduce principal-aware path-format strings for those resolvers if needed.

### Decision 5: `listWorkspaceIds()` stays `[DEFAULT_WORKSPACE]`

**Choice**: Unchanged. The sentinel value continues to mean "the session user's workspace." Server-side principal discovery (PROPFIND on `principal-collection-set` etc.) is deferred to a future change.

**Why**: Discovery is a separable concern — it would expand scope into auth-context handling, optional caching, and per-server quirks in how principals are surfaced. Callers who already know the principal names they want can use them today without needing discovery.

### Decision 6: Negative test only for cross-principal access; no delegation fixture setup

**Choice**: Tests verify:
- (positive) `getCollections(getSessionUser())` is equivalent to `getCollections(DEFAULT_WORKSPACE)` (same content, no exception).
- (negative) `getCollections("non-existent-other-principal")` raises `ObjectStoreException` (server returns 403/404 because no such principal or no delegation).

Configuring Baikal or Radicale to create a second user and grant the first user delegation rights on the second user's home-set is real ops work — sqlite fixture changes for Baikal, permissions-file changes for Radicale, and verifying the rights propagate end-to-end. Deferred to a future "delegation-aware test fixtures" change.

**Why**: The negative path is the only cross-principal behaviour we can reliably test without fixture setup. The positive (session-user equivalence) test confirms the plumbing works for the common case. Skipping fixture setup keeps this change small and the test surface stable.

### Decision 7: Bundle the EventQuery getetag namespace fix

**Choice**: In `EventQuery.toXml`, replace:
```java
newCalDavElement(document, DavConstants.PROPERTY_GETETAG)
```
with:
```java
newElement(document, DavConstants.PROPERTY_GETETAG, DavConstants.NAMESPACE)
```

Add an integration test that exercises `EventQuery` end-to-end via `getEventsForTimePeriod` against Baikal + Radicale.

**Why**: Same fix shape as the multiget change. Flagged as latent there. Tiny diff, same touch surface as the workspace work (both edit DAV request classes). Bundling avoids a one-line follow-up change.

**Risk**: The previous change documented this bug but did not exercise EventQuery in integration tests. If `getEventsForTimePeriod` had silently been broken for one or both servers, this fix unblocks it (good) or surfaces a different bug downstream (in which case we deal with it within this change or defer surgically).

## Risks / Trade-offs

- **[PathResolver formats vary: some ignore the workspace arg]** → For non-Baikal/non-Radicale resolvers like CHANDLER, CGP, KMS, the workspace value silently has no effect. Callers targeting those servers with a non-default workspace get a request to the session user's path, which may succeed or fail unpredictably. Mitigation: documented in design; future work to introduce principal-aware formatting for those resolvers.

- **[Negative test depends on the server actually 403'ing rather than 404'ing or hanging]** → Both Baikal and Radicale return 4xx for unknown/non-delegated principals in our experience. If the underlying behaviour varies, the test can assert the broader contract: "some `ObjectStoreException` was raised" without coupling to the status code.

- **[Bundling EventQuery fix slightly expands scope]** → tiny one-line change with an existing latent-bug callout. Net positive: keeps the protocol-compliance fixes coherent and avoids a one-line follow-up commit.

- **[Cross-provider callers who pass a workspace value expecting rejection]** → none exist in this codebase. The contract change is documented as **BREAKING** but the prior contract was a placeholder. Risk-tolerable.

## Open Questions

1. Should the negative test pin on exception message text (e.g. contains "403") or accept any `ObjectStoreException`? Leaning *any `ObjectStoreException`* for portability.
2. Should `addCollection(name, workspace)` against a workspace where the session user has create rights actually succeed and persist? Per RFC, yes — but verifying this requires delegation fixture setup. For this change, the negative test (server 403) is enough; the positive cross-principal create is testable when the fixtures land.

## Implementation Notes (added during apply)

### Helper-method naming

The proposal called for an overload `findCalendarHomeSet(String principal)`. In the existing code, the single-arg version took a *propfind URI* (already-resolved), not a principal name. To avoid silently changing the meaning of the existing private signature and to make intent loud, used a new name: `findCalendarHomeSetForPrincipal(String principal)`. The no-arg `findCalendarHomeSet()` delegates to it with the session user. Same shape on `CardDavStore` (`findAddressBookHomeSetForPrincipal`).

### Pre-existing bug uncovered in `getCollection(String id)`

`CalDavCalendarStore.getCollection(String id)` previously delegated to `getCollection(id, DEFAULT_WORKSPACE)`, which passed the literal string `"default"` to `pathResolver.getCalendarPath(id, "default")`. This was broken for any session user not named `"default"` — it would resolve a path under `/calendars/default/<id>` regardless of who connected. The bug was latent because the existing tests use `addCollection` (which returns a reference the test holds directly) rather than `getCollection(id)`. Fixed in this change by switching the delegation to `getCollection(id, null)` so the path is resolved via the session-user workspace.

### Pre-existing bug uncovered in `EventQuery`

While bundling the getetag-namespace fix to `EventQuery`, the new `getEventsForTimePeriod` integration tests exposed a DOM bug in `EventQuery.toXml`: it called `document.appendChild(property)` (attaching `<D:prop>` as the document element) before calling `super.toXml(document)`, which then tried to attach the `<calendar-query>` report root and threw `HIERARCHY_REQUEST_ERR` (DOM Documents can only have one root element). Same bug shape we fixed in `CalendarMultiget` two changes ago. Resolved by removing the spurious `appendChild` calls and using only `setContentElement` — Jackrabbit's `ReportInfo.toXml` handles the wrapper element insertion.

This means `EventQuery` was previously non-functional end-to-end. The class is now actually exercised by integration tests for the first time.

### Server-side authorization errors propagate via `RuntimeException`, need catch+wrap

The `AbstractResponseHandler.getMultiStatus` helper throws `RuntimeException` on a non-2xx response (it's the standard way the existing handlers signal "unexpected status"). The store methods previously caught `DavException | IOException` only. For the cross-principal negative test ("server 403s our propfind for an unauthorized principal"), the `RuntimeException` escaped unwrapped. Added `RuntimeException` to the catch block in `getCollections(workspace)` on both stores so the spec-required `ObjectStoreException` wrapper is produced.

This is a localised fix scoped to the new principal-routing code paths only — not a broader change to how response-handler errors are propagated elsewhere in the codebase. The wider question ("should all `RuntimeException`s from response handlers be auto-wrapped?") is left for a future hygiene change.

### Test infra flake — Radicale containers

The full DAV module test suite hits intermittent Radicale container startup flakes (`ContainerLaunchException: RetryCountExceededException`) — Testcontainers spins up a fresh Radicale container per integration test class, and one of them randomly fails to come up healthy. Hits roughly 1 in 4 runs across `RadicaleDavClientIntegrationTest` / `RadicaleCardStoreIntegrationTest`. Not caused by this change; the 4 store integration test classes (which are what this change actually exercises) run reliably across all observed runs. Documented here so future apply sessions don't get confused.

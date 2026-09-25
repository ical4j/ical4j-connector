## Context

The `complete-dav-module-parity` change (archived 2026-05-24) deliberately scoped itself to the high-level CalDav/CardDav store and collection surface. Implementing those scenarios uncovered three bugs in adjacent low-level code that, while real, sit outside that change's scope:

- `request/MkColEntity.java` — request body XML for MKCOL.
- `DefaultDavClient.java#resolvePath` — repository-relative vs server-absolute path handling.
- `response/GetPropertyValue.java` — property-value type assumption in PROPFIND response parsing.

All three are tested against Baikal but masked by `@IgnoreIf("Cut B")` annotations on 7 Baikal-specific integration test methods (1 in `AbstractCalendarStoreIntegrationTest`, 6 in `AbstractCardStoreIntegrationTest`). Radicale's lenience hides these bugs end-to-end.

This change fixes the three bugs in isolation and removes the ignore guards. It is a prerequisite for `complete-dav-module-cut-b`, which will add multiget / freebusy / merge / export / workspace-principal features on top.

## Goals / Non-Goals

**Goals:**
- Bring `MkColEntity` into compliance with RFC 5689 (Extended MKCOL).
- Make `resolvePath` idempotent when given a path that already contains the repository prefix.
- Make `GetPropertyValue` tolerate both the `Element`-wrapped and plain-`String` shapes of property values.
- Have all 7 previously-`@IgnoreIf`d Baikal tests pass with no other code changes.
- Maintain green tests on Radicale (no regression).

**Non-Goals:**
- Refactoring the low-level DAV client beyond these three localized fixes.
- Adding new request/response handlers or DAV methods.
- Any of the originally-deferred Cut B feature work (multiget, freebusy, merge, export, workspace-as-principal). Those belong in `complete-dav-module-cut-b`.
- Bedework integration test re-enablement. Bedework has separate issues already `@Ignore`d at a different layer; out of scope here.
- Unit tests for `resolvePath` / `MkColEntity` / `GetPropertyValue` in isolation. The integration tests against Baikal + Radicale already exercise both the bug-hit and bug-miss paths; adding unit tests doubles maintenance for marginal gain (same call as in the parity change).

## Decisions

### Decision 1: `MkColEntity` emits `<D:mkcol>` per RFC 5689

**Choice**: Change `MkColEntity.toXml()` to produce a root element named `mkcol` in the `DAV:` namespace instead of `create`. The inner structure (`<D:set><D:prop>...</D:prop></D:set>`) is already RFC 5689-correct and stays unchanged.

**Why**: RFC 5689 §3 defines the Extended MKCOL request body as:
```
<D:mkcol xmlns:D="DAV:">
  <D:set>
    <D:prop>...</D:prop>
  </D:set>
</D:mkcol>
```
The current `<D:create>` element does not exist in any DAV RFC. It happens to work against Radicale because Radicale is lenient about the request body for plain `MKCOL`; Baikal validates strictly and rejects with `400 Bad Request`.

**Alternatives considered**:
- *Add a feature flag*: keep `<D:create>` as default, opt into `<D:mkcol>`. Rejected — there is no legitimate reason to emit `<D:create>`; it's a bug, not a configuration choice.
- *Only change for CardDav MKCOL*: rejected — RFC 5689 is generic, the bug is in the generic entity, fix once.

### Decision 2: `resolvePath` short-circuits when path already starts with repositoryPath

**Choice**: In `DefaultDavClient.resolvePath(String path)`, when `path` is non-null and starts with the `repositoryPath` prefix, return the path unchanged (after the `/+` → `/` collapse). Otherwise behave exactly as today.

```java
private String resolvePath(String path) {
    if (path == null) {
        return repositoryPath;
    }
    if (path.startsWith(repositoryPath + "/") || path.equals(repositoryPath)) {
        return path.replaceAll("/+", "/");
    }
    if (path.startsWith("/")) {
        return (repositoryPath + path).replaceAll("/+", "/");
    }
    return (repositoryPath + "/" + path).replaceAll("/+", "/");
}
```

**Why**: The bug is structural: `findCalendarHomeSet` (and the addressbook equivalent) propfinds the principal path, reads back a property value, and returns it. Some servers return that value as a *server-absolute* path (Baikal: `/dav.php/calendars/test/`); others return it server-root-relative (no repository prefix). The current code unconditionally prepends `repositoryPath`, producing `/dav.php/dav.php/...` for the first case.

Checking for the repository prefix and skipping re-prepend is the minimal correct fix. Edge case: a legitimate path that happens to start with the same characters as the repository prefix but is in fact a sibling (e.g. `repositoryPath = /dav` and target `/dav-other/x`) would be misclassified by a pure `startsWith` check; requiring either equality or a following `/` boundary avoids that.

**Alternatives considered**:
- *Always treat home-set responses as absolute*: would require fixing in the caller (`findCalendarHomeSet` strips the prefix before returning). Rejected — the bug surfaces wherever any code path receives a server-returned path, not just home-set. A single fix in `resolvePath` is centralized.
- *Use URI parsing*: heavier; doesn't add correctness for this case.

### Decision 3: `GetPropertyValue` handles both `Element` and `String` property values

**Choice**: In `GetPropertyValue.handleResponse`, branch on the runtime type of the property value:
- If the value is an `Element`, retain the current `propElement.getFirstChild().getNodeValue()` extraction.
- If the value is a `String`, return it as-is (cast to `T`).
- Otherwise return `value.toString()` cast to `T` as a defensive fallback (matches existing `null` return semantics for unrecognised shapes).

**Why**: Jackrabbit's `DefaultDavProperty.getValue()` returns the parsed property value as an `Object`. For properties whose XML body is a simple text node, Jackrabbit's parser may return either:
- the wrapping `Element` (the parser preserves the XML node), or
- the unwrapped `String` text (the parser extracts the text content).

Which path runs depends on the property's registration and the namespace declaration in the response — both server-controlled. Radicale's `calendar-home-set` response shape exercises the `Element` path; Baikal's `addressbook-home-set` response shape exercises the `String` path. The handler must tolerate both.

**Alternatives considered**:
- *Register all relevant property names as href-typed in Jackrabbit*: would force `HrefProperty` parsing and a uniform return shape, but requires changes scattered across `PropertyNameSets` and is fragile. Centralized handler-side branching is simpler.
- *Always call `toString()`*: works for `String` and `Element` (Element.toString gives debug XML, not the value) — would break the working Radicale case. Rejected.

### Decision 4: Remove `@IgnoreIf` annotations, don't replace them with anything

**Choice**: Delete the `@IgnoreIf({ instance.getClass().simpleName.contains('Baikal') })` annotations (and adjacent explanatory comments) from the 7 feature methods, leaving the methods plain. Both Baikal and Radicale subclasses then run them.

**Why**: The annotations were a known-failure marker, not a fixture-specific skip. Once the bugs are fixed there's nothing per-server to gate on. If a regression occurs later, a failing test is the right signal — not a re-added ignore.

## Risks / Trade-offs

- **[`MkColEntity` change could break a non-tested server]** → No DAV server we test or know about depends on `<D:create>`. RFC 5689 is the standard. Mitigation: integration tests cover Radicale + Baikal post-fix; both will exercise the new XML. If a downstream user reports a regression on an exotic server, address as a follow-up.

- **[`resolvePath` change could regress a path that legitimately starts with repository prefix as substring]** → Mitigated by requiring either exact equality or a trailing `/` after the prefix. The remaining theoretical case (e.g. a calendar literally named `dav.phpfoo` under root) is implausible and would be a server-side anti-pattern.

- **[`GetPropertyValue` defensive `toString()` fallback could hide future bugs]** → Without it, a third unexpected type would `ClassCastException` (current behaviour for `String`). With it, it returns a possibly-meaningless string. Mitigation: log at WARN when the fallback fires. Considered but rejected as out-of-scope noise — simply return `null` for the third case rather than fall back to `toString()`. This means unknown shapes propagate as null, same as the existing empty-response code path.

- **[Removing all 7 `@IgnoreIf` at once means a single failed fix produces multiple test failures]** → Acceptable; the failures are diagnostic. Each test points at a single bug or its cascade.

## Open Questions

1. Should the `resolvePath` fix also handle the case where `path` starts with a full scheme (`http://...`)? Current code doesn't, and no caller passes such a path today. Leaning *no — out of scope*; if it becomes needed it's a separate concern.
2. Should we log at DEBUG when `resolvePath` takes the new short-circuit branch, to aid future diagnosis if a server returns inconsistent path shapes? Leaning *no* — log noise. Add if it becomes useful.
3. For Decision 3, return `null` for unknown property value types, or throw a more descriptive `IOException`? Current behaviour throws `ClassCastException` (unhelpful). `null` matches the existing "no responses" return; `IOException` would be louder. Leaning *return null* for consistency, accept the trade-off that an unknown shape silently disappears.

## Implementation Notes (added during apply)

Two additional fixes were needed for the spec scenarios to pass against Baikal. Both are the same family as Decision 3 (response parsing tolerance) and were included in scope:

### Additional fix A: `GetCollections.handleResponse` casts `resourcetype.getValue()` to `List<Element>`

Jackrabbit's response parser returns the `resourcetype` property's value as either a `List<Element>` (the typical multi-value case: `<resourcetype><collection/><calendar/></resourcetype>`) or a single `Element` (single-value case). The current code only handles the `List` shape, throwing `ClassCastException` on the `Element` shape — which is what Baikal returns for some collections.

Fixed by introducing a `resourceTypeNames(Object)` helper that branches on the runtime type and returns a `Stream<String>` of local element names, then matching that stream against the requested resource type set. Same defensive shape as the `GetPropertyValue` Element-or-String tolerance.

### Additional fix B: `GetPropertyValue` was non-deterministic when the SC_OK property set contained multiple properties

The original handler called `getProperties(SC_OK).iterator().nextProperty()` to grab the first property in the response, regardless of which property the caller actually wanted. For propfinds that requested multiple property names (e.g. `PROPFIND_CARD_HOME` includes both `addressbook-home-set` and `displayname`), this returned whichever the parser iterated first. When `addressbook-home-set` was not in the SC_OK set (e.g. Baikal returned it under a different per-property status), the handler silently returned the `displayname` value instead — masking the real issue and making `findAddressBookHomeSet()` return the user's display name as if it were a URL.

Refactored to make the target property name explicit:
- `GetPropertyValue(DavPropertyName propertyName)` constructor stores the target.
- `handleResponse` does `msr.getProperties(SC_OK).get(propertyName)` to look up the specific property; returns `null` if missing.
- Value extraction switched from `firstChild.getNodeValue()` to `getTextContent()` on Element shapes — this transparently handles both bare-text shapes (`<prop>text</prop>`) and href-wrapped shapes (`<prop><href>text</href></prop>`), the latter being the RFC representation for href-typed properties.
- Updated all 6 call sites to pass the relevant `DavPropertyName`: `DISPLAYNAME` (owner-name lookup), `ADDRESSBOOK_HOME_SET`, `CALENDAR_HOME_SET`, `SCHEDULE_OUTBOX_URL`, `SCHEDULE_INBOX_URL`, and an existing per-call name in `AbstractDavResource.getPropertyValue`.

The no-arg `GetPropertyValue()` constructor was removed — every existing caller passed a single-property nameSet, so requiring the name explicitly is strictly clearer.

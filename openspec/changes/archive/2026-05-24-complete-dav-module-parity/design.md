## Context

The `ical4j-connector-dav` module is the oldest connector in this codebase. Its low-level layers (`DefaultDavClient`, `request/`, `response/`, `method/`, `AbstractDavResource`) are substantially complete and exercised by an existing Testcontainers-based integration test suite against Baikal, Radicale, and Bedework. The high-level `ObjectStore`/`CalendarCollection`/`CardCollection` surface, however, has a handful of stubs that throw `UnsupportedOperationException` or return `null`. The sibling MSGraph connector (and the in-flight Google connector) have established a "happy-path parity" bar covering collection CRUD, object CRUD, UID listing, and metadata accessors; the DAV module should match that bar.

The Google connector's design (see `openspec/changes/develop-google-connector/design.md`) made one deliberate choice we will mirror: it does not expose workspace semantics. Google's API has no equivalent of MS Graph's `calendarGroups`, so it collapses workspaces to a single `DEFAULT_WORKSPACE` and rejects anything else. DAV is different — it does have a workspace-shaped concept (per-principal home-sets via `DavSessionConfiguration.workspace`), but that workspace is set once at connection time, not per call. Implementing per-call workspace switching as DAV-principal re-targeting is real protocol work (delegation, ACLs, path-resolver re-computation) and is explicitly deferred to a follow-up "Cut B" change.

## Goals / Non-Goals

**Goals:**
- Match the happy-path API surface that MSGraph and Google connectors provide: collection CRUD, object add/remove, UID listing, and core metadata accessors all return real data (no `UnsupportedOperationException`, no `return null`) for both CalDAV and CardDAV stores.
- Honour the `ObjectStore.DEFAULT_WORKSPACE` contract on every workspace-parameterised method, so cross-provider code that passes `DEFAULT_WORKSPACE` works uniformly across DAV, MSGraph, and Google.
- Exercise the new code paths with Testcontainers-based integration tests against Baikal and Radicale — no live-server credentials required, no new mocking infrastructure.

**Non-Goals:**
- `calendar-multiget`, `free-busy-query`, `merge(String, CalendarCollection)`, `export()` — these remain stubbed and are scoped to a follow-up Cut B change.
- Implementing the workspace argument as DAV-principal re-targeting (delegation, alternate home-sets) — also Cut B.
- Refactoring `DefaultDavClient` into a `DavClient` interface for unit-test mockability — not needed if we lean on the existing integration test infrastructure.
- Refactoring or rewriting the low-level DAV client layers; this change touches only the high-level store/collection classes and the integration test suite.
- Bedework integration tests beyond what already exists — Bedework's container behaviour has known quirks; new assertions go into Baikal + Radicale only.

## Decisions

### Decision 1: Adopt `DEFAULT_WORKSPACE` pattern, reject non-default

**Choice**: For every `*(..., String workspace)` method on `CalDavCalendarStore` and `CardDavStore`:
- if `workspace == null` or `workspace.equals(ObjectStore.DEFAULT_WORKSPACE)`, delegate to the non-workspace variant;
- otherwise throw `ObjectStoreException("Workspace '<value>' not supported; only DEFAULT_WORKSPACE is recognised")`.

`listWorkspaceIds()` returns `List.of(ObjectStore.DEFAULT_WORKSPACE)` on both stores.

**Why**: Same call as the Google connector design (see `develop-google-connector/design.md` Decision 1). DAV does have a principal-switching concept, but it's set at connection time via `DavSessionConfiguration.workspace`, not per call. Pretending the per-call workspace argument means something it doesn't would silently break cross-provider code. Rejecting non-default surfaces the impedance mismatch loudly; accepting `null` and `DEFAULT_WORKSPACE` lets cross-provider callers pass the canonical value.

**Alternatives considered**:
- *Implement workspace as DAV principal switch*: real feature, real work — `pathResolver.getCalendarPath(id, workspace)` would need to re-target the home-set for an arbitrary principal, and that requires resolving delegation/ACL rules. Deferred to Cut B.
- *Silently ignore non-default workspace*: more forgiving but obscures the semantic gap. The Google design rejected this for the same reason.
- *Throw `UnsupportedOperationException` (status quo)*: punishes even the canonical `DEFAULT_WORKSPACE` caller.

### Decision 2: `listObjectUIDs` uses existing report machinery

**Choice**:
- `CalDavCalendarCollection.listObjectUIDs()` uses the same `calendar-query` `ReportInfo` that `getComponentsByType` already uses, iterates the returned `Calendar` objects, and extracts `Property.UID` from each.
- `CardDavCollection.listObjectUIDs()` uses the existing `addressbook-query` `ReportInfo` that `getAll()` already uses, iterates the returned `VCard` objects, and extracts each card's UID.

**Why**: Both collections already issue these reports via `getComponentsByType` / `getAll` and parse the responses into in-memory iCal/VCard objects. Reusing those code paths means we don't add a new XML request shape or a new response handler. The performance cost (full object bodies returned just to extract UIDs) is acceptable for V1 — the existing `getAll()` already pays it, and large-collection performance is a separable concern.

**Alternatives considered**:
- *PROPFIND with depth 1 for child URIs only*: lighter wire traffic (URIs only, no calendar bodies), but URIs ≠ UIDs — the URI convention is `${UID}.ics` per `defaultUriFromUid`, but servers may or may not honour that. Going via parsed objects is correct by construction.
- *New `calendar-query` variant requesting only UID*: technically smaller payload, but requires a new response handler. Not worth the surface area for V1.

### Decision 3: `CardDavCollection.removeAll` mirrors `CalDavCalendarCollection.removeAll`

**Choice**: Resolve each UID to a default URI (`${UID}.vcf` for CardDAV, mirroring `${UID}.ics` for CalDAV), DELETE each, return the list of removed `VCard` objects.

**Why**: Symmetry with the working calendar implementation. The URI convention is what this collection uses on add (`save(VCard)` writes to `${UID}.vcf`), so it's internally consistent. Servers that store cards under different URIs will return 404 on delete and the operation will fail loudly — that's the right behaviour for V1.

### Decision 4: `CardDavCollection.getDescription` reads `ADDRESSBOOK_DESCRIPTION`

**Choice**: Read `CardDavPropertyName.ADDRESSBOOK_DESCRIPTION` via the same `getProperty` helper that `getDisplayName` uses.

**Why**: Same shape as `CalDavCalendarCollection.getDescription` which reads `CalDavPropertyName.CALENDAR_DESCRIPTION`. The constructor already writes this property on creation (line 93 of `CardDavCollection`); the read accessor was just never wired up.

### Decision 5: `CardDavStore.addCollection(5-arg)` drops `supportedComponents` and `timezone`

**Choice**: `CardDavStore.addCollection(id, name, description, supportedComponents, timezone)` constructs a `CardDavCollection` with the given `id`, `displayName=name`, `description=description`, then calls `create()`. Silently drops `supportedComponents` (CalDAV-only concept) and `timezone` (CalDAV-only concept).

**Why**: The `ObjectStore` interface is generic across calendar and card stores, so its `addCollection` signature carries calendar-specific parameters. For a card store, those parameters have no meaning. The MSGraph connector follows the same "silently drop irrelevant args" pattern. Throwing would punish cross-provider callers passing `null` for those args.

### Decision 6: Tests integration-only, no `DavClient` interface extraction

**Choice**: Add new test methods to `AbstractCalendarStoreIntegrationTest` for the new CalDAV behaviour. Create a new `AbstractCardStoreIntegrationTest` for CardDAV behaviour, with `BaikalCardStoreIntegrationTest` and `RadicaleCardStoreIntegrationTest` concrete subclasses. Run against the existing Baikal + Radicale Testcontainers fixtures.

**Why**: The integration test infrastructure already exists and works. Adding unit tests would require extracting a `DavClient` interface from `DefaultDavClient` and threading it through `AbstractDavObjectStore` — a non-trivial refactor that isn't needed for this change's scope. Integration tests catch real protocol behaviour (which Baikal and Radicale interpret slightly differently — exactly what we want to surface).

**Alternatives considered**:
- *Unit tests with mocked `DefaultDavClient`*: would require the interface extraction. Saved for a future change if it becomes valuable.
- *Both*: belt-and-braces but doubles test maintenance for marginal extra coverage.

### Decision 7: Workspace methods on collections (not just stores) — out of scope

**Choice**: Workspace semantics live only on the stores. Collections do not gain workspace-parameterised method variants.

**Why**: The `CalendarCollection` and `CardCollection` interfaces don't declare workspace-parameterised methods, so there's nothing to implement at the collection level. This is purely a store-level concern.

## Risks / Trade-offs

- **[`listObjectUIDs` fetches full object bodies just to extract UIDs]** → wire-cost and parse-cost are O(n) on collection size. Mitigation: documented as acceptable for V1; large-collection optimisation is a separable concern. If users hit it, a follow-up can add a lightweight UID-only response handler.
- **[`removeAll` assumes URI is `${UID}.vcf`]** → if a server stored the card under a different URI (e.g. someone wrote it directly via raw DAV with a custom URI), the DELETE will 404. Mitigation: this collection writes cards using exactly this convention, so internal consistency is guaranteed. External writers are out of scope.
- **[Workspace rejection is a behaviour change for current callers]** → callers passing `DEFAULT_WORKSPACE` previously got `UnsupportedOperationException`; now they get a real result. Callers passing arbitrary other strings still get an exception, just a different one (`ObjectStoreException` instead of `UnsupportedOperationException`). Mitigation: documented as **BREAKING** in the proposal. No internal callers in the codebase currently pass a non-null workspace.
- **[Baikal and Radicale interpret addressbook properties differently]** → `ADDRESSBOOK_DESCRIPTION` may not be returned by all servers on PROPFIND. Mitigation: tests assert the value we set on creation comes back; if a server doesn't support it, the test will fail loudly and we'll adjust expectations or skip per-server.
- **[No unit tests means slower test feedback loop]** → integration tests are seconds-to-minutes per run vs. milliseconds for units. Mitigation: accepted trade-off for not building mock infrastructure; the existing CI run cost already includes these containers.

## Open Questions

1. Should `addCollection(null-or-default workspace)` succeed silently, or should we log a debug message when callers pass `DEFAULT_WORKSPACE` explicitly (vs. `null`)? Leaning *silent succeed* — the explicit `DEFAULT_WORKSPACE` is the canonical cross-provider value, not a smell.
2. For `CardDavCollection.listObjectUIDs`, should an empty addressbook return `List.of()` or `null`? The current `return null` stub is the bug; we'll return `List.of()` to match `Collections.emptyList()` semantics. Confirming this aligns with what the `ObjectCollection` interface expects — checking against MSGraph's implementation as the precedent.
3. Should `removeAll` short-circuit if any UID delete fails, or attempt all and report which succeeded? The current `CalDavCalendarCollection.removeAll` short-circuits via `throw new RuntimeException(e)`. Mirroring that for consistency in this change; a "best-effort" mode could be a future enhancement.

## Implementation Notes (added during apply)

### Pre-existing bugs uncovered and fixed within this change

Implementing the spec scenarios surfaced four bugs in adjacent code that were in scope to fix (otherwise the spec scenarios could not pass):

- `CalDavCalendarCollection.add(Calendar)` was returning `calendar.getRequiredProperty(Property.UID).getValue()`. RFC-compliant calendars carry UID on the VEVENT, not at calendar level, so this always threw `ConstraintViolationException`. Changed to `Calendars.getUid(calendar).getValue()`, which `writeCalendarOnServer` already uses.
- `CardDavCollection.create()` invoked plain `mkCol` with display name + description properties only. RFC-strict CardDAV servers (Radicale) reject this with 403 because the request lacks the addressbook resourcetype. Added `new ResourceType(new int[]{COLLECTION, addressbookType})` to the collection's properties at construction; `addressbookType` is registered once via `ResourceType.registerResourceType("addressbook", CardDavPropertyName.NAMESPACE)`.
- `CardDavStore.getCollection(String id)` passed the raw `id` to `propFind` instead of resolving via `pathResolver.getCardPath(id, workspace)`. The CalDav equivalent already did this correctly; brought CardDav in line and adopted the same null-tolerant pattern (return `null` instead of `IndexOutOfBoundsException` on empty result).
- `PropertyNameSets.PROPFIND_CARD` did not include `ADDRESSBOOK_DESCRIPTION` (parallel omission to `PROPFIND_CALENDAR.add(CALENDAR_DESCRIPTION)`). Added so `getDescription()` works after fetching a collection via `getCollection(id)`.

### Pre-existing bugs deferred to Cut B (Baikal-only test ignores)

Two further bugs in the low-level DAV client surfaced via Baikal integration tests; they affect protocol-correctness behaviour that is outside this change's scope (high-level store/collection completion). Affected new tests are `@Ignore`d in `BaikalCalendarStoreIntegrationTest` and `BaikalCardStoreIntegrationTest` with `Ignore` messages pointing to Cut B:

- **`MkColEntity` uses `<D:create>` as root element.** RFC 5689 (Extended MKCOL) specifies `<D:mkcol>`. Baikal enforces this and returns `400 Bad Request`; Radicale accepts either. Fix is a one-line change to `MkColEntity.toXml()`, but carries risk of breaking other non-strict servers we rely on. Deferred to Cut B for proper cross-server validation.
- **`DefaultDavClient.resolvePath` double-prefixes absolute paths containing the repository root.** Baikal's `findCalendarHomeSet`/`findAddressBookHomeSet` returns server-absolute paths like `/dav.php/calendars/test/`; `resolvePath` prepends the repository root `/dav.php` again, yielding `/dav.php/dav.php/...` → 404. Fix requires detecting whether `path` already begins with the repository prefix and skipping the re-prepend; this needs cross-server validation since other servers may rely on the current behaviour. Deferred to Cut B.
- (Adjacent issue, same bucket: Baikal's `addressbook-home-set` PROPFIND response shape causes a `ClassCastException` in `GetPropertyValue` — it returns a String body rather than an Element. Same Cut B trip.)

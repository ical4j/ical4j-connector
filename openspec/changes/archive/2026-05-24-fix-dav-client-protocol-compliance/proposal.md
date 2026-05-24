## Why

The `complete-dav-module-parity` change (archived 2026-05-24) brought the high-level CalDav/CardDav store and collection surface to MSGraph/Google parity, but in doing so it surfaced three pre-existing bugs in the low-level DAV client and response-handling code. Those bugs were out of scope for that change (it touched only the high-level layer) and were deferred via `@IgnoreIf("Cut B")` annotations on the affected Baikal integration tests. This change fixes those three bugs so Baikal becomes fully exercised by the integration suite, and unblocks the follow-up `complete-dav-module-cut-b` feature work (multiget, free-busy-query, merge, export, workspace-as-principal) — which it depends on.

## What Changes

- **Fix MKCOL root element in `MkColEntity.toXml(...)`** — emit `<D:mkcol>` per RFC 5689 (Extended MKCOL) instead of the current `<D:create>`. RFC-strict servers (Baikal) reject the current request with `400 Bad Request`; lenient servers (Radicale) accept either, so the change is forward-compatible.
- **Fix path double-prefixing in `DefaultDavClient.resolvePath(String)`** — when the incoming `path` already begins with the configured `repositoryPath`, don't prepend it again. Baikal's `calendar-home-set` / `addressbook-home-set` propfind responses return server-absolute paths like `/dav.php/calendars/test/`; the current `resolvePath` produces `/dav.php/dav.php/calendars/test/` and gets a `404`. Servers that return paths without the prefix continue to work unchanged.
- **Fix property-value type assumption in `GetPropertyValue.handleResponse(...)`** — handle the case where the property's underlying value is already a `String` (Baikal's representation of href-typed properties) in addition to the current `Element` cast. Currently throws `ClassCastException` on Baikal.
- **Remove `@IgnoreIf("Cut B")` annotations** from `AbstractCalendarStoreIntegrationTest`, `AbstractCardStoreIntegrationTest`, and the `BaikalCardStoreIntegrationTest` / `BaikalCalendarStoreIntegrationTest` subclasses for the tests these bugs were blocking. After the fixes, those tests should pass against Baikal alongside Radicale.
- **Out of scope** (deferred to `complete-dav-module-cut-b`): `calendar-multiget`, `free-busy-query`, `merge(String, CalendarCollection)`, `export()`, workspace-as-principal-switching.

## Capabilities

### New Capabilities
- `dav-protocol-compliance`: Cross-cutting low-level DAV client invariants for RFC compliance and server-implementation portability — request body formats (MKCOL per RFC 5689), path resolution (idempotent under server-absolute responses), and response parsing (tolerant of both Element-wrapped and String-shaped property values). These were implicitly assumed before but not stated; this change makes them explicit so future changes can rely on them.

### Modified Capabilities
<!-- None — `dav-calendar-connector` and `dav-card-connector` requirement text is
     already correct; the bugs were violating it. Fix removes the violation. -->


## Impact

- **Module**: `ical4j-connector-dav`
  - Modified: `request/MkColEntity.java` (root element), `DefaultDavClient.java` (resolvePath), `response/GetPropertyValue.java` (cast/String handling).
  - Test changes: remove `@IgnoreIf` predicates from 7 feature methods across `AbstractCalendarStoreIntegrationTest` (1) and `AbstractCardStoreIntegrationTest` (6).
- **API surface**: No changes to `ical4j-connector-api`. No changes to public method signatures in the DAV module.
- **Dependencies**: No new dependencies.
- **Risk**: All three fixes change behaviour in low-level shared code paths. Mitigation is the existing Testcontainers integration suite — running against Baikal *and* Radicale (plus the already-`@Ignore`'d Bedework) catches regressions in either direction. The MkCol root-element change has the highest theoretical risk (some non-RFC server could rely on `<D:create>`), but no such server is currently tested or known to be used.
- **Spec deltas**: None. Existing requirement text in both capabilities already specifies the correct behaviour; this change makes that behaviour observable on Baikal in addition to Radicale.

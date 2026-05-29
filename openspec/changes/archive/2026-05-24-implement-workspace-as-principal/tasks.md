## 1. CalDavCalendarStore — principal-aware home-set + workspace routing

- [x] 1.1 Refactored `findCalendarHomeSet()` to delegate to a new private `findCalendarHomeSetForPrincipal(String principal)` (cleaner name than overloading; the existing single-arg version took a URI, not a principal). The session-user entry point is the no-arg `findCalendarHomeSet()`.
- [x] 1.2 No new `getCollectionsForHomeSet(...)` overload needed beyond the existing one — but added a 3-arg variant that takes a principal override so the resulting collections know which principal to address.
- [x] 1.3 Rewrote `getCollections(String workspace)`: null/DEFAULT delegates to session-user `getCollections()`; otherwise propfind that principal's home set and list collections under it with the principal override.
- [x] 1.4 Rewrote `getCollection(String id, String workspace)`: null/DEFAULT routes via session-user workspace; otherwise routes via the named principal. Result collection carries the principal override. The no-arg `getCollection(String id)` now delegates to `getCollection(id, null)` instead of `getCollection(id, DEFAULT_WORKSPACE)` (the latter previously passed the literal `"default"` string to `pathResolver.getCalendarPath`, which was broken for any session user not named "default" — fixed as part of this change).
- [x] 1.5 Rewrote `addCollection(String name, String workspace)`: null/DEFAULT delegates; otherwise constructs `CalDavCalendarCollection(this, name, name, "", workspace)` (principal-override-aware 5-arg constructor — see Task 3.1) and creates.
- [x] 1.6 Rewrote `addCollection(String id, String name, String description, String[] supportedComponents, Calendar timezone, String workspace)`: same dispatch shape using the principal-override-aware constructor.
- [x] 1.7 Deleted the private `assertDefaultWorkspace(String)` helper.

## 2. CardDavStore — same shape

- [x] 2.1 Added private `findAddressBookHomeSetForPrincipal(String principal)`; no-arg `findAddressBookHomeSet()` delegates to it.
- [x] 2.2 Rewrote `getCollections(String workspace)` with the null/DEFAULT-delegate-or-principal-route dispatch.
- [x] 2.3 Rewrote `getCollection(String id, String workspace)` similarly; no-arg version delegates with null workspace.
- [x] 2.4 Rewrote `addCollection(String name, String workspace)` with the dispatch + principal-override constructor.
- [x] 2.5 Rewrote `addCollection(String id, String name, String description, String[] supportedComponents, Calendar timezone, String workspace)` with the dispatch.
- [x] 2.6 Deleted the private `assertDefaultWorkspace(String)` helper.

## 3. Collection constructor overloads to carry a principal override

- [x] 3.1 Added `CalDavCalendarCollection(store, id, displayName, description, principalOverride)` constructor + a `CalDavCalendarCollection(store, id, props, principalOverride)` variant for the props-driven path. `getPath()` uses the override if set, otherwise falls back to `getStore().getSessionConfiguration().getWorkspace()`.
- [x] 3.2 Equivalent constructor pair on `CardDavCollection` + `getPath()` override using `pathResolver.getCardPath(...)`.

## 4. EventQuery getetag namespace fix

- [x] 4.1 Changed `newCalDavElement(document, DavConstants.PROPERTY_GETETAG)` to `newElement(document, DavConstants.PROPERTY_GETETAG, DavConstants.NAMESPACE)` in `EventQuery.toXml`. Also (added during apply): removed a pre-existing `document.appendChild(property)` bug that conflicted with `ReportInfo.toXml`'s root-element insertion (would throw `HIERARCHY_REQUEST_ERR`). Same bug shape we fixed in `CalendarMultiget` two changes ago; was latent because no integration tests exercised `getEventsForTimePeriod`. Verified with the new Task 6 tests.

## 5. Integration tests — workspace-as-principal

- [x] 5.1 Added `'test getCollections accepts session-user workspace'` to `AbstractCalendarStoreIntegrationTest`: verifies `store.getCollections(getUser())` and `store.getCollections(DEFAULT_WORKSPACE)` return the same set of collection ids.
- [x] 5.2 Added `'test getCollections rejects unknown foreign principal'`: verifies `store.getCollections("nonexistent-other-principal")` raises `ObjectStoreException` (server 403 propagated and wrapped — see Task 5.4).
- [x] 5.3 Added the same two tests to `AbstractCardStoreIntegrationTest` for `CardDavStore`.
- [x] 5.4 (Added during apply): updated `getCollections(workspace)` on both stores to catch `RuntimeException` in addition to `DavException | IOException` and wrap in `ObjectStoreException`. The `GetPropertyValue` / `GetCollections` response handlers throw `RuntimeException` from `AbstractResponseHandler.getMultiStatus` on non-2xx; without the catch the negative test would see `RuntimeException` instead of the spec-required `ObjectStoreException`.

## 6. Integration tests — EventQuery namespace fix

- [x] 6.1 Added `'test getEventsForTimePeriod returns added events'` to `AbstractCalendarStoreIntegrationTest`: add a timed event, query over a covering window, assert non-empty result containing the event UID.
- [x] 6.2 Added `'test getEventsForTimePeriod returns empty list for empty collection'`.

## 7. Verification

- [x] 7.1 Ran integration tests across the 4 store classes (`RadicaleCalendar`, `RadicaleCard`, `BaikalCalendar`, `BaikalCard`): 56 tests, 5 skipped (pre-existing server-specific), **0 failures**. Stable across reruns.
- [x] 7.2 Attempted full DAV module suite. Hit pre-existing Radicale container-startup flakes — `RadicaleDavClientIntegrationTest` or `RadicaleCardStoreIntegrationTest` randomly fail ~1/4 runs with `ContainerLaunchException: RetryCountExceededException`. Not caused by this change; the 4 store classes are reliable. Full suite when not hitting the flake: 99 tests, 28 skipped, 0 failures.
- [x] 7.3 `./gradlew :ical4j-connector-api:check` and `:ical4j-connector-dav:check -x test` both clean. Full `make check` blocked by pre-existing unrelated `:ical4j-connector-google:compileJava` failure — out of scope.

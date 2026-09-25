## Why

The `ical4j-connector-dav` module has substantial low-level DAV client code and high-level store/collection scaffolding, but several `ObjectStore`/`ObjectCollection` methods are still stubbed with `UnsupportedOperationException` or `return null` — most notably `listObjectUIDs`, `getDescription` on `CardDavCollection`, `removeAll` on `CardDavCollection`, the 5-arg `addCollection(...)` on `CardDavStore`, and every workspace-parameterised method on both stores. The sibling MSGraph and (in-flight) Google connectors have reached a "happy-path read/add/delete" baseline; the older DAV module should match that bar so cross-provider code can rely on a consistent contract.

## What Changes

- Implement `CalDavCalendarCollection.listObjectUIDs()` by querying calendar object URIs from the collection and extracting UIDs (via existing calendar-query report or PROPFIND on the collection).
- Implement `CardDavCollection.listObjectUIDs()` mirroring the above using addressbook-query.
- Implement `CardDavCollection.getDescription()` by reading `CardDavPropertyName.ADDRESSBOOK_DESCRIPTION`.
- Implement `CardDavCollection.removeAll(String...)` mirroring `CalDavCalendarCollection.removeAll` (resolve each UID to a default URI, delete, return removed cards).
- Implement `CardDavStore.addCollection(String id, String name, String description, String[] supportedComponents, Calendar timezone)` to create an addressbook collection with the given display name and description (ignore `supportedComponents` and `timezone` — neither maps to CardDAV).
- Adopt the `DEFAULT_WORKSPACE` pattern from the Google connector for both stores: `listWorkspaceIds()` returns `[ObjectStore.DEFAULT_WORKSPACE]`; every `*(..., String workspace)` method throws `ObjectStoreException` if `workspace` is non-null and not equal to `DEFAULT_WORKSPACE`, otherwise delegates to the non-workspace variant. **BREAKING** vs. current stubs: those methods previously threw `UnsupportedOperationException` unconditionally.
- Out of scope (deferred to a follow-up "Cut B" change): `calendar-multiget`, `free-busy-query`, `merge(String, CalendarCollection)`, `export()`, and any future work that interprets `workspace` as a DAV principal switch.

## Capabilities

### New Capabilities
- `dav-calendar-connector`: Read/write access to a CalDAV server's calendars via the iCal4j `ObjectStore`/`CalendarCollection` abstractions, including collection CRUD, calendar object CRUD, UID listing, and metadata properties (display name, description, supported components, timezone).
- `dav-card-connector`: Read/write access to a CardDAV server's address books via the iCal4j `ObjectStore`/`CardCollection` abstractions, including collection CRUD, vCard CRUD, UID listing, and metadata properties (display name, description).

### Modified Capabilities
<!-- None — no existing specs in openspec/specs/. -->

## Impact

- **Module**: `ical4j-connector-dav`
  - Modified: `CalDavCalendarStore` (workspace methods), `CardDavStore` (workspace methods, 5-arg `addCollection`), `CalDavCalendarCollection` (`listObjectUIDs`), `CardDavCollection` (`listObjectUIDs`, `getDescription`, `removeAll`).
  - Unchanged: low-level DAV client (`DefaultDavClient`, `request/`, `response/`, `method/`), `AbstractDavResource`, builders, path resolvers.
- **API surface**: No breaking changes to `ical4j-connector-api`. The workspace-method behaviour change is internal to the DAV module — current callers receive `UnsupportedOperationException` either way; behaviour for `workspace == null` or `workspace == DEFAULT_WORKSPACE` becomes "succeeds" instead of "throws", which is strictly more permissive.
- **Dependencies**: No new dependencies. No changes to `gradle/libs.versions.toml` or `module-info.java`.
- **Tests**: Extend existing `AbstractCalendarStoreIntegrationTest` with assertions covering `listObjectUIDs` and the workspace-method contract. Introduce a new `AbstractCardStoreIntegrationTest` mirroring the calendar one for CardDAV operations. Both run against the existing Baikal + Radicale Testcontainers fixtures. No new unit-test infrastructure (no `DavClient` interface extraction).

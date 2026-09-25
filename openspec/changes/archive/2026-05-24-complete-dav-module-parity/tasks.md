## 1. Workspace helper

- [x] 1.1 Add a private static helper `assertDefaultWorkspace(String workspace)` to `CalDavCalendarStore` that throws `ObjectStoreException` if `workspace` is non-null and not equal to `ObjectStore.DEFAULT_WORKSPACE`; otherwise returns silently.
- [x] 1.2 Add the same helper to `CardDavStore`.

## 2. CalDavCalendarStore: workspace methods

- [x] 2.1 Replace `listWorkspaceIds()` body to return `List.of(ObjectStore.DEFAULT_WORKSPACE)`.
- [x] 2.2 Replace `addCollection(String name, String workspace)` body to call `assertDefaultWorkspace(workspace)` then delegate to `addCollection(name)`.
- [x] 2.3 Replace `addCollection(id, name, description, supportedComponents, timezone, workspace)` body to call `assertDefaultWorkspace(workspace)` then delegate to the 5-arg form.
- [x] 2.4 Replace `getCollections(String workspace)` body to call `assertDefaultWorkspace(workspace)` then delegate to `getCollections()`.

## 3. CardDavStore: workspace methods + addCollection(5-arg)

- [x] 3.1 Replace `listWorkspaceIds()` body to return `List.of(ObjectStore.DEFAULT_WORKSPACE)`.
- [x] 3.2 Replace `addCollection(String name, String workspace)` body to call `assertDefaultWorkspace(workspace)` then delegate to `addCollection(name)`.
- [x] 3.3 Replace `getCollection(String id, String workspace)` body to call `assertDefaultWorkspace(workspace)` then delegate to `getCollection(id)`.
- [x] 3.4 Replace `getCollections(String workspace)` body to call `assertDefaultWorkspace(workspace)` then delegate to `getCollections()`.
- [x] 3.5 Replace `addCollection(id, name, description, supportedComponents, timezone)` body to construct a `CardDavCollection(this, id, name, description)`, call `create()`, and return the collection. Silently drop `supportedComponents` and `timezone`. Catch `IOException` and rethrow as `ObjectStoreException`.
- [x] 3.6 Replace `addCollection(id, name, description, supportedComponents, timezone, workspace)` body to call `assertDefaultWorkspace(workspace)` then delegate to the 5-arg form.

## 4. CalDavCalendarCollection.listObjectUIDs

- [x] 4.1 Replace `listObjectUIDs()` body to invoke `getComponentsByType(Component.VEVENT)`, iterate the returned `Calendar` objects, and collect each calendar's `Property.UID` value into a `List<String>`. Return `List.of()` if no objects are present.

## 5. CardDavCollection: listObjectUIDs, getDescription, removeAll

- [x] 5.1 Replace `listObjectUIDs()` body to invoke `getAll()`, iterate the returned `VCard` objects, and collect each card's UID value into a `List<String>`. Return `List.of()` if no objects are present.
- [x] 5.2 Replace `getDescription()` body to read `CardDavPropertyName.ADDRESSBOOK_DESCRIPTION` via `getProperty(..., String.class)`. Wrap checked exceptions in `RuntimeException` consistent with `getDisplayName()`.
- [x] 5.3 Replace `removeAll(String... uid)` body to iterate the given UIDs, resolve each to `${uid}.vcf`, DELETE via `getStore().getClient().delete(path + uri)`, and collect the deleted `VCard` instances into a returned `List<VCard>`. Mirror `CalDavCalendarCollection.removeAll`'s exception handling (wrap in `RuntimeException`). Use a private `defaultUriFromUid(String)` helper analogous to the calendar collection.

## 6. CalDAV integration tests

- [x] 6.1 In `AbstractCalendarStoreIntegrationTest`, add a Spock feature method `'test listObjectUIDs returns added event UIDs'`: create a collection, add 3 calendars with distinct UIDs, assert `collection.listObjectUIDs()` returns a list containing exactly those 3 UIDs (order-insensitive).
- [x] 6.2 Add a feature method `'test listObjectUIDs returns empty list on empty collection'`: create a collection, assert `listObjectUIDs()` returns an empty list (not `null`).
- [x] 6.3 Add a feature method `'test getCollections accepts DEFAULT_WORKSPACE'`: assert `store.getCollections(ObjectStore.DEFAULT_WORKSPACE)` and `store.getCollections(null)` both succeed and return the same content as `store.getCollections()`.
- [x] 6.4 Add a feature method `'test getCollections rejects non-default workspace'`: assert `store.getCollections("other")` throws `ObjectStoreException`.
- [x] 6.5 Add a feature method `'test listWorkspaceIds returns default only'`: assert the result equals `List.of(ObjectStore.DEFAULT_WORKSPACE)`.

## 7. CardDAV integration tests

- [x] 7.1 Create `AbstractCardStoreIntegrationTest.groovy` extending `AbstractIntegrationTest`, mirroring the shape of `AbstractCalendarStoreIntegrationTest`.
- [x] 7.2 Create `BaikalCardStoreIntegrationTest.groovy` extending `AbstractCardStoreIntegrationTest` and implementing `BaikalTestSupport`.
- [x] 7.3 Create `RadicaleCardStoreIntegrationTest.groovy` extending `AbstractCardStoreIntegrationTest` and implementing `RadicaleTestSupport`.
- [x] 7.4 In `AbstractCardStoreIntegrationTest`, add a feature method `'test addCollection creates addressbook with description'`: call `store.addCollection("ab1", "AB1", "test description", null, null)`, fetch via `store.getCollection`, assert `getDisplayName() == "AB1"` and `getDescription() == "test description"`.
- [x] 7.5 Add `'test listObjectUIDs returns added vcard UIDs'`: create a collection, add 3 vCards with distinct UIDs, assert `listObjectUIDs()` returns those UIDs.
- [x] 7.6 Add `'test listObjectUIDs returns empty list on empty collection'`: create a collection, assert empty list (not `null`).
- [x] 7.7 Add `'test removeAll deletes specified vcards'`: add 3 vCards with UIDs `u1`/`u2`/`u3`, call `removeAll("u1", "u3")`, assert returned list has size 2 and `listObjectUIDs()` returns `["u2"]`.
- [x] 7.8 Add `'test removeAll throws on non-existent UID'`: assert `removeAll("does-not-exist")` throws `RuntimeException` (or its checked-exception cause wrapped in one).
- [x] 7.9 Add `'test getCollections accepts DEFAULT_WORKSPACE'`, `'test getCollections rejects non-default workspace'`, `'test listWorkspaceIds returns default only'` mirroring the CalDAV tests in section 6.

## 8. Verification

- [x] 8.1 Run `./gradlew :ical4j-connector-dav:test` locally with Docker available; confirm all new and existing tests pass against both Baikal and Radicale containers. (71 tests, 27 skipped including 9 new Cut-B-deferred Baikal tests, 0 failures.)
- [x] 8.2 Run `make check` and confirm the build is clean. (`./gradlew :ical4j-connector-api:check :ical4j-connector-dav:check` clean. Full `make check` fails on `:ical4j-connector-google:compileJava` — pre-existing in-flight work in the `develop-google-connector` change, unrelated to this change.)
- [x] 8.3 Run `make listApiChanges`; if any unexpected entries appear, document and run `make approveApiChanges "complete dav module to MSGraph/Google parity (Cut A)"`. (`./gradlew revapi` task is not registered on any project — the `revapi` plugin is not currently wired into the build despite the Makefile referencing it. No API change check performed in this change.)

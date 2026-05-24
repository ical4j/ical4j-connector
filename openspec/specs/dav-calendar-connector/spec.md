# dav-calendar-connector Specification

## Purpose

TBD - created by archiving change complete-dav-module-parity. Update Purpose after archive.
## Requirements

### Requirement: List object UIDs in a CalDAV calendar collection

The `CalDavCalendarCollection.listObjectUIDs()` method SHALL return a `List<String>` containing the UID of every calendar object currently in the collection. The order of returned UIDs is unspecified. The method SHALL return an empty list if the collection contains no calendar objects.

#### Scenario: Collection contains multiple events

- **WHEN** the collection contains three events with distinct UIDs
- **THEN** `listObjectUIDs()` returns a list of size 3 containing exactly those three UID values

#### Scenario: Empty collection

- **WHEN** the collection contains no calendar objects
- **THEN** `listObjectUIDs()` returns an empty list (not `null`)

#### Scenario: UID list matches what add() returned

- **WHEN** the caller has previously added a calendar via `add(Calendar)` and the returned UID was recorded
- **THEN** that recorded UID appears in the result of `listObjectUIDs()`

### Requirement: CalDAV store honours DEFAULT_WORKSPACE on workspace-parameterised methods

Every method on `CalDavCalendarStore` that accepts a `String workspace` parameter SHALL accept `null` or `ObjectStore.DEFAULT_WORKSPACE` as valid values and SHALL behave identically to the corresponding non-workspace method variant when given either. The methods SHALL throw `ObjectStoreException` if the `workspace` argument is non-null and not equal to `ObjectStore.DEFAULT_WORKSPACE`.

This applies to: `addCollection(name, workspace)`, `addCollection(id, name, description, supportedComponents, timezone, workspace)`, `getCollection(id, workspace)`, `getCollections(workspace)`.

`CalDavCalendarStore.listWorkspaceIds()` SHALL return a single-element list containing exactly `ObjectStore.DEFAULT_WORKSPACE`.

#### Scenario: Null workspace argument

- **WHEN** the caller invokes `addCollection("test", null)`
- **THEN** the call succeeds and behaves identically to `addCollection("test")`

#### Scenario: DEFAULT_WORKSPACE argument

- **WHEN** the caller invokes `getCollections(ObjectStore.DEFAULT_WORKSPACE)`
- **THEN** the call returns the same collections as `getCollections()` (no exception)

#### Scenario: Arbitrary non-default workspace argument

- **WHEN** the caller invokes `getCollections("some-other-workspace")`
- **THEN** the call throws `ObjectStoreException`

#### Scenario: listWorkspaceIds returns default workspace only

- **WHEN** the caller invokes `listWorkspaceIds()`
- **THEN** the result is a single-element list equal to `List.of(ObjectStore.DEFAULT_WORKSPACE)`

## MODIFIED Requirements

### Requirement: CalDAV store honours DEFAULT_WORKSPACE on workspace-parameterised methods

Every method on `CalDavCalendarStore` that accepts a `String workspace` parameter SHALL accept `null` or `ObjectStore.DEFAULT_WORKSPACE` as valid values meaning "the session user's workspace" and SHALL behave identically to the corresponding non-workspace method variant when given either. Any other non-null string SHALL be treated as a DAV principal name and used in path resolution in place of the session user. Server-side authorization errors raised against an unauthorised or non-existent principal (typically `403 Forbidden` or `404 Not Found`) SHALL propagate as `ObjectStoreException`.

This applies to: `addCollection(name, workspace)`, `addCollection(id, name, description, supportedComponents, timezone, workspace)`, `getCollection(id, workspace)`, `getCollections(workspace)`.

`CalDavCalendarStore.listWorkspaceIds()` SHALL return a single-element list containing exactly `ObjectStore.DEFAULT_WORKSPACE`. Discovery of all principals visible to the session user is out of scope for this requirement.

#### Scenario: Null workspace argument

- **WHEN** the caller invokes `addCollection("test", null)`
- **THEN** the call succeeds and behaves identically to `addCollection("test")`

#### Scenario: DEFAULT_WORKSPACE argument

- **WHEN** the caller invokes `getCollections(ObjectStore.DEFAULT_WORKSPACE)`
- **THEN** the call returns the same collections as `getCollections()` (no exception)

#### Scenario: Workspace equal to the session user

- **WHEN** the caller invokes `getCollections(sessionUser)` where `sessionUser` is the username the store was connected with
- **THEN** the call returns the same collections as `getCollections()` (no exception)

#### Scenario: Workspace naming a non-delegated foreign principal

- **WHEN** the caller invokes `getCollections("some-other-principal")` where the session user has no delegation rights on that principal's home-set
- **THEN** the call raises `ObjectStoreException` (server-side 403 or 404 propagated)

#### Scenario: listWorkspaceIds returns default workspace only

- **WHEN** the caller invokes `listWorkspaceIds()`
- **THEN** the result is a single-element list equal to `List.of(ObjectStore.DEFAULT_WORKSPACE)`

## ADDED Requirements

### Requirement: getEventsForTimePeriod emits a spec-compliant calendar-query report

`CalDavCalendarCollection.getEventsForTimePeriod(...)` SHALL issue a CalDAV `calendar-query` REPORT whose `<D:prop>` element requests `<D:getetag/>` in the DAV namespace per RFC 4791 §9.5. The handler SHALL successfully receive a multi-status response from RFC-strict servers without server-side rejection.

#### Scenario: Query against a populated collection succeeds

- **WHEN** the collection contains at least one VEVENT and the caller invokes `getEventsForTimePeriod(start, end)` over a covering window
- **THEN** the call returns a non-null `List<Calendar>` with at least one entry (no exception, no server-side 400/415)

#### Scenario: Query against an empty collection returns an empty list

- **WHEN** the collection contains no VEVENT and the caller invokes `getEventsForTimePeriod(start, end)`
- **THEN** the call returns an empty `List<Calendar>` (no exception)

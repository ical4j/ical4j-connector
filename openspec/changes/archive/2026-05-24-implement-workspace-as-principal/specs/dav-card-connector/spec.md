## MODIFIED Requirements

### Requirement: CardDAV store honours DEFAULT_WORKSPACE on workspace-parameterised methods

Every method on `CardDavStore` that accepts a `String workspace` parameter SHALL accept `null` or `ObjectStore.DEFAULT_WORKSPACE` as valid values meaning "the session user's workspace" and SHALL behave identically to the corresponding non-workspace method variant when given either. Any other non-null string SHALL be treated as a DAV principal name and used in path resolution in place of the session user. Server-side authorization errors raised against an unauthorised or non-existent principal (typically `403 Forbidden` or `404 Not Found`) SHALL propagate as `ObjectStoreException`.

This applies to: `addCollection(name, workspace)`, `addCollection(id, name, description, supportedComponents, timezone, workspace)`, `getCollection(id, workspace)`, `getCollections(workspace)`.

`CardDavStore.listWorkspaceIds()` SHALL return a single-element list containing exactly `ObjectStore.DEFAULT_WORKSPACE`. Discovery of all principals visible to the session user is out of scope for this requirement.

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

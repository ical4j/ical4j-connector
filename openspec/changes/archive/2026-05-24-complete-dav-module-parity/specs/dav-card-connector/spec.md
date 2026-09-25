## ADDED Requirements

### Requirement: List object UIDs in a CardDAV addressbook collection

The `CardDavCollection.listObjectUIDs()` method SHALL return a `List<String>` containing the UID of every vCard currently in the collection. The order of returned UIDs is unspecified. The method SHALL return an empty list if the collection contains no vCards.

#### Scenario: Collection contains multiple vCards

- **WHEN** the collection contains three vCards with distinct UIDs
- **THEN** `listObjectUIDs()` returns a list of size 3 containing exactly those three UID values

#### Scenario: Empty collection

- **WHEN** the collection contains no vCards
- **THEN** `listObjectUIDs()` returns an empty list (not `null`)

#### Scenario: UID list matches what add() returned

- **WHEN** the caller has previously added a vCard via `add(VCard)` and the returned UID was recorded
- **THEN** that recorded UID appears in the result of `listObjectUIDs()`

### Requirement: Read addressbook description from a CardDAV collection

`CardDavCollection.getDescription()` SHALL return the value of the `CARDDAV:addressbook-description` property if set on the collection, or `null` if the property is unset.

#### Scenario: Collection created with description

- **WHEN** a collection was created with description `"Family contacts"` and the caller invokes `getDescription()`
- **THEN** the result is `"Family contacts"`

#### Scenario: Collection with no description

- **WHEN** a collection has no `addressbook-description` property set
- **THEN** `getDescription()` returns `null`

### Requirement: Remove multiple vCards by UID from a CardDAV collection

`CardDavCollection.removeAll(String... uids)` SHALL delete each vCard identified by the given UIDs from the server and SHALL return a `List<VCard>` containing the deleted vCards. The method SHALL throw a `RuntimeException` if any deletion fails (mirroring the `CalDavCalendarCollection.removeAll` contract).

#### Scenario: Remove a single existing vCard

- **WHEN** the collection contains a vCard with UID `"abc-123"` and the caller invokes `removeAll("abc-123")`
- **THEN** the method returns a single-element list containing that vCard, and a subsequent `listObjectUIDs()` does not contain `"abc-123"`

#### Scenario: Remove multiple vCards

- **WHEN** the collection contains vCards with UIDs `"u1"`, `"u2"`, `"u3"` and the caller invokes `removeAll("u1", "u3")`
- **THEN** the returned list has size 2 and a subsequent `listObjectUIDs()` returns a list containing only `"u2"`

#### Scenario: Remove non-existent UID

- **WHEN** the caller invokes `removeAll("does-not-exist")`
- **THEN** the method throws a `RuntimeException`

### Requirement: Create a CardDAV addressbook collection with display name and description

`CardDavStore.addCollection(String id, String name, String description, String[] supportedComponents, Calendar timezone)` SHALL create a new addressbook collection on the server at the location resolved from `id`, with `displayName=name` and `addressbook-description=description`. The `supportedComponents` and `timezone` parameters SHALL be silently ignored (they have no CardDAV equivalent). The method SHALL return a `CardDavCollection` wrapping the newly created collection.

#### Scenario: Create with display name and description

- **WHEN** the caller invokes `addCollection("contacts", "Contacts", "My contacts", null, null)`
- **THEN** a new addressbook is created, the returned `CardDavCollection.getDisplayName()` is `"Contacts"`, and `getDescription()` is `"My contacts"`

#### Scenario: supportedComponents and timezone are ignored

- **WHEN** the caller invokes `addCollection("c2", "C2", "desc", new String[]{"VEVENT"}, new Calendar())`
- **THEN** the call succeeds and the calendar-specific parameters do not cause an error

### Requirement: CardDAV store honours DEFAULT_WORKSPACE on workspace-parameterised methods

Every method on `CardDavStore` that accepts a `String workspace` parameter SHALL accept `null` or `ObjectStore.DEFAULT_WORKSPACE` as valid values and SHALL behave identically to the corresponding non-workspace method variant when given either. The methods SHALL throw `ObjectStoreException` if the `workspace` argument is non-null and not equal to `ObjectStore.DEFAULT_WORKSPACE`.

This applies to: `addCollection(name, workspace)`, `addCollection(id, name, description, supportedComponents, timezone, workspace)`, `getCollection(id, workspace)`, `getCollections(workspace)`.

`CardDavStore.listWorkspaceIds()` SHALL return a single-element list containing exactly `ObjectStore.DEFAULT_WORKSPACE`.

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

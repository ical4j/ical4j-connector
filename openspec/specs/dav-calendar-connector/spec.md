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

### Requirement: Fetch specific calendar objects by href via multiget

`CalDavCalendarCollection.getObjectsByMultiget(List<String> hrefs)` SHALL issue a CalDAV `calendar-multiget` REPORT (RFC 4791 §7.9) requesting GETETAG and calendar-data for each href, and return a `List<Calendar>` containing the calendar objects successfully fetched. Hrefs that the server returns with a non-OK per-href status SHALL be omitted from the result (no exception). If the input list is empty, the method SHALL return an empty list without issuing a request.

#### Scenario: Fetch two known hrefs

- **WHEN** the collection contains three calendars at URIs `uid1.ics`, `uid2.ics`, `uid3.ics`, and the caller invokes `getObjectsByMultiget(List.of("<collection-path>/uid1.ics", "<collection-path>/uid3.ics"))`
- **THEN** the result is a `List<Calendar>` of size 2 containing the calendars for UIDs `uid1` and `uid3`

#### Scenario: Fetch with one missing href

- **WHEN** the collection contains a calendar at `uid1.ics` and the caller invokes `getObjectsByMultiget(List.of("<collection-path>/uid1.ics", "<collection-path>/does-not-exist.ics"))`
- **THEN** the result contains exactly one Calendar (for `uid1`); the missing href is silently dropped

#### Scenario: Empty href list

- **WHEN** the caller invokes `getObjectsByMultiget(List.of())`
- **THEN** the result is an empty list and no HTTP request is issued

### Requirement: Query free/busy intervals over a time range

`CalDavCalendarCollection.doFreeBusyQuery(Instant start, Instant end)` SHALL issue a CalDAV `free-busy-query` REPORT (RFC 4791 §7.10) bounding the query to the half-open interval `[start, end)`, and return a `Calendar` containing exactly one VFREEBUSY component synthesized by the server.

#### Scenario: Query covering a known event

- **WHEN** the collection contains an event from `2030-01-01T10:00:00Z` to `2030-01-01T11:00:00Z` and the caller invokes `doFreeBusyQuery(parse("2030-01-01T00:00:00Z"), parse("2030-01-02T00:00:00Z"))`
- **THEN** the result is a non-null `Calendar` containing a `VFREEBUSY` whose FREEBUSY property includes the interval `2030-01-01T10:00:00Z/2030-01-01T11:00:00Z`

#### Scenario: Query covering no events

- **WHEN** the collection contains no events overlapping the time range
- **THEN** the result is a non-null `Calendar` containing a `VFREEBUSY` with no FREEBUSY intervals

### Requirement: Export all events in a collection as a single iCalendar

`CalDavCalendarCollection.export()` SHALL return a single `net.fortuna.ical4j.model.Calendar` that aggregates every VEVENT in the collection. The aggregation uses iCal4j's `Calendar.merge` semantics — properties deduplicated, components unioned. The method SHALL return an empty `Calendar` (no components) if the collection contains no events.

#### Scenario: Export aggregates multiple events

- **WHEN** the collection contains three events with distinct UIDs and the caller invokes `export()`
- **THEN** the returned `Calendar` contains three `VEVENT` components, one per UID

#### Scenario: Export empty collection

- **WHEN** the collection contains no events
- **THEN** `export()` returns a `Calendar` with zero `VEVENT` components (not `null`, no exception)

### Requirement: getEventsForTimePeriod emits a spec-compliant calendar-query report

`CalDavCalendarCollection.getEventsForTimePeriod(...)` SHALL issue a CalDAV `calendar-query` REPORT whose `<D:prop>` element requests `<D:getetag/>` in the DAV namespace per RFC 4791 §9.5. The handler SHALL successfully receive a multi-status response from RFC-strict servers without server-side rejection.

#### Scenario: Query against a populated collection succeeds

- **WHEN** the collection contains at least one VEVENT and the caller invokes `getEventsForTimePeriod(start, end)` over a covering window
- **THEN** the call returns a non-null `List<Calendar>` with at least one entry (no exception, no server-side 400/415)

#### Scenario: Query against an empty collection returns an empty list

- **WHEN** the collection contains no VEVENT and the caller invokes `getEventsForTimePeriod(start, end)`
- **THEN** the call returns an empty `List<Calendar>` (no exception)

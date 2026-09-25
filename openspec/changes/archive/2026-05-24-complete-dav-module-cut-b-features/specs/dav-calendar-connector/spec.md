## ADDED Requirements

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

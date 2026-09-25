## ADDED Requirements

### Requirement: Caller-constructed Google API client

The `GoogleCalendarStore` SHALL accept an authenticated `com.google.api.services.calendar.Calendar` client as a constructor argument and use it for all Google API interaction. The store SHALL NOT itself perform authentication.

#### Scenario: Store constructed with an authenticated client

- **WHEN** a caller constructs `new GoogleCalendarStore(client)` with a pre-authenticated `Calendar` client
- **THEN** the store is ready to service `addCollection` / `getCollection(s)` calls without any further setup

#### Scenario: connect() is a no-op stub

- **WHEN** a caller invokes `store.connect()` or `store.connect(username, password)`
- **THEN** the method returns `false` and performs no authentication

#### Scenario: disconnect() is a no-op stub

- **WHEN** a caller invokes `store.disconnect()`
- **THEN** the method returns without performing any action and without raising an exception

### Requirement: Workspace concept collapses to default

Because Google Calendar has no equivalent of calendar groups, the connector SHALL expose exactly one workspace, identified by `ObjectStore.DEFAULT_WORKSPACE`.

#### Scenario: listWorkspaceIds returns only the default workspace

- **WHEN** a caller invokes `store.listWorkspaceIds()`
- **THEN** the returned list contains exactly one element, equal to `ObjectStore.DEFAULT_WORKSPACE`

#### Scenario: Workspace-parameterised methods reject non-default workspaces

- **WHEN** a caller invokes `addCollection(name, workspace)`, `getCollection(id, workspace)`, or `getCollections(workspace)` with a `workspace` value that is not equal to `ObjectStore.DEFAULT_WORKSPACE`
- **THEN** the method raises `ObjectStoreException`

#### Scenario: Workspace-parameterised methods accept the default workspace

- **WHEN** a caller invokes `getCollections(ObjectStore.DEFAULT_WORKSPACE)`
- **THEN** the method behaves identically to `getCollections()`

### Requirement: Listing collections includes owned and subscribed calendars

`getCollections()` SHALL return one `CalendarCollection` for every calendar visible to the authenticated user, including subscribed calendars.

#### Scenario: User has owned and subscribed calendars

- **WHEN** `getCollections()` is invoked and the user has 2 owned and 3 subscribed calendars in `calendarList()`
- **THEN** the returned list has 5 elements, each a `GoogleCalendarCollection` wrapping the corresponding calendar id

### Requirement: Creating a collection creates an owned Google calendar

`addCollection(name)` SHALL create a new calendar in the user's account via the Google `calendars().insert(...)` endpoint with `summary = name`, and return a `CalendarCollection` wrapping the newly assigned id.

#### Scenario: Successful collection creation

- **WHEN** `addCollection("Team Events")` is invoked
- **THEN** the Google API receives an insert with `summary = "Team Events"` and the method returns a `GoogleCalendarCollection` whose id matches the id returned by Google

#### Scenario: Collection creation with description and timezone

- **WHEN** `addCollection(id, "Team Events", "Shared events", supportedComponents, timezone)` is invoked
- **THEN** the Google API receives an insert with `summary = "Team Events"`, `description = "Shared events"`, and `timeZone` derived from the iCal4j `timezone` argument
- **AND** the caller-supplied `id` and `supportedComponents` arguments are ignored (Google generates the id and does not honour per-collection component restrictions)

### Requirement: Deleting a collection removes the underlying Google calendar

`removeCollection(id)` and `CalendarCollection.delete()` SHALL delete the underlying calendar via `calendars().delete(id)`.

#### Scenario: Deleting an owned calendar succeeds

- **WHEN** `removeCollection(id)` is invoked for a calendar the user owns
- **THEN** the Google API receives a delete on that calendar id

#### Scenario: Deleting a subscribed calendar surfaces the API error

- **WHEN** `removeCollection(id)` is invoked for a calendar the user does not own
- **THEN** the underlying 403 from the Google API propagates as an `ObjectStoreException`

### Requirement: Reading events from a collection

`GoogleCalendarCollection` SHALL expose the events of its Google calendar through `listObjectUIDs()` and `get(uid)`, transparently paginating the Google `events().list()` call so callers see the full event set.

#### Scenario: listObjectUIDs returns every event's iCalUID

- **WHEN** `collection.listObjectUIDs()` is invoked on a calendar containing 5 events
- **THEN** the returned list contains 5 strings, each equal to the corresponding event's `iCalUID`

#### Scenario: listObjectUIDs paginates correctly

- **WHEN** `collection.listObjectUIDs()` is invoked on a calendar where `events().list()` returns results across 3 pages (each with a `nextPageToken` except the last)
- **THEN** the returned list contains the union of all 3 pages, with no duplicates and no truncation

#### Scenario: get(uid) returns the matching event as an iCal4j Calendar

- **WHEN** `collection.get(uid)` is invoked for a uid that matches an event's `iCalUID`
- **THEN** an `Optional<net.fortuna.ical4j.model.Calendar>` is returned containing a `Calendar` whose `VEVENT` has properties mapped from the Google `Event` per the mapping spec

#### Scenario: get(uid) returns empty for unknown uids

- **WHEN** `collection.get(uid)` is invoked for a uid not present in the calendar
- **THEN** an empty `Optional` is returned

### Requirement: Adding an event to a collection

`GoogleCalendarCollection.add(Calendar)` SHALL take an iCal4j `Calendar` containing a single `VEVENT`, convert it to a Google `Event` via `GoogleEventBuilder`, persist it via `events().insert(...)`, and return the resulting `iCalUID`.

#### Scenario: Adding a simple event

- **WHEN** `collection.add(calendar)` is invoked with a `Calendar` containing one `VEVENT` (summary, dtstart, dtend)
- **THEN** the Google API receives an insert with the mapped fields
- **AND** the returned string equals the inserted event's `iCalUID`

#### Scenario: Adding a calendar with no VEVENT raises an exception

- **WHEN** `collection.add(calendar)` is invoked with a `Calendar` containing no `VEVENT` component
- **THEN** the method raises `ObjectStoreException`

### Requirement: iCal4j ↔ Google Event field mapping

`GoogleEventBuilder` (VEvent → Google Event) and `ICalCalendarBuilder` (Google Event → iCal4j Calendar) SHALL map the following fields losslessly in both directions.

| iCal4j VEvent property              | Google Event field                                     |
| ----------------------------------- | ------------------------------------------------------ |
| UID                                 | iCalUID                                                |
| SUMMARY                             | summary                                                |
| DESCRIPTION                         | description                                            |
| LOCATION                            | location                                               |
| DTSTART (date)                      | start.date                                             |
| DTSTART (date-time, with TZID)      | start.dateTime + start.timeZone                        |
| DTEND (date)                        | end.date                                               |
| DTEND (date-time, with TZID)        | end.dateTime + end.timeZone                            |
| ORGANIZER                           | organizer.email / organizer.displayName                |
| ATTENDEE (each)                     | attendees[].email / displayName / responseStatus       |
| RRULE / RDATE / EXDATE              | recurrence[] (raw iCal lines, pass-through)            |
| CREATED                             | created                                                |
| LAST-MODIFIED                       | updated                                                |

Fields not in the table SHALL be silently dropped during conversion.

#### Scenario: All-day event round-trip

- **WHEN** a VEVENT with `DTSTART;VALUE=DATE:20260601` and `DTEND;VALUE=DATE:20260602` is converted to a Google Event and back
- **THEN** the round-tripped VEVENT has `DTSTART;VALUE=DATE:20260601` and `DTEND;VALUE=DATE:20260602` (no time components, no timezone)

#### Scenario: Timed event with timezone round-trip

- **WHEN** a VEVENT with `DTSTART;TZID=Australia/Melbourne:20260601T093000` is converted to a Google Event and back
- **THEN** the Google Event has `start.dateTime` set and `start.timeZone = "Australia/Melbourne"`
- **AND** the round-tripped VEVENT has `DTSTART;TZID=Australia/Melbourne:20260601T093000`

#### Scenario: Timed event without timezone defaults to UTC

- **WHEN** a VEVENT with `DTSTART:20260601T093000Z` (UTC, no TZID) is converted to a Google Event
- **THEN** the Google Event has `start.timeZone = "UTC"`

#### Scenario: PARTSTAT mapping

- **WHEN** an ATTENDEE with `PARTSTAT=ACCEPTED` is mapped to a Google attendee
- **THEN** the Google attendee's `responseStatus` is `"accepted"`
- **AND** the reverse mapping from `"accepted"` yields `PARTSTAT=ACCEPTED`

#### Scenario: Recurrence rule round-trip

- **WHEN** a VEVENT with `RRULE:FREQ=WEEKLY;BYDAY=MO` is converted to a Google Event
- **THEN** the Google Event's `recurrence[]` contains the string `"RRULE:FREQ=WEEKLY;BYDAY=MO"`
- **AND** the reverse mapping reconstructs the same `RRULE` on the VEVENT

#### Scenario: iCalUID preserved

- **WHEN** a VEVENT with `UID:some-uid@example.com` is converted to a Google Event
- **THEN** the Google Event's `iCalUID` equals `"some-uid@example.com"`
- **AND** the reverse mapping yields `UID:some-uid@example.com`

#### Scenario: Unmapped fields are dropped

- **WHEN** a VEVENT containing `URL`, `CLASS`, and `CATEGORIES` properties is converted to a Google Event
- **THEN** no exception is raised
- **AND** those properties do not appear on the resulting Google Event

### Requirement: Constructor field naming reflects role, not type

The `GoogleCalendarStore` constructor argument and the corresponding field SHALL be named `client` (not `calendarService`) to avoid collision with `net.fortuna.ical4j.model.Calendar` and the package's empty `GoogleCalendarService` class.

#### Scenario: Package-private accessor exists

- **WHEN** another class in the `org.ical4j.connector.google` package needs the Google API client to perform an operation on behalf of a `GoogleCalendarCollection`
- **THEN** it obtains the client via `store.getClient()`

### Requirement: Out-of-scope methods stub explicitly

`CalendarCollection` methods that are not part of the V1 parity scope SHALL return safe stub values, matching the MSGraph connector's current behaviour.

#### Scenario: merge returns an empty array

- **WHEN** `collection.merge(calendar)` is invoked
- **THEN** an empty `Uid[]` is returned

#### Scenario: export returns null

- **WHEN** `collection.export()` is invoked
- **THEN** `null` is returned

#### Scenario: removeAll returns an empty list

- **WHEN** `collection.removeAll(uids)` is invoked
- **THEN** an empty `List<Calendar>` is returned

#### Scenario: Listener accessors return null

- **WHEN** `store.getObjectStoreListeners()` or `collection.getObjectCollectionListeners()` is invoked
- **THEN** `null` is returned

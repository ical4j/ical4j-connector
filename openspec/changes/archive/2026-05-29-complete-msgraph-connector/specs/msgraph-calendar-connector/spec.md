## ADDED Requirements

### Requirement: Caller-constructed Microsoft Graph client

The `MSGraphCalendarStore` SHALL accept an authenticated `com.microsoft.graph.serviceclient.GraphServiceClient` as a constructor argument (via `AbstractMSGraphObjectStore`) and use it for all Graph API interaction. The store SHALL NOT itself perform authentication.

#### Scenario: Store constructed with an authenticated client

- **WHEN** a caller constructs `new MSGraphCalendarStore(client)` with a pre-authenticated `GraphServiceClient`
- **THEN** the store is ready to service `addCollection` / `getCollection(s)` calls without any further setup

#### Scenario: connect() and disconnect() are no-op stubs

- **WHEN** a caller invokes `store.connect()`, `store.connect(username, password)`, or `store.disconnect()`
- **THEN** `connect` variants return `false`, `disconnect` returns without action, and no authentication is performed

### Requirement: Calendar groups surface as workspaces

The connector SHALL expose Microsoft Graph calendar groups as workspaces. `listWorkspaceIds()` SHALL return the ids of the user's calendar groups, and the workspace-parameterised store methods SHALL operate within the calendar group identified by the supplied workspace id.

#### Scenario: listWorkspaceIds returns calendar group ids

- **WHEN** a caller invokes `store.listWorkspaceIds()`
- **THEN** the returned list contains the id of each calendar group returned by `me().calendarGroups()`

#### Scenario: Listing collections within a workspace

- **WHEN** `getCollections(workspace)` is invoked with a calendar group id
- **THEN** the returned collections are the calendars within that calendar group, each wrapping its group id

### Requirement: Creating a collection creates a Graph calendar

`addCollection(name)` SHALL create a calendar via `me().calendars().post(...)` with `name = name` and return a `CalendarCollection` wrapping the new id. `addCollection(name, workspace)` SHALL create the calendar within the calendar group identified by `workspace`.

#### Scenario: Successful collection creation

- **WHEN** `addCollection("Team Events")` is invoked
- **THEN** the Graph API receives a calendar POST with `name = "Team Events"` and the method returns an `MSGraphCalendarCollection` whose id matches the id returned by Graph

#### Scenario: Collection creation with description and timezone drops unsupported inputs

- **WHEN** `addCollection(id, "Team Events", "Shared events", supportedComponents, timezone)` is invoked
- **THEN** the Graph API receives a calendar POST with `name = "Team Events"`
- **AND** the caller-supplied `id`, `description`, `supportedComponents`, and `timezone` are ignored, because a Graph `Calendar` resource has no field to carry them

#### Scenario: Collection creation within a workspace

- **WHEN** `addCollection(id, "Team Events", "Shared events", supportedComponents, timezone, workspace)` is invoked with a calendar group id
- **THEN** the calendar is created within that calendar group

### Requirement: Deleting a collection removes the underlying Graph calendar

`removeCollection(id)` and `CalendarCollection.delete()` SHALL delete the underlying calendar via `me().calendars().byCalendarId(id).delete()`, using the calendar group path when the collection carries a group id.

#### Scenario: Deleting an ungrouped calendar

- **WHEN** `delete()` is invoked on a collection with no calendar group id
- **THEN** the Graph API receives a delete on `me().calendars().byCalendarId(id)`

#### Scenario: Deleting a grouped calendar

- **WHEN** `delete()` is invoked on a collection that carries a calendar group id
- **THEN** the Graph API receives a delete on the calendar within that calendar group

### Requirement: Reading events from a collection

`MSGraphCalendarCollection` SHALL expose the master events of its Graph calendar through `listObjectUIDs()` and `get(uid)`, transparently paginating the Graph `events()` collection (following `@odata.nextLink`) so callers see the full event set. Both methods SHALL read from `events()` (master events, one per UID), not `calendarView()`.

#### Scenario: listObjectUIDs returns every event's iCalUId

- **WHEN** `collection.listObjectUIDs()` is invoked on a calendar containing 5 events
- **THEN** the returned list contains 5 strings, each equal to the corresponding event's `iCalUId`

#### Scenario: listObjectUIDs paginates correctly

- **WHEN** `collection.listObjectUIDs()` is invoked on a calendar where `events()` returns results across 3 pages (each with an `@odata.nextLink` except the last)
- **THEN** the returned list contains the union of all 3 pages, with no truncation

#### Scenario: get(uid) returns the matching event as an iCal4j Calendar

- **WHEN** `collection.get(uid)` is invoked for a uid that matches an event's `iCalUId`
- **THEN** an `Optional<net.fortuna.ical4j.model.Calendar>` is returned containing a `Calendar` whose `VEVENT` has properties mapped from the Graph `Event` per the mapping requirement

#### Scenario: get(uid) returns empty for unknown uids

- **WHEN** `collection.get(uid)` is invoked for a uid not present in the calendar
- **THEN** an empty `Optional` is returned

### Requirement: Adding an event to a collection

`MSGraphCalendarCollection.add(Calendar)` SHALL take an iCal4j `Calendar` containing a single `VEVENT`, convert it to a Graph `Event` via `MSGraphEventBuilder`, persist it via `events().post(...)`, and return the `iCalUId` assigned by Graph in the response.

#### Scenario: Adding a simple event returns the server-assigned identifier

- **WHEN** `collection.add(calendar)` is invoked with a `Calendar` containing one `VEVENT` (summary, dtstart, dtend)
- **THEN** the Graph API receives a POST with the mapped fields
- **AND** the returned string equals the `iCalUId` present on the Graph response (which Graph assigns; the connector does not rely on the submitted UID)

### Requirement: Server-assigned event identity

Because Microsoft Graph assigns `iCalUId` server-side and ignores a client-supplied value on create, the connector SHALL treat the identifier returned by `add(Calendar)` as authoritative. The write mapping SHALL NOT depend on preserving the submitted iCal `UID`.

#### Scenario: Submitted UID is not the lookup key after add

- **WHEN** a caller adds a `VEVENT` carrying `UID:foo@example.com` and Graph assigns a different `iCalUId`
- **THEN** `add(Calendar)` returns Graph's assigned `iCalUId`
- **AND** a subsequent `get("foo@example.com")` does not resolve the event; the caller must use the returned identifier

### Requirement: Bulk read and removal operations

`MSGraphCalendarCollection` SHALL implement `export()`, `merge(Calendar)`, and `removeAll(uid...)` against the Graph API.

#### Scenario: export aggregates all events into one calendar

- **WHEN** `collection.export()` is invoked on a calendar containing several events
- **THEN** a single `net.fortuna.ical4j.model.Calendar` is returned containing one `VEVENT` per master event, each mapped via the read builder

#### Scenario: merge adds one object per UID

- **WHEN** `collection.merge(calendar)` is invoked with a `Calendar` containing multiple components across distinct UIDs
- **THEN** each distinct UID is added to the collection
- **AND** the returned `Uid[]` contains the UIDs that were merged

#### Scenario: removeAll deletes matching events and returns them

- **WHEN** `collection.removeAll(uid1, uid2)` is invoked and both UIDs resolve to events
- **THEN** the Graph API receives a delete for each resolved event id
- **AND** the returned list contains the removed events as `Calendar` objects

#### Scenario: removeAll skips unknown UIDs

- **WHEN** `collection.removeAll(uid1, unknownUid)` is invoked and `unknownUid` matches no event
- **THEN** only `uid1`'s event is deleted
- **AND** the returned list contains only the calendar(s) actually removed

### Requirement: iCal4j ↔ Graph Event field mapping

`MSGraphEventBuilder` (VEvent → Graph `Event`) and `ICalCalendarBuilder` (Graph `Event` → iCal4j `Calendar`) SHALL map the following fields in both directions. Fields not in the table SHALL be silently dropped during conversion.

| iCal4j VEvent property         | Microsoft Graph Event field                              |
| ------------------------------ | -------------------------------------------------------- |
| UID                            | iCalUId (read path; server-assigned on write)            |
| SUMMARY                        | subject                                                  |
| DESCRIPTION                    | body.content (contentType = text)                        |
| LOCATION                       | location.displayName                                     |
| DTSTART (date, all-day)        | start (date) + isAllDay = true                           |
| DTSTART (date-time, with TZID) | start.dateTime + start.timeZone                          |
| DTEND (date, all-day)          | end (date) + isAllDay = true                             |
| DTEND (date-time, with TZID)   | end.dateTime + end.timeZone                              |
| ORGANIZER                      | organizer.emailAddress.address / .name                   |
| ATTENDEE (each)                | attendees[].emailAddress / type / status.response        |
| RRULE (common patterns)        | recurrence (PatternedRecurrence)                         |
| CREATED                        | createdDateTime (read path only)                         |
| LAST-MODIFIED                  | lastModifiedDateTime (read path only)                    |

PARTSTAT ↔ Graph `responseStatus.response`:

| iCal PARTSTAT | Graph responseStatus.response |
| ------------- | ----------------------------- |
| NEEDS-ACTION  | none / notResponded           |
| ACCEPTED      | accepted                      |
| DECLINED      | declined                      |
| TENTATIVE     | tentativelyAccepted           |

#### Scenario: UID is emitted from iCalUId on read

- **WHEN** a Graph `Event` with `iCalUId = "abc@outlook.com"` is converted to an iCal4j `Calendar`
- **THEN** the resulting `VEVENT` has `UID:abc@outlook.com`

#### Scenario: Missing iCalUId yields a generated UID

- **WHEN** a Graph `Event` with no `iCalUId` is converted to an iCal4j `Calendar`
- **THEN** the resulting `VEVENT` has a generated, non-empty `UID`

#### Scenario: All-day event mapping

- **WHEN** a VEVENT with `DTSTART;VALUE=DATE:20260601` and `DTEND;VALUE=DATE:20260602` is converted to a Graph `Event`
- **THEN** the Graph `Event` has `isAllDay = true` with date-only `start`/`end`
- **AND** converting it back yields `DTSTART;VALUE=DATE:20260601` / `DTEND;VALUE=DATE:20260602` with no time component

#### Scenario: Timed event with timezone round-trip

- **WHEN** a VEVENT with `DTSTART;TZID=Australia/Melbourne:20260601T093000` is converted to a Graph `Event` and back
- **THEN** the Graph `Event` has `start.dateTime` set and `start.timeZone` representing Australia/Melbourne
- **AND** the round-tripped VEVENT has `DTSTART;TZID=Australia/Melbourne:20260601T093000`

#### Scenario: Description written as plain text

- **WHEN** a VEVENT with `DESCRIPTION:Bring your laptop` is converted to a Graph `Event`
- **THEN** the Graph `Event` `body.contentType` is `text` and `body.content` is `"Bring your laptop"`

#### Scenario: HTML body converted to plain text on read

- **WHEN** a Graph `Event` whose `body.contentType` is `html` and `body.content` is `"<p>Bring your <b>laptop</b></p>"` is converted to an iCal4j `Calendar`
- **THEN** the resulting `VEVENT` has a plain-text `DESCRIPTION` with the markup removed (e.g. `Bring your laptop`)

#### Scenario: Timed event without timezone defaults to UTC

- **WHEN** a VEVENT with `DTSTART:20260601T093000Z` (UTC, no TZID) is converted to a Graph `Event`
- **THEN** the Graph `Event` has `start.timeZone` equal to `"UTC"`

#### Scenario: PARTSTAT mapping

- **WHEN** an ATTENDEE with `PARTSTAT=TENTATIVE` is mapped to a Graph attendee
- **THEN** the Graph attendee's `status.response` is `tentativelyAccepted`
- **AND** the reverse mapping from `tentativelyAccepted` yields `PARTSTAT=TENTATIVE`

#### Scenario: Simple recurrence round-trip

- **WHEN** a VEVENT with `RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO` and a `DTSTART` is converted to a Graph `Event`
- **THEN** the Graph `Event` has a `PatternedRecurrence` with weekly pattern, interval 1, `daysOfWeek` containing Monday, and a range starting at the `DTSTART` date
- **AND** the reverse mapping reconstructs `RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO`

#### Scenario: Unmapped fields are dropped

- **WHEN** a VEVENT containing `URL`, `CLASS`, `CATEGORIES`, `RDATE`, and `EXDATE` is converted to a Graph `Event`
- **THEN** no exception is raised
- **AND** none of those properties appear on the resulting Graph `Event`

### Requirement: Honest handling of unsupported collection metadata

Where a Microsoft Graph `Calendar` resource has no corresponding field, the connector SHALL return safe, documented stub values rather than fabricated data.

#### Scenario: getDescription returns empty because Graph calendars have no description

- **WHEN** `collection.getDescription()` is invoked
- **THEN** an empty string is returned

#### Scenario: getTimeZone returns null

- **WHEN** `collection.getTimeZone()` is invoked
- **THEN** `null` is returned

#### Scenario: Listener accessors return null

- **WHEN** `store.getObjectStoreListeners()` or `collection.getObjectCollectionListeners()` is invoked
- **THEN** `null` is returned

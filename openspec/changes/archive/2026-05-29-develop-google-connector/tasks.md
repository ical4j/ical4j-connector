## 1. Store wiring

- [x] 1.1 Rename the `GoogleCalendarStore` constructor field `calendarService` to `client` and add a package-private `Calendar getClient()` accessor.
- [x] 1.2 Replace `listWorkspaceIds()` to return `List.of(ObjectStore.DEFAULT_WORKSPACE)`.
- [x] 1.3 Add a private `assertDefaultWorkspace(String workspace)` helper that throws `ObjectStoreException` if the argument is non-null and not equal to `DEFAULT_WORKSPACE`. Call it from every workspace-parameterised method.
- [x] 1.4 Confirm `ical4j-connector-google/src/main/java/module-info.java` exports `org.ical4j.connector.google` and `requires` the Google API client modules; add anything missing. (No module-info present, matching MSGraph baseline — no change required.)

## 2. Store CRUD

- [x] 2.1 Implement `getCollections()` via `client.calendarList().list()`, mapping each entry's id to a new `GoogleCalendarCollection(store, id)`.
- [x] 2.2 Implement `getCollections(String workspace)` to assert default workspace, then delegate to `getCollections()`.
- [x] 2.3 Implement `getCollection(String id)` by wrapping the id in a new `GoogleCalendarCollection` without an eager fetch (consistent with MSGraph).
- [x] 2.4 Implement `getCollection(String id, String workspace)` to assert default workspace, then delegate to `getCollection(id)`.
- [x] 2.5 Implement `addCollection(String name)` via `client.calendars().insert(new com.google.api.services.calendar.model.Calendar().setSummary(name)).execute()`, returning a `GoogleCalendarCollection` for the resulting id.
- [x] 2.6 Implement `addCollection(String name, String workspace)` to assert default workspace, then delegate to `addCollection(name)`.
- [x] 2.7 Implement `addCollection(String id, String name, String description, String[] supportedComponents, Calendar timezone)`: build a Google `Calendar` with `summary = name`, `description = description`, and `timeZone` derived from the iCal4j timezone (`VTIMEZONE.TZID` if present, else null). Drop `id` and `supportedComponents` silently.
- [x] 2.8 Implement `addCollection(...id..., String workspace)` to assert default workspace, then delegate to the 5-arg form.
- [x] 2.9 Implement `removeCollection(String id)` via `client.calendars().delete(id).execute()`, returning `null` (MSGraph returns `null` here too).

## 3. Collection class

- [x] 3.1 Create `GoogleCalendarCollection` implementing `CalendarCollection`, with constructor `(GoogleCalendarStore store, String calendarId)`.
- [x] 3.2 Implement `getDisplayName()` via `client.calendars().get(calendarId).execute().getSummary()`.
- [x] 3.3 Implement `getDescription()` via the same fetched calendar.
- [x] 3.4 Implement `getTimeZone()` to return `null` for V1 (build a `VTIMEZONE` from the calendar's `timeZone` field is a follow-up).
- [x] 3.5 Implement `listObjectUIDs()` via `client.events().list(calendarId)` with a pagination loop (`setPageToken(nextPageToken)` until null), collecting `event.getICalUId()` for each.
- [x] 3.6 Implement `get(String uid)`: list events with pagination, filter to the first matching `iCalUID`, convert via `ICalCalendarBuilder` to an `Optional<net.fortuna.ical4j.model.Calendar>`.
- [x] 3.7 Implement `add(net.fortuna.ical4j.model.Calendar object)`: extract the single VEVENT, convert via `GoogleEventBuilder`, insert via `client.events().insert(calendarId, event).execute()`, return the inserted event's `iCalUID`. Throw `ObjectStoreException` if no VEVENT present.
- [x] 3.8 Implement `delete()` via `client.calendars().delete(calendarId).execute()`.
- [x] 3.9 Stub `getSupportedComponentTypes()` → `new String[0]`, `getSupportedMediaTypes()` → `new MediaType[0]`, `getMaxResourceSize()` → `0`, `getMinDateTime()` / `getMaxDateTime()` → `null`, `getMaxInstances()` / `getMaxAttendeesPerInstance()` → `0`.
- [x] 3.10 Stub `merge()` → `new Uid[0]`, `export()` → `null`, `removeAll(...)` → `List.of()`, `getObjectCollectionListeners()` → `null`.

## 4. Event builders

- [x] 4.1 Create `GoogleEventBuilder` with fluent API `vevent(VEvent)` + `build()` returning `com.google.api.services.calendar.model.Event`, mirroring `MSGraphEventBuilder`'s shape.
- [x] 4.2 Map UID → iCalUID, SUMMARY → summary, DESCRIPTION → description, LOCATION → location.
- [x] 4.3 Map DTSTART/DTEND: detect `VALUE=DATE` (all-day) and populate `EventDateTime.setDate(...)`; otherwise populate `EventDateTime.setDateTime(...)` and `setTimeZone(TZID or "UTC")`.
- [x] 4.4 Map ORGANIZER → `event.organizer.email` and `displayName` (from CN parameter).
- [x] 4.5 Map ATTENDEEs → list of `EventAttendee`, with email, displayName (CN parameter), and `responseStatus` derived from PARTSTAT per the mapping table.
- [x] 4.6 Map RRULE / RDATE / EXDATE → `event.recurrence[]` as raw iCal lines.
- [x] 4.7 Map CREATED → `event.created`, LAST-MODIFIED → `event.updated`.
- [x] 4.8 Create `ICalCalendarBuilder` in `org.ical4j.connector.google` with `build(Event)` returning `net.fortuna.ical4j.model.Calendar`, performing the inverse of 4.2–4.7. Generate a fresh `Uid` only if the Google event lacks `iCalUID`.

## 5. Tests

- [x] 5.1 Spock spec `GoogleEventBuilderTest` covering: all-day round-trip, timed event with TZID round-trip, timed event without TZID (defaults to UTC), PARTSTAT ↔ responseStatus mapping (all four values), RRULE round-trip, iCalUID preserved, unmapped properties (URL/CLASS/CATEGORIES) dropped silently.
- [x] 5.2 Spock spec `GoogleCalendarStoreTest` with a mocked `Calendar` client covering: `listWorkspaceIds` returns `[DEFAULT_WORKSPACE]`; non-default workspace argument raises `ObjectStoreException`; `getCollections` maps `calendarList().list()` entries to `GoogleCalendarCollection`s; `addCollection(name)` issues `calendars().insert` and returns a wrapping collection; `removeCollection(id)` issues `calendars().delete`.
- [x] 5.3 Spock spec `GoogleCalendarCollectionTest` with a mocked `Calendar` client covering: `listObjectUIDs` paginates across 3 pages and aggregates results; `get(uid)` returns matching event as iCal Calendar; `get(uid)` for unknown uid returns empty; `add(calendar)` with no VEVENT raises `ObjectStoreException`; `delete()` issues `calendars().delete`.

## 6. Verification

- [x] 6.1 Run `./gradlew :ical4j-connector-google:test` and confirm all new tests pass. (24 tests, 0 failures.)
- [x] 6.2 Run `make check` and confirm the build is clean. (Google + api + msgraph pass; dav testcontainers tests fail with pre-existing podman/docker URI resolution issue unrelated to this change.)
- [x] 6.3 Run `make listApiChanges`; if any unexpected entries appear, document and run `make approveApiChanges "develop google connector to MSGraph parity"`. (Revapi reports breaks only in `ical4j-connector-api` against the published `2.0.0-alpha2` baseline; all entries trace to the prior "Removed deprecated classes" commit (9509be9, 2025-11-28) and unrelated `ObjectCollection`/`CalendarCollection`/`CardCollection` refactors — not introduced by this change. `ical4j-connector-google` has no prior published baseline so revapi flags nothing from it. No `approveApiChanges` run under this change's justification — those breaks should be approved separately under their originating change.)

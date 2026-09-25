## Why

The `ical4j-connector-google` module exists as a stub: `GoogleCalendarStore` implements `ObjectStore<CalendarCollection>` but every method except `listWorkspaceIds()` returns `null`/`false`/empty. The sibling `ical4j-connector-msgraph` module has reached a working baseline (real CRUD on the store, basic read/add on the collection, iCal ↔ provider-event mapping). Google is the next obvious provider to bring to the same level so the library's promise of "unified API across calendar backends" actually delivers on Google Calendar.

## What Changes

- Flesh out `GoogleCalendarStore` CRUD methods (`addCollection`, `removeCollection`, `getCollection(s)`) by mapping to the Google Calendar API (`calendars()` and `calendarList()` endpoints).
- Introduce `GoogleCalendarCollection` implementing `CalendarCollection`, covering display name/description, `listObjectUIDs`, `get(uid)`, `add(Calendar)`, and `delete()` — the same surface MSGraph currently fills.
- Introduce `GoogleEventBuilder` (iCal4j `VEvent` → Google `Event`) and `ICalCalendarBuilder` (Google `Event` → iCal4j `Calendar`), mirroring the MSGraph package's builder pair.
- Drop Google's notion of "workspace" from the store: `listWorkspaceIds()` returns `[DEFAULT_WORKSPACE]`, and workspace-parameterised methods ignore the workspace argument. Google Calendar has no calendar-group concept analogous to Microsoft Graph's, so honest stub > misleading mapping. **BREAKING** vs. the current stub: `listWorkspaceIds()` no longer returns calendar IDs.
- Authentication stays external to the store: the caller constructs an authenticated `com.google.api.services.calendar.Calendar` client and hands it to the constructor. `connect()` / `disconnect()` remain no-ops, matching the MSGraph pattern.
- Out of scope (parity-only): `merge`, `export`, `removeAll`, listener support, and the empty `service/` subpackage (`GoogleCalendarService`, `GoogleKeepService`, `GoogleTasksService`) — these are stubs in MSGraph too and are left for follow-up work.

## Capabilities

### New Capabilities
- `google-calendar-connector`: Read/write access to a user's Google Calendar through the iCal4j `ObjectStore` / `CalendarCollection` abstractions, including event ↔ iCal mapping for the common cases (summary, description, location, start/end with timezone or all-day, organizer, attendees, recurrence, created/updated, iCalUID).

### Modified Capabilities
<!-- None — no existing specs in openspec/specs/. -->

## Impact

- **Module**: `ical4j-connector-google`
  - New: `GoogleCalendarCollection`, `GoogleEventBuilder`, `ICalCalendarBuilder` (in `org.ical4j.connector.google`).
  - Modified: `GoogleCalendarStore` (constructor field renamed `calendarService` → `client` for clarity; CRUD methods implemented).
  - Unchanged: `service/` subpackage stays as-is.
- **Module-info**: `org.ical4j.connector.google` needs to export `org.ical4j.connector.google` (verify current state) and requires on Google API client modules.
- **Dependencies**: Already present (`google-api-calendar`, `google-api-client`, `google-oauth-client-jetty`). No new entries in `gradle/libs.versions.toml`.
- **API surface**: No breaking changes to `ical4j-connector-api`. The breaking change in `listWorkspaceIds()` semantics is internal to the Google module and only affects callers of the stub (none expected — current implementation throws on the happy path).
- **Tests**: New Spock specs under `ical4j-connector-google/src/test/groovy/` for builder round-trips and store/collection behaviour. Integration tests against the live Google API are out of scope; unit tests use mocked `Calendar` clients.

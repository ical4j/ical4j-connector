## Why

The `ical4j-connector-msgraph` module was the reference shape for the recently-completed Google connector, but it never actually reached the parity bar it set. The field mappers are placeholders: `MSGraphEventBuilder` writes only `iCalUId` + `subject`, and `ICalCalendarBuilder` reads only `subject` → `SUMMARY` (it does not even emit a `UID`). As a result the connector can create near-empty events and read back identity-less, time-less events — a `get(uid)` round-trip is effectively broken. Event listing is unpaginated and inconsistent (`listObjectUIDs()` reads `calendarView()`, `get(uid)` reads `events()`), and the `CalendarCollection` contract (`merge`, `export`, `removeAll`) is stubbed. This change brings MSGraph to genuine parity with — and in a few places past — the Google connector, so the library's "unified API across calendar backends" promise holds for Microsoft 365 calendars.

## What Changes

- **Flesh out `MSGraphEventBuilder` (VEvent → Graph `Event`):** subject, body/description, location, start/end (timed + all-day via `isAllDay`), organizer, attendees (with PARTSTAT → `responseStatus`), recurrence (RRULE → `PatternedRecurrence`, common cases), created/last-modified. `iCalUId` handling acknowledges that Graph assigns it server-side (see design).
- **Flesh out `ICalCalendarBuilder` (Graph `Event` → iCal4j `Calendar`):** the inverse mapping, and crucially emit a `UID` (from `iCalUId`, generating one only if absent). This is the single biggest correctness fix.
- **Fix event access:** make `listObjectUIDs()` and `get(uid)` consistent — both go through `events()` (master events, one per UID), with transparent pagination over `@odata.nextLink`. `get(uid)` filters server-side by `iCalUId` where possible.
- **Complete the `CalendarCollection` contract:** implement `merge(Calendar)` (decompose per-UID and add each), `export()` (aggregate all events into one `Calendar`), and `removeAll(uid...)` (resolve event ids by `iCalUId`, delete, return the removed calendars). These go *past* the Google connector, which left them stubbed — MSGraph can implement them cleanly on the Graph API.
- **Complete the store creation overloads:** implement `addCollection(id, name, description, supportedComponents, timezone[, workspace])` by mapping to a Graph `Calendar` (name only; unsupported inputs documented and dropped).
- **Honest impedance caveats:** Graph `Calendar` resources expose no `description` field, so `getDescription()` returns `""`; `getTimeZone()` returns `null`. Both documented rather than faked.
- Authentication stays caller-side: the caller builds an authenticated `GraphServiceClient` and injects it. `connect()` / `disconnect()` remain no-ops, matching the Google and MSGraph baseline.

## Capabilities

### New Capabilities
- `msgraph-calendar-connector`: Read/write access to a user's Microsoft 365 calendars (including calendar groups as workspaces) through the iCal4j `ObjectStore` / `CalendarCollection` abstractions, including `Event` ↔ iCal mapping for the common cases (summary, description/body, location, start/end with timezone or all-day, organizer, attendees, recurrence, created/updated, iCalUID).

### Modified Capabilities
<!-- None — no existing msgraph spec in openspec/specs/. -->

## Impact

- **Module**: `ical4j-connector-msgraph`
  - Modified: `MSGraphEventBuilder` (full write mapping), `ICalCalendarBuilder` (full read mapping incl. UID), `MSGraphCalendarCollection` (pagination, `merge`/`export`/`removeAll`, consistent listing), `MSGraphCalendarStore` (creation overloads).
  - Unchanged: the empty `service/` subpackage (`CalendarService`, `ToDoService`, `PlannerService`, `OneNoteService`) — these are a separate, net-new scope (VTODO/VJOURNAL across To Do / Planner / OneNote) tracked as future work, not part of this change.
- **Dependencies**: `microsoft-graph` / `azure-identity` already present. A Windows↔IANA timezone mapping may require `com.microsoft.graph` built-ins or a small internal table — no new top-level dependency expected (confirm during task 4).
- **API surface**: No changes to `ical4j-connector-api`. All changes are internal to the MSGraph module.
- **Out of scope (deferred to follow-up)**: the four empty `service/` classes; recurrence *exceptions* (Graph models edited single occurrences as separate objects via `seriesMasterId` — V1 maps the master `PatternedRecurrence` only); `RDATE`/`EXDATE` (no direct Graph equivalent); listener support (`getObjectStoreListeners` / `getObjectCollectionListeners` stay `null`, matching Google); the OAuth flow.
- **Tests**: New Spock specs under `ical4j-connector-msgraph/src/test/groovy/` for builder round-trips and store/collection behaviour against a mocked `GraphServiceClient`. Live Graph integration tests are out of scope.

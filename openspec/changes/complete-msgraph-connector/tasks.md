## 1. Read builder — `ICalCalendarBuilder` (Graph `Event` → iCal4j `Calendar`)

- [x] 1.1 Emit `UID` from `event.getICalUId()`, generating a random `Uid` only when Graph returns none. (Highest-priority correctness fix — currently no UID is emitted at all.)
- [x] 1.2 Map `subject` → `SUMMARY`, `body.content` → `DESCRIPTION` (use content directly when `contentType = text`; convert HTML → plain text best-effort when `contentType = html`), `location.displayName` → `LOCATION` (null-safe on each Graph wrapper object).
- [x] 1.3 Map `start`/`end` (`DateTimeTimeZone`): when `isAllDay` is true emit `DTSTART;VALUE=DATE`/`DTEND;VALUE=DATE` (date only); otherwise parse `dateTime` + `timeZone` into a `ZonedDateTime` (Windows→IANA fallback, default UTC) and emit `DTSTART`/`DTEND` with `TZID`.
- [x] 1.4 Map `organizer.emailAddress` → `ORGANIZER` (address as `mailto:`, name as `CN`).
- [x] 1.5 Map each `attendees[]` → `ATTENDEE` (email, `CN`, `PARTSTAT` from `status.response` per the mapping table).
- [x] 1.6 Map `recurrence` (`PatternedRecurrence`) → `RRULE` for the common pattern/range cases (Decision 4). Skip patterns Graph expresses but iCal cannot represent simply.
- [x] 1.7 Map `createdDateTime` → `CREATED`, `lastModifiedDateTime` → `LAST-MODIFIED`.
- [x] 1.8 Replace the current single-property `build(Event)` body; keep the existing `ContentHandler`/`TimeZoneRegistry` constructor seam intact.

## 2. Write builder — `MSGraphEventBuilder` (VEvent → Graph `Event`)

- [x] 2.1 Map `SUMMARY` → `subject`, `DESCRIPTION` → `body` (`ItemBody` with `contentType = text` — iCal DESCRIPTION is plain text), `LOCATION` → `location.displayName`.
- [x] 2.2 Map `DTSTART`/`DTEND`: detect `VALUE=DATE` (all-day) → set date-only `start`/`end` + `isAllDay = true`; otherwise set `DateTimeTimeZone.dateTime` + IANA `timeZone` derived from the `TZID` parameter (default UTC).
- [x] 2.3 Map `ORGANIZER` → `organizer` (`Recipient` with `emailAddress.address`/`.name`).
- [x] 2.4 Map `ATTENDEE`s → `attendees[]` (`Attendee` with `emailAddress`, default `type = required`, `status.response` from `PARTSTAT` per the mapping table).
- [x] 2.5 Map simple `RRULE` → `PatternedRecurrence` (`RecurrencePattern` + `RecurrenceRange` with `startDate` from `DTSTART`) per Decision 4. Do not map `RDATE`/`EXDATE`.
- [x] 2.6 Do **not** depend on setting `iCalUId` for identity (server-assigned, Decision 2); leave it unset and rely on the create response. Remove the current `setICalUId(...)` reliance.
- [x] 2.7 Keep the fluent `vevent(VEvent)` + `build()` shape; null-safe on every optional property.

## 3. Collection — `MSGraphCalendarCollection`

- [x] 3.1 Add a private paginated event lister that follows `@odata.nextLink` (Graph SDK `PageIterator` or next-link re-issue) over `me().calendars().byCalendarId(calendarId).events()`, group-aware (calendarGroup path when `calendarGroupId != null`).
- [x] 3.2 Reimplement `listObjectUIDs()` to use the paginated lister (Decision 3) — replacing the current `getCalendarView()` call — collecting each event's `iCalUId`.
- [x] 3.3 Reimplement `get(uid)`: filter server-side (`$filter=iCalUId eq '<uid>'`) with a paged-scan fallback; convert the match via `ICalCalendarBuilder` to `Optional<Calendar>`; empty when absent.
- [x] 3.4 Keep `add(Calendar)` POSTing the mapped event, but return the `iCalUId` from the Graph response (it is server-assigned).
- [x] 3.5 Implement `removeAll(uid...)`: for each UID resolve the Graph event id by `iCalUId`, capture it as a `Calendar` via the read builder, delete it (group-aware), and return the list of removed calendars; skip UIDs not found (Open Question 2).
- [x] 3.6 Implement `export()`: page all master events, convert each, aggregate the `VEVENT`s into a single `Calendar`.
- [x] 3.7 Implement `merge(Calendar)`: decompose the calendar into per-`UID` objects, `add()` each, return the resulting `Uid[]`.
- [x] 3.8 Leave honest stubs (Decision 7): `getDescription()` → `""`, `getTimeZone()` → `null`, and the `getMax*`/`getMin*`/`getSupported*` metadata accessors at their current safe defaults. `getObjectCollectionListeners()` → `null`.
- [x] 3.9 Confirm `getDisplayName()` still resolves via `getCalendar().getName()` for both grouped and ungrouped collections.

## 4. Timezone + recurrence helpers

- [x] 4.1 Add a Windows↔IANA timezone helper backed by a CLDR-derived resource bundled in the module (e.g. generated `windows-iana.properties`, loaded once); default unknown zone names → UTC with a logged warning. No new runtime dependency (Decision 5).
- [x] 4.2 Map Graph `DateTimeTimeZone` → `Temporal` using the event's own returned zone (no `Prefer: outlook.timezone` header — preserve the original zone for faithful round-trips), translating Windows zone names to IANA via the 4.1 helper (Decision 5).
- [x] 4.3 Add a recurrence translation helper pair (`RRULE` ↔ `PatternedRecurrence`) covering the cases in Decision 4, shared by both builders.

## 5. Store — `MSGraphCalendarStore`

- [x] 5.1 Implement `addCollection(id, name, description, supportedComponents, timezone)`: build a Graph `Calendar` with `name = name`; drop `id`, `description`, `supportedComponents`, `timezone` (documented, Decision 7); return a wrapping `MSGraphCalendarCollection`.
- [x] 5.2 Implement `addCollection(id, name, description, supportedComponents, timezone, workspace)`: create within the calendar group identified by `workspace`, otherwise as 5.1.
- [x] 5.3 Leave `connect`/`disconnect`/`isConnected` as no-op stubs and `getObjectStoreListeners()` → `null` (matches Google + baseline). Confirm no other store method still returns a placeholder.

## 6. Tests

- [x] 6.1 Spock spec `MSGraphEventBuilderTest`: all-day round-trip (`isAllDay` + date-only), timed-with-TZID round-trip, timed-without-TZID → UTC, PARTSTAT ↔ `responseStatus` (all four), simple `RRULE` round-trip (DAILY/WEEKLY/MONTHLY + INTERVAL + COUNT/UNTIL), organizer + attendees mapping, unmapped properties (URL/CLASS/CATEGORIES) dropped silently.
- [x] 6.2 Spock spec `ICalCalendarBuilderTest`: `UID` emitted from `iCalUId`; random `Uid` generated when absent; body/location/organizer/attendees mapped; `text` body used directly and `html` body converted to plain text; Windows zone name → IANA `TZID`; all-day vs timed start/end; created/last-modified mapped.
- [x] 6.3 Spock spec `MSGraphCalendarCollectionTest` (mocked `GraphServiceClient`): `listObjectUIDs` aggregates across ≥3 pages; `get(uid)` returns the filtered match and empty for unknown; `add` returns the response `iCalUId`; `removeAll` resolves-and-deletes and returns removed calendars (and skips missing UIDs); `export` aggregates all events; `merge` adds one per UID; group-aware paths exercised.
- [x] 6.4 Spock spec `MSGraphCalendarStoreTest` (mocked client): `addCollection(id,…tz)` posts a `Calendar` with `name` only; workspace overload targets the calendar group; `getCollections(±workspace)` mapping unchanged.

## 7. Verification

- [x] 7.1 Run `./gradlew :ical4j-connector-msgraph:test` and confirm all new tests pass.
- [x] 7.2 Run `make check` and confirm the build is clean (note any pre-existing, unrelated failures). (`:ical4j-connector-msgraph:check` passes — main compiles, 35 tests green. Two pre-existing, unrelated failures in the wider build, both already noted by `develop-google-connector`: (a) `:ical4j-connector-api:revapi` breaks on `CalendarCollection.merge` (void→`Uid[]`), `removeCalendar`/`removeCollection` exception signatures — from prior api refactors; this change touches no api code; (b) `:ical4j-connector-dav:test` Baikal/Radicale `initializationError` — testcontainers require a running podman/docker.)
- [x] 7.3 Run `make listApiChanges`; if unexpected entries appear, document them and run `make approveApiChanges "complete msgraph connector to Google parity"` only for changes genuinely introduced here. (`listApiChanges` reports no entries originating from `ical4j-connector-msgraph` — the module has no published baseline and this change makes no api-module changes. All revapi breaks trace to `ical4j-connector-api` from prior commits, so `approveApiChanges` was NOT run under this change's justification.)

## 1. Register CalDAV report types

- [x] 1.1 In `ical4j-connector-dav/src/main/java/org/ical4j/connector/dav/property/CalDavPropertyName.java`, register `CALENDAR_MULTIGET` as a new `ReportType` (XML local name `calendar-multiget`, NAMESPACE = CalDAV). Note: `FREEBUSY_QUERY` was already registered at line 344, so no new registration needed for free-busy-query.

## 2. CalendarMultiget request class

- [x] 2.1 Replace the empty `CalendarMultiget` stub with a `ReportInfo` + `XmlSupport` subclass: constructor `CalendarMultiget(List<String> hrefs)` calling `super(CalDavPropertyName.CALENDAR_MULTIGET, 0)`; override `toXml(Document)` to emit `<C:calendar-multiget>` containing `<D:prop><D:getetag/><C:calendar-data/></D:prop>` (note: `<D:getetag/>` in DAV namespace per RFC 4791 §7.9, not CalDAV) followed by one `<D:href>{href}</D:href>` per element of `hrefs`.

## 3. Wire multiget into CalDavCalendarCollection

- [x] 3.1 Replace `getObjectsByMultiget(ArrayList<URI>, Element)` returning `Calendar[0]` with `List<Calendar> getObjectsByMultiget(List<String> hrefs)`. Empty-list short-circuit; otherwise delegate to `report(getPath(), new CalendarMultiget(hrefs), new GetCalendarData())`. Updated throws to `IOException, ParserConfigurationException` only.

## 4. FreeBusyQuery request class

- [x] 4.1 Replace the empty `FreeBusyQuery` stub with a `ReportInfo` + `XmlSupport` subclass: constructor `FreeBusyQuery(Instant start, Instant end)` calling `super(CalDavPropertyName.FREEBUSY_QUERY, 0)`; override `toXml(Document)` to emit `<C:free-busy-query>` containing a single `<C:time-range start="..." end="..."/>` element. Uses a `DateTimeFormatter("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)` for the bound values per RFC 4791 §9.9.

## 5. Wire freebusy-query into CalDavCalendarCollection

- [x] 5.1 Confirmed `GetFreeBusyData` returns `List<ScheduleResponse>` (used by the scheduling-outbox flow in `findFreeBusyInfoForAttendees`), not a `Calendar`. Built a new lightweight `GetFreeBusyCalendar` handler that parses the response body via `CalendarBuilder` and returns a single `Calendar`.
- [x] 5.2 Replaced `doFreeBusyQuery()` (no-arg, `Calendar[0]`) with `Calendar doFreeBusyQuery(Instant start, Instant end)` that calls `report(getPath(), new FreeBusyQuery(start, end), new GetFreeBusyCalendar())`.

## 6. export() on CalDavCalendarCollection

- [x] 6.1 Replaced `UnsupportedOperationException` body with iteration over `getComponentsByType(Component.VEVENT)` accumulating via `aggregate.merge(c)`.

## 7. export() on CardDavCollection

- [x] 7.1 Replaced `UnsupportedOperationException` body with iteration over `getAll()` accumulating via `aggregate.merge(c)`; wrapped `ObjectStoreException` in `RuntimeException`.

## 8. Delete dead store-level merge methods

- [x] 8.1 Deleted `CalDavCalendarStore.merge(String, CalendarCollection)` and the adjacent commented-out `replace` block.
- [x] 8.2 Deleted `CardDavStore.merge(String, CalendarCollection)` and the adjacent commented-out `replace` block.

## 9. Integration tests for CalDAV features

- [x] 9.1 Added `'test getObjectsByMultiget returns requested events'`: 3 events added, multiget 2, asserts result size 2 and UIDs match.
- [x] 9.2 Added `'test getObjectsByMultiget skips missing hrefs'`: `@IgnoreIf` Baikal (Baikal returns 403 for the whole multiget when any href is missing, rather than per-href status; server-specific behaviour). Asserts Radicale per-href-status behaviour.
- [x] 9.3 Added `'test getObjectsByMultiget empty input returns empty list'`: confirms short-circuit, no HTTP request issued.
- [x] 9.4 Added `'test doFreeBusyQuery returns busy interval for covering event'`: `@IgnoreIf` Radicale (Radicale's `free-busy-query` implementation does not populate FREEBUSY intervals from collection events — server limitation). Asserts on Baikal that at least one FREEBUSY interval is reported. Note: the exact reported interval is server-tz-dependent on Baikal (configured timezone applied to event times), so the test verifies structural presence rather than exact-value equality.
- [x] 9.5 Added `'test doFreeBusyQuery returns empty VFREEBUSY for range with no events'`: `@IgnoreIf` Radicale (returns empty body instead of an empty VFREEBUSY component). Asserts Baikal returns a VFREEBUSY with no FREEBUSY intervals.
- [x] 9.6 Added `'test export aggregates added events'`: 3 events added, export, assert 3 VEVENTs with matching UIDs.
- [x] 9.7 Added `'test export empty collection returns empty calendar'`: assert non-null Calendar with zero VEVENT components.

## 10. Integration tests for CardDAV features

- [x] 10.1 Added `'test export aggregates added vcards'`: 3 vCards added, assert export returns non-null VCard.
- [x] 10.2 Added `'test export empty collection returns empty vcard'`: assert non-null VCard.

## 11. Verification

- [x] 11.1 Run integration tests for the 4 store classes: 48 tests, 5 skipped (2 pre-existing + 3 new server-specific), 0 failures. Stable across runs after the multiget XML namespace fix (`<D:getetag/>` not `<C:getetag/>`) and the href-prefix fix (include repository prefix from `getRepositoryPath()` when building multiget href values).
- [x] 11.2 Run full DAV module test suite: 88 tests, 25 skipped, 0 failures on a clean run. Intermittent Radicale container startup flakes (`ContainerLaunchException` from testcontainers) are infrastructure-side, not code-side; retry typically resolves.
- [x] 11.3 Run `./gradlew :ical4j-connector-api:check :ical4j-connector-dav:check`: both modules clean. (Full `make check` still blocked by unrelated in-flight `:ical4j-connector-google:compileJava` failure — out of scope.)

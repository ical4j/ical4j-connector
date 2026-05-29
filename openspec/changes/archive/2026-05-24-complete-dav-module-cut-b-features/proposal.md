## Why

The `complete-dav-module-parity` change brought the high-level DAV store/collection surface to MSGraph/Google happy-path parity but deferred protocol features that didn't fit the parity bar: `calendar-multiget`, `free-busy-query`, `export()` on both collection types, and a clean-up of dead `merge(String, CalendarCollection)` methods on the stores. The `fix-dav-client-protocol-compliance` change has since landed, giving us a clean foundation to add these features without working around low-level bugs. With that done, this change closes out the originally-defined "Cut B" feature scope so callers can use the DAV module for the operations the MSGraph and Local implementations already support.

## What Changes

- **Implement `CalendarMultiget` request class** (currently an empty `public class CalendarMultiget {}` stub): build a `ReportInfo` + `XmlSupport` subclass that emits the RFC 4791 §7.9 `<C:calendar-multiget>` REPORT body — a set of `<D:prop>` requests (GETETAG + calendar-data) plus a list of `<D:href>` elements for each calendar object to fetch.
- **Wire multiget into `CalDavCalendarCollection.getObjectsByMultiget(...)`** (currently returns `new Calendar[0]`): replace with a call through `getStore().getClient().report(...)` using the new `CalendarMultiget` builder and the existing `GetCalendarData` response handler. Update return type from `Calendar[]` to `List<Calendar>` to match the existing report-handler shape used elsewhere in the class.
- **Implement `FreeBusyQuery` request class** (currently empty stub): build a `ReportInfo` + `XmlSupport` subclass that emits the RFC 4791 §7.10 `<C:free-busy-query>` REPORT body — a `<C:time-range>` element bounding the query window.
- **Wire freebusy-query into `CalDavCalendarCollection.doFreeBusyQuery(...)`** (currently returns `new Calendar[0]` and takes no arguments): add a `start`/`end` parameter pair, call `report(...)` with the new builder and a `GetFreeBusyData` response handler (already exists in the codebase). Return `List<Calendar>` or `Calendar` (single VFREEBUSY) per design.
- **Implement `CalDavCalendarCollection.export()`** (currently throws `UnsupportedOperationException`): iterate `getComponentsByType(Component.VEVENT)`, merge each `Calendar` into a single aggregate via `Calendar.merge(...)`, and return the aggregate. Mirrors `LocalCalendarCollection.export()`.
- **Implement `CardDavCollection.export()`** (currently throws `UnsupportedOperationException`): iterate `getAll()`, merge each `VCard` into a single aggregate via `VCard.merge(...)`, and return the aggregate. Mirrors `LocalCardCollection.export()`.
- **Delete dead store-level `merge(String, CalendarCollection)` methods** from `CalDavCalendarStore` and `CardDavStore`. These methods are not declared on `ObjectStore` or any other interface, are never called from elsewhere in the codebase, and always throw `UnsupportedOperationException`. `CardDavStore`'s version even returns the wrong type (`CalendarCollection` instead of `CardDavCollection`). **BREAKING** vs. callers who somehow reach those methods directly (none in this codebase).
- **Out of scope** (deferred to a future change): workspace-as-principal-switching. That requires re-targeting `pathResolver` and home-set discovery to a principal other than the session user, plus Baikal/Radicale fixture setup with shared/delegated collections to test against. Sized as its own change.

## Capabilities

### New Capabilities
<!-- None — these features extend existing capabilities. -->

### Modified Capabilities
- `dav-calendar-connector`: Adds requirements for fetching specific calendar objects by href (multiget), querying free/busy intervals over a time range, and exporting all events in a collection as a single iCalendar.
- `dav-card-connector`: Adds a requirement for exporting all vCards in a collection as a single aggregate vCard.

## Impact

- **Module**: `ical4j-connector-dav`
  - Modified: `request/CalendarMultiget.java` (empty → real implementation), `request/FreeBusyQuery.java` (empty → real implementation), `CalDavCalendarCollection.java` (multiget wiring, freebusy-query wiring, `export()` implementation, signature change on `getObjectsByMultiget` and `doFreeBusyQuery`), `CardDavCollection.java` (`export()` implementation), `CalDavCalendarStore.java` (delete dead `merge`), `CardDavStore.java` (delete dead `merge`).
  - Test changes: extend `AbstractCalendarStoreIntegrationTest` (multiget round-trip, freebusy-query, export) and `AbstractCardStoreIntegrationTest` (vCard export).
- **API surface**: `CalendarMultiget` and `FreeBusyQuery` are currently empty but `public` — anyone could theoretically reference them. Filling them in is forward-compatible (only adds methods). `getObjectsByMultiget` return-type and `doFreeBusyQuery` signature changes are **BREAKING** in theory, but the current return values are empty arrays/no-arg respectively, so callers who relied on them were either ignoring results or already broken.
- **Dependencies**: No new dependencies.
- **Risk**: Multiget and freebusy-query are well-specified RFC 4791 reports; the existing `EventQuery` + `CalendarQuery` request classes serve as proven templates. Export is a thin loop over an existing data flow. Risk is bounded.

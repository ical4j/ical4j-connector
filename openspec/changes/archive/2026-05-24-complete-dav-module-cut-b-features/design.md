## Context

After the `complete-dav-module-parity` and `fix-dav-client-protocol-compliance` changes, the DAV module has a working high-level CalDav/CardDav store-and-collection API on top of a now-RFC-compliant low-level client. The remaining originally-defined Cut B scope splits into two groups:

- **Protocol features** that close out CalDAV operations callers already use against MSGraph / Local: `calendar-multiget`, `free-busy-query`, and `export()` on both collection types. These are the focus of this change.
- **Workspace-as-principal-switching**, which is a larger semantic change requiring delegation fixture setup against Baikal/Radicale. Deferred to its own future change.

Two empty stub classes — `CalendarMultiget` and `FreeBusyQuery` — already exist in `request/`, suggesting the original author intended these features but never built them. The existing `EventQuery` (`calendar-query` REPORT) and `CalendarQuery` (CalDavCalendarCollection's filter helper) serve as proven templates for how to build new `ReportInfo` + `XmlSupport` subclasses in this codebase. `GetCalendarData` and `GetFreeBusyData` response handlers also already exist and can be reused as-is.

The `export()` implementations on both collection types are tiny — copy the `LocalCalendarCollection.export()` / `LocalCardCollection.export()` pattern, swap the file-iteration for `getAll()` / `getComponentsByType(VEVENT)`. The iCal4j `Calendar.merge` and `VCard.merge` instance methods do the aggregation work.

The dead `merge(String, CalendarCollection)` methods on both stores are a tidy-up: they're not on the `ObjectStore` interface, never called, always throw, and `CardDavStore`'s version has the wrong return type. Deleting them improves the API surface without breaking anything reachable from the codebase.

## Goals / Non-Goals

**Goals:**
- `CalDavCalendarCollection` supports `calendar-multiget` for fetching a list of named calendar objects in a single request.
- `CalDavCalendarCollection` supports `free-busy-query` for computing free/busy intervals over a time range, returning a synthesized VFREEBUSY-bearing `Calendar` per RFC 4791 §7.10.
- `CalDavCalendarCollection.export()` returns a single iCalendar aggregating every event in the collection (parity with `LocalCalendarCollection`).
- `CardDavCollection.export()` returns a single vCard aggregating every card in the collection (parity with `LocalCardCollection`).
- Dead store-level `merge(String, CalendarCollection)` methods removed.
- All new features exercised by integration tests against Baikal + Radicale.

**Non-Goals:**
- Workspace-as-principal-switching — separate future change.
- Re-implementing `findFreeBusyInfoForAttendees` at the store level, which uses a different (`MAILTO:` recipient via scheduling outbox) flow per RFC 6638 §3.2.3. That code path remains untouched.
- Adding `expand` or `limit-recurrence-set` filters to multiget/freebusy. Out of scope; only the basic REPORT forms.
- Replacing the existing `getObjectsByMultiget(ArrayList<URI>, Element calData)` API verbatim. Its `calData` parameter is unused and its `ArrayList<URI>` parameter type is unidiomatic; the new method will take `List<String> hrefs` (matching the wire shape) and use the standard etag+calendar-data request.
- Unit tests for the new request classes in isolation. The integration tests already exercise them end-to-end.

## Decisions

### Decision 1: `CalendarMultiget` as a `ReportInfo + XmlSupport` subclass, hrefs in constructor

**Choice**: New class:
```java
public class CalendarMultiget extends ReportInfo implements XmlSupport {
    private final List<String> hrefs;
    public CalendarMultiget(List<String> hrefs) {
        super(CalDavPropertyName.CALENDAR_MULTIGET, 0);
        this.hrefs = hrefs;
    }
    @Override
    public Element toXml(Document document) {
        // <C:calendar-multiget>
        //   <D:prop><D:getetag/><C:calendar-data/></D:prop>
        //   <D:href>{href}</D:href> ...
        // </C:calendar-multiget>
        ...
    }
}
```

Need to register `CALENDAR_MULTIGET` as a `ReportType` in `CalDavPropertyName` (currently `CALENDAR_QUERY` is registered; `CALENDAR_MULTIGET` is not).

**Why**: Mirrors the shape of `EventQuery` (extends `ReportInfo`, implements `XmlSupport`). The hrefs are passed in the constructor because the report is per-request immutable. Depth is 0 (multiget operates on the collection itself; the response contains one `<D:response>` per requested href).

**Alternatives considered**:
- *Fluent builder pattern (`new CalendarMultiget().withHref(...).withHref(...)`)*: nicer for many-href callers but adds API surface. The wrapper-`new CalendarMultiget(List.of(href1, href2))`-on-each-call form is fine.

### Decision 2: `getObjectsByMultiget` signature changes to `List<Calendar> getObjectsByMultiget(List<String> hrefs)`

**Choice**: Replace the current
```java
public Calendar[] getObjectsByMultiget(ArrayList<URI> hrefs, Element calData)
        throws IOException, DavException, ParserConfigurationException, ParserException {
    return new Calendar[0];
}
```
with
```java
public List<Calendar> getObjectsByMultiget(List<String> hrefs) throws IOException, ParserConfigurationException {
    if (hrefs.isEmpty()) return List.of();
    return getStore().getClient().report(getPath(), new CalendarMultiget(hrefs), new GetCalendarData());
}
```

**Why**: 
- `ArrayList<URI>` is unidiomatic; `List<String>` matches what the REPORT XML actually contains.
- The `Element calData` parameter is unused in the current empty implementation and isn't meaningful in the RFC's basic multiget shape; if extended `calendar-data` filtering becomes needed it can be a separate overload.
- Return type `List<Calendar>` matches other report-returning methods on the same class (`getComponentsByType`, `getEventsForTimePeriod`).
- The empty-input short-circuit avoids issuing a malformed multiget with zero hrefs (RFC requires ≥ 1).
- `throws DavException, ParserException` removed because the current `report(...)` overload only throws `IOException` and `ParserConfigurationException`.

**Breaking?** The old method always returned `Calendar[0]`. Any caller using it was either ignoring results or getting nothing useful. **BREAKING** in type signature, but no real-world callers can be broken in semantics.

### Decision 3: `FreeBusyQuery` takes a time range, returns one synthesized VFREEBUSY-bearing Calendar

**Choice**: New class:
```java
public class FreeBusyQuery extends ReportInfo implements XmlSupport {
    private final Instant start;
    private final Instant end;
    public FreeBusyQuery(Instant start, Instant end) {
        super(CalDavPropertyName.FREE_BUSY_QUERY, 0);
        this.start = start;
        this.end = end;
    }
    @Override
    public Element toXml(Document document) {
        // <C:free-busy-query>
        //   <C:time-range start="..." end="..."/>
        // </C:free-busy-query>
    }
}
```
Need to register `FREE_BUSY_QUERY` as a `ReportType` in `CalDavPropertyName` (currently not registered).

**Why**: RFC 4791 §7.10 is the simplest of the CalDAV REPORTs — only a time-range filter. Using `Instant` for time bounds avoids the legacy `net.fortuna.ical4j.model.DateTime` (which iCal4j is moving away from). Per RFC, the time bound XML attributes are UTC-formatted DateTime ("19980119T070000Z"). A helper formats Instant → that string.

**Alternatives considered**:
- *Use `Temporal` or `LocalDateTime`*: rejected — UTC instant is the protocol's actual unit.
- *Match `findFreeBusyInfoForAttendees`'s use of legacy `DtStart`/`DtEnd`*: rejected — that's the older scheduling flow; we shouldn't perpetuate its API choices.

### Decision 4: `doFreeBusyQuery(Instant start, Instant end)` returns `Calendar` (single VFREEBUSY)

**Choice**: Change `doFreeBusyQuery()` signature to `Calendar doFreeBusyQuery(Instant start, Instant end)`. Per RFC 4791 §7.10, the response body is exactly one iCalendar object containing a single VFREEBUSY component. Use the existing `GetFreeBusyData` response handler (which returns `List<ScheduleResponse>` — confirm; if it returns something different from a `Calendar`, build a small handler that returns `Calendar`).

**Why**: The free-busy-query response shape is well-defined and single-valued. Returning `Calendar` (not `List<Calendar>`) reflects the protocol.

**Open question (Open Q1)**: confirm `GetFreeBusyData` returns the right shape; if not, build a `GetFreeBusyCalendar` handler.

### Decision 5: `CalDavCalendarCollection.export()` aggregates VEVENTs via Calendar.merge

**Choice**: 
```java
public Calendar export() {
    var aggregate = new Calendar();
    for (Calendar calendar : getComponentsByType(Component.VEVENT)) {
        aggregate = aggregate.merge(calendar);
    }
    return aggregate;
}
```

**Why**: Direct port of `LocalCalendarCollection.export()`. iCal4j's `Calendar.merge` instance method handles property deduplication and component union.

**Trade-off**: For very large collections this materializes everything in memory. Acceptable; matches the Local behaviour. A streaming variant could come later if needed.

### Decision 6: `CardDavCollection.export()` aggregates VCards via VCard.merge

**Choice**:
```java
public VCard export() {
    var aggregate = new VCard();
    try {
        for (VCard card : getAll()) {
            aggregate = aggregate.merge(card);
        }
    } catch (ObjectStoreException e) {
        throw new RuntimeException(e);
    }
    return aggregate;
}
```

**Why**: Direct port of `LocalCardCollection.export()`. Same trade-off as above.

### Decision 7: Delete dead `merge(String, CalendarCollection)` from both stores

**Choice**: Remove the methods entirely (no @Deprecated period). Including the trailing commented-out `replace(String id, CalendarCollection)` ghost-code blocks.

**Why**: Not on any interface (verified by grep of `merge` in `ObjectStore.java` — no match). Never called from anywhere in the codebase. Always throws. CardDavStore's return type (`CalendarCollection`) is wrong. Keeping them is an attractive nuisance — anyone reading the class might think they're real.

**Breaking?** Theoretically for any external caller who imports `CalDavCalendarStore`/`CardDavStore` and calls `.merge(...)`. No such caller exists in this codebase. The methods always threw `UnsupportedOperationException`, so any caller was already getting a runtime failure. Net positive.

## Risks / Trade-offs

- **[Multiget signature change is a breaking API change]** → mitigated by the fact that the prior method always returned an empty array; no real-world caller could have been depending on the result. Theoretical breakage on type signature only.

- **[FreeBusyQuery's `GetFreeBusyData` response handler shape may not match expected return]** → mitigation: read `GetFreeBusyData.java` during apply; if it returns `List<ScheduleResponse>` (used by `findFreeBusyInfoForAttendees`), build a new lightweight handler `GetFreeBusyCalendar` that extracts the single VFREEBUSY-bearing `Calendar` from the response body.

- **[`export()` materializes the full collection in memory]** → matches Local behaviour. Acceptable for V1. Document as known.

- **[Deleting `merge(String, CalendarCollection)` is technically breaking]** → as above, no callers, methods always threw. Net positive removal.

- **[Multiget on a missing href returns a per-href 404 in the response, not a top-level failure]** → callers may want a way to distinguish "fetched" from "404"d hrefs. For V1 we return only the successfully-fetched calendars, dropping missing hrefs silently (per `GetCalendarData`'s existing behaviour). A future change could surface partial-failure metadata.

## Open Questions

1. Does `GetFreeBusyData` return a shape suitable for `doFreeBusyQuery` (single `Calendar`)? Need to inspect it during apply. If yes, reuse. If no, build a small `GetFreeBusyCalendar` handler.
2. Should `getObjectsByMultiget` accept `List<URI>` instead of `List<String>` for slight type-safety? Leaning *String* because the wire format is a string href and conversion adds friction without benefit.
3. Should `doFreeBusyQuery` accept `Temporal` rather than `Instant` for more flexibility (e.g. caller passes `ZonedDateTime`)? Leaning *Instant* — UTC instant is the protocol's native unit, callers can `.toInstant()` from any zoned type.

## Implementation Notes (added during apply)

### Resolutions to open questions

- **Q1**: `GetFreeBusyData` returns `List<ScheduleResponse>` (used by the scheduling-outbox-driven `findFreeBusyInfoForAttendees` flow). Built a new `GetFreeBusyCalendar` handler that parses the response body via `CalendarBuilder` and returns a single `Calendar`. Confirms the design's fallback path.
- **Q2** & **Q3**: kept the proposed defaults (`List<String>` and `Instant`).

### Multiget XML namespace correction

Initial implementation emitted `<C:getetag/>` (CalDAV namespace). RFC 4791 §7.9 specifies `<D:getetag/>` (DAV namespace). The EventQuery class in the same module also uses the wrong namespace via `newCalDavElement(document, DavConstants.PROPERTY_GETETAG)`, but EventQuery isn't currently exercised by integration tests so the bug was latent. Corrected in CalendarMultiget by using `newElement(document, DavConstants.PROPERTY_GETETAG, DavConstants.NAMESPACE)`. EventQuery's identical bug is out of scope for this change but flagged here as a follow-up.

### Multiget href absolute-path requirement

The `<D:href>` elements in the multiget request body must contain the full server-absolute path (including repository prefix) — the same shape the server returns in PROPFIND responses. The collection's `getPath()` returns the path *without* the repository prefix (the DefaultDavClient adds it when issuing requests via `resolvePath`). For multiget, the request body is XML content that the server reads as-is — no `resolvePath` applies. Tests construct hrefs as `getRepositoryPath() + collection.getPath() + "/" + uid + ".ics"`, with `'/'` collapsed away.

### Server-specific quirks for free-busy-query

Two server behaviour differences surfaced during testing, both `@IgnoreIf`-gated rather than worked around in client code (the client behaviour is RFC-correct):

- **Radicale** does not populate `FREEBUSY` intervals from collection events in its `free-busy-query` REPORT response. The VFREEBUSY component returned for an empty range is also absent (rather than empty). Both freebusy tests are `@IgnoreIf` for Radicale.
- **Baikal** populates `FREEBUSY` intervals, but applies its configured timezone (`Australia/Melbourne`) to event times in a way that shifts UTC values by the zone offset. An event PUT with `DTSTART:20300101T100000Z` appears in the busy report as `20300101T000000Z/20300101T010000Z` (a ~10-hour shift). The busy-interval test asserts only that *some* FREEBUSY interval is reported on Baikal, not the exact value — verifying structural correctness without coupling to the server's tz handling.

### Server-specific quirk for calendar-multiget

**Baikal** returns `403 Forbidden` for the entire multiget request if any requested href is missing, rather than returning per-href status codes per RFC 4791 §7.9. The "skips missing hrefs" test is `@IgnoreIf` for Baikal; per-href-status behaviour is verified on Radicale.

### Test-side DTSTART/DTEND serialization

`new DtStart<>(Instant)` (typed Temporal constructor) writes the property as a floating local datetime, not UTC. Tests use `new DtStart<>(String)` with an explicit UTC-formatted string ("20300101T100000Z") to force UTC serialization. This is a test-side helper concern; the production code never invokes `DtStart` constructors directly.

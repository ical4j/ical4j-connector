## Context

The MSGraph module already has the right structure — caller-injected `GraphServiceClient`, a thin `AbstractMSGraphObjectStore` base, an id-addressed `MSGraphCalendarCollection` that lazily fetches from the SDK, and a builder pair for iCal4j ↔ SDK conversion. What it lacks is *substance*: the two builders map a single field each, listing is unpaginated and self-inconsistent, and half the `CalendarCollection` contract returns stubs. The Google connector (`develop-google-connector`, archived 2026-05-29) defined the parity bar and is the structural reference here.

Microsoft Graph is a harder target than Google in four specific ways, and most of the design decisions below exist to handle them:

1. **`iCalUId` is read-only / server-assigned.** Unlike Google's `iCalUID`, you cannot set `iCalUId` when POSTing an event; Graph generates it. The original iCal `UID` is therefore not preserved by the server on create.
2. **Recurrence is structured, not iCal text.** Google round-trips recurrence as raw `RRULE:`/`EXDATE:` lines. Graph uses a typed `PatternedRecurrence` (`RecurrencePattern` + `RecurrenceRange`). A real translation is required.
3. **Date-times carry a zone *name*, often a Windows zone.** Graph `start`/`end` are `DateTimeTimeZone` (`dateTime` string + `timeZone` name). By default the name is a Windows zone (`"AUS Eastern Standard Time"`); IANA names require the `Prefer: outlook.timezone="..."` request header.
4. **`Calendar` resources are thin.** A Graph `Calendar` has `name`, `color`, `owner`, ownership flags — but **no description** and no settable default timezone. Some `CalendarCollection`/store inputs have nowhere to map.

## Goals / Non-Goals

**Goals:**
- Functional parity with the Google connector for the read / add / delete / list happy path, plus the field-level mapping fidelity Google achieved.
- A correct `get(uid)` round-trip: an event added through the connector and read back preserves UID, summary, description, location, start/end (incl. zone or all-day), organizer, attendees, and simple recurrence.
- Go *past* Google where the Graph API makes it cheap and honest: implement `merge`, `export`, and `removeAll` (Google stubs these).
- Transparent pagination so large calendars are not silently truncated.
- Be honest about impedance mismatches (`description`, `timeZone`, `iCalUId` assignment) rather than faking them.

**Non-Goals:**
- The `service/` subpackage (`CalendarService`, `ToDoService`, `PlannerService`, `OneNoteService`) — net-new VTODO/VJOURNAL scope, separate change.
- Recurrence *exceptions* / edited single occurrences (Graph `seriesMasterId` instances) and `RDATE`/`EXDATE`.
- Listener support (`getObjectStoreListeners` / `getObjectCollectionListeners` stay `null`, matching Google).
- Owning the OAuth / `TokenCredential` flow inside the store — auth stays caller-side.
- Live Graph integration tests.

## Decisions

### Decision 1: Builders cover the common-field set; everything else is dropped silently
**Choice**: `MSGraphEventBuilder` (VEvent → `Event`) and `ICalCalendarBuilder` (`Event` → `Calendar`) map the field set in the table below, in both directions. Properties not listed are silently ignored on conversion (consistent with the Google connector).

```
   iCal4j VEvent property           Microsoft Graph Event field
   ───────────────────────────────  ──────────────────────────────────────────
   UID                              iCalUId          (read-only on create — D2)
   SUMMARY                          subject
   DESCRIPTION                      body.content (contentType = text)
   LOCATION                         location.displayName
   DTSTART (date, all-day)          start (date at 00:00) + isAllDay = true
   DTSTART (date-time, with TZID)   start.dateTime + start.timeZone
   DTEND   (date, all-day)          end + isAllDay = true
   DTEND   (date-time, with TZID)   end.dateTime + end.timeZone
   ORGANIZER                        organizer.emailAddress.address / .name
   ATTENDEE (each)                  attendees[].emailAddress + .type + .status.response
   RRULE                            recurrence (PatternedRecurrence) — see D4
   CREATED                          createdDateTime   (read-only — read path only)
   LAST-MODIFIED                    lastModifiedDateTime (read-only — read path only)
```

PARTSTAT ↔ Graph `responseStatus.response`:
```
   iCal PARTSTAT     Graph responseStatus.response
   ───────────────   ─────────────────────────────
   NEEDS-ACTION      none / notResponded
   ACCEPTED          accepted
   DECLINED          declined
   TENTATIVE         tentativelyAccepted
```

**Why**: Matches the proven Google mapping surface, adjusted for Graph's field names and its `body`/`location`/`responseStatus` object wrappers.

**Body content type** (resolves Open Question 1): the write path sets `body.contentType = text`, because iCal `DESCRIPTION` is plain text by definition (RFC 5545). The read path uses `body.content` directly when `contentType = text`; when Graph returns `html` (Outlook's default storage format) it converts the HTML to plain text best-effort before emitting `DESCRIPTION`. Carrying the HTML alternate as `X-ALT-DESC` is deferred to follow-up.

### Decision 2: Accept that `iCalUId` is server-assigned; the read path is the source of truth
**Choice**: `MSGraphEventBuilder` does **not** rely on setting `iCalUId` to carry identity (Graph ignores it on create). On `add(Calendar)`, the connector POSTs the event and returns the `iCalUId` Graph assigns in the response. `ICalCalendarBuilder` reads that `iCalUId` back into the iCal `UID`, generating a random `Uid` only if Graph somehow returns none.

**Why**: Fighting the platform here is futile and silently lossy. The honest contract is "the store assigns identity on add, and `add()` returns it" — which is exactly what `ObjectCollection.add` already returns.

**Consequence / risk**: A caller who adds an event with a meaningful `UID:foo@example.com` and later looks it up by that original UID will not find it — the stored event has Graph's UID. Documented as a known limitation; callers should use the UID returned from `add()`. (A future enhancement could stash the original UID in an `extendedProperty`.)

**Alternatives considered**: Persisting the original UID in an Outlook `singleValueExtendedProperty` — richer round-trip but adds a Graph-specific read/write path and query complexity; deferred.

### Decision 3: List via `events()` (master events), paginated — not `calendarView()`
**Choice**: Both `listObjectUIDs()` and `get(uid)` use `me().calendars().byCalendarId(id).events()`. `get(uid)` filters server-side (`$filter=iCalUId eq '…'`) where possible; `listObjectUIDs()` pages through the full set. Pagination follows `@odata.nextLink` (via the Graph SDK `PageIterator`, or by re-issuing with the next-link URL) until exhausted.

**Why**: The current code is inconsistent — `listObjectUIDs()` uses `calendarView()` while `get()` uses `events()`. `calendarView()` *expands* recurrences into instances and *requires* a `start`/`end` window; it answers "what's on my calendar between X and Y", not "what objects does this collection hold". A UID-addressed object store wants master events (one per UID), which is what `events()` returns. Choosing `events()` everywhere fixes both the inconsistency and the silent-window bug.

**Alternatives considered**: Keep `calendarView()` with a default wide window — rejected: it changes cardinality (instances ≠ objects) and bounds results by date.

### Decision 4: Recurrence — translate simple `RRULE` ↔ `PatternedRecurrence`; defer the rest
**Choice**: Map the common recurrence cases between iCal `RRULE` and Graph `PatternedRecurrence`:
- `FREQ=DAILY|WEEKLY|MONTHLY|YEARLY` → `RecurrencePattern.type` (`daily`/`weekly`/`absoluteMonthly`/`absoluteYearly`, or `relative*` when `BYDAY` ordinals are present)
- `INTERVAL` → `pattern.interval`
- `BYDAY` → `pattern.daysOfWeek` (+ `index` for relative monthly/yearly)
- `COUNT` → `range.type = numbered` + `numberOfOccurrences`; `UNTIL` → `range.type = endDate` + `endDate`; neither → `range.type = noEnd`
- `range.startDate` is taken from `DTSTART`.

`RDATE` and `EXDATE` are **not** mapped (no direct Graph equivalent — Graph models exceptions as separate occurrence objects). Recurrence patterns Graph cannot express round-trip lossily and are documented.

**Why**: Recurrence is a real part of calendar parity (Google does it), but Graph's structured model makes full fidelity a large task. The common patterns cover the overwhelming majority of real events; the long tail is documented and deferred rather than silently corrupted.

**Alternatives considered**: Defer recurrence entirely (cheaper, but a visible parity gap vs Google) — rejected. Full `RRULE` fidelity incl. `BYSETPOS`/`BYMONTHDAY` edge cases — deferred as follow-up.

### Decision 5: Preserve the event's own zone via a bundled Windows↔IANA map — do **not** force a `Prefer` zone
**Choice**: The connector reads each event in its stored timezone (Graph's default behaviour, no `Prefer: outlook.timezone` header) and maps the returned `DateTimeTimeZone` → `Temporal`: if `timeZone` is already an IANA name use it directly, otherwise translate the Windows zone name to IANA via a bundled lookup, defaulting unknown names to UTC with a logged warning. Writes send IANA `timeZone` names derived from the iCal `TZID` (Graph accepts IANA on write — no translation needed). All-day events set `isAllDay = true` and use date-only `start`/`end`.

The Windows↔IANA lookup ships as a **CLDR-derived resource bundled in the module** (a generated `windows-iana.properties` / map loaded once), so there is **no new runtime dependency** (resolves Open Question 4).

**Why**: An earlier draft proposed forcing `Prefer: outlook.timezone="UTC"` to get parseable output. That is rejected: it collapses every event to UTC and **destroys the original zone**, breaking the `Australia/Melbourne` round-trip this change promises. Faithful round-tripping requires reading the event's *own* zone, which Graph returns as a Windows name by default — so the Windows→IANA map is the primary read path, not a fallback. A bundled CLDR table is complete and version-pinned without dragging in a dependency.

**Risk**: Windows↔IANA mapping is many-to-one and CLDR-version-sensitive. Mitigation: bundle a current CLDR snapshot, default unknown names to UTC with a logged warning, refresh the snapshot as a maintenance task, and unit-test all-day vs timed and the Windows-name → IANA path explicitly.

**Alternatives considered**: *Force `Prefer: outlook.timezone`* — rejected (loses the zone). *Add a CLDR/zone-mapping library dependency* — rejected; a bundled generated resource is enough and keeps the dependency surface flat.

### Decision 6: Implement `merge`, `export`, `removeAll` — going past Google
**Choice**:
- `export()` — page all master events, convert each via `ICalCalendarBuilder`, and aggregate the `VEVENT`s into one `Calendar`.
- `merge(Calendar)` — split the incoming `Calendar` into per-`UID` objects and `add()` each; return the resulting `Uid[]`.
- `removeAll(uid...)` — for each UID resolve the Graph event id (`$filter=iCalUId eq '…'`), capture the event as a `Calendar`, delete it, and return the list of removed calendars.

**Why**: The Google connector stubbed these because Google's stub baseline did; but they are straightforward and genuinely useful on Graph, and implementing them makes the `CalendarCollection` contract actually complete. Where the work is cheap and correct, parity should mean "complete", not "matched the stub".

**Alternatives considered**: Mirror Google's stubs for symmetry — rejected; leaves the contract half-empty for no benefit.

### Decision 7: Honest stubs for unmappable inputs
**Choice**: `getDescription()` returns `""` (Graph `Calendar` has no description); `getTimeZone()` returns `null`; the metadata accessors (`getMaxResourceSize`, `getMin/MaxDateTime`, `getMaxInstances`, `getMaxAttendeesPerInstance`, `getSupportedComponentTypes`, `getSupportedMediaTypes`) keep their safe defaults. `addCollection(id, name, description, supportedComponents, timezone[, workspace])` sets `name` only; `id` (server-generated), `description`, `supportedComponents`, and `timezone` are dropped (documented).

**Why**: These are platform limits, not laziness. Faking a description or a per-collection timezone would mislead cross-provider callers.

## Risks / Trade-offs

- **[Original UID not preserved on add]** (Decision 2) → callers must use the UID returned by `add()`, not the one they submitted. Mitigation: documented in spec + javadoc; future `extendedProperty` enhancement noted.
- **[Recurrence fidelity]** (Decision 4) → only common `RRULE` shapes round-trip; `RDATE`/`EXDATE` and exceptions are dropped. Mitigation: documented limitation + unit tests for the supported shapes; assert dropped parts don't throw.
- **[Windows zone names]** (Decision 5) → unrecognised Windows zones fall back to UTC. Mitigation: cover common zones, log on fallback, test all-day vs timed explicitly.
- **[Pagination correctness]** → naive single-call listing truncates at the Graph page size (default 10). Mitigation: page from day one via `PageIterator` / `@odata.nextLink`; unit-test multi-page aggregation.
- **[`removeAll` is N+1 calls]** → one filter + one delete per UID. Acceptable for V1; a `$batch` optimisation is possible later.
- **[No integration tests]** → live Graph tests need a tenant + credentials. Mitigation: unit tests with a mocked `GraphServiceClient` cover wiring; builder round-trip tests cover mapping.

## Resolved Questions

1. **Description body content type** → **`text`** on write (iCal `DESCRIPTION` is plain text per RFC 5545); read converts an `html` body to plain text best-effort. `X-ALT-DESC` deferred. (Folded into Decision 1.)
2. **`removeAll` when a UID is absent** → **skip missing UIDs silently** and return only the calendars actually removed (best-effort bulk delete). (Reflected in Decision 6 and the spec's `removeAll skips unknown UIDs` scenario.)
3. **`get(uid)` filter vs scan** → **server-side `$filter=iCalUId eq '…'` first, paged-scan fallback** if a tenant/query rejects the filter. (Folded into Decision 3.)
4. **Windows↔IANA mapping source** → **bundle a CLDR-derived resource in the module; no new runtime dependency**; unknown zones default to UTC with a warning. (Folded into Decision 5, which also reverses the earlier `Prefer: outlook.timezone` idea to keep round-trips faithful.)

## Context

The `ical4j-connector-msgraph` module is the reference: it gives us a working shape for a SaaS-backed `ObjectStore<CalendarCollection>` implementation. Its key choices are (a) auth is constructed by the caller and the client is injected into the store, (b) the store holds a thin wrapper over the SDK, (c) the collection class is identified by an id (and optional workspace id) and lazily fetches data from the SDK on each call, and (d) two builder classes handle the iCal4j ↔ SDK conversion in both directions.

Google Calendar's API surface is similar enough to mirror the shape, but different enough in two ways worth calling out:

1. **No "calendar group" concept.** Microsoft Graph has `calendarGroups`, which the MSGraph connector surfaces as `workspaceIds`. Google has only a flat list of calendars accessible via `calendarList()` (the user's view, including subscribed) and `calendars()` (CRUD on owned calendars). There is nothing for workspaces to map to.
2. **iCal-friendly fields.** Google's `Event` exposes `iCalUID` directly (matching MSGraph) and exposes recurrence rules as raw iCal lines (`RRULE:…`, `EXDATE:…`), which sidesteps the conversion work MSGraph requires for recurrence.

The existing stub `GoogleCalendarStore` was built with the intention to mirror MSGraph; this design fleshes it out and corrects one misalignment (workspace semantics).

## Goals / Non-Goals

**Goals:**
- Functional parity with the MSGraph connector for the read/add/delete happy path.
- Faithful, lossless iCal ↔ Google Event mapping for the common fields (summary, description, location, start/end including all-day, organizer, attendees, recurrence, timestamps, iCalUID).
- Be honest about the impedance mismatch on workspaces — collapse to a single `DEFAULT_WORKSPACE` rather than pretend.
- Visual/structural similarity to the MSGraph module so a reader can navigate one if they know the other.

**Non-Goals:**
- Implementing `merge`, `export`, `removeAll`, or listener support on the collection (MSGraph stubs these too — out of scope for parity).
- Filling in the `service/` subpackage (`GoogleCalendarService`, `GoogleKeepService`, `GoogleTasksService`). Future work.
- Owning the OAuth flow inside the store — auth stays caller-side.
- Integration tests against the live Google API.
- Google-specific features without an iCal equivalent (Hangout/Meet links, reminders, colors, attachments, conference data).

## Decisions

### Decision 1: Drop workspace semantics — `listWorkspaceIds()` returns `[DEFAULT_WORKSPACE]`
**Choice**: `listWorkspaceIds()` returns a single-element list containing `ObjectStore.DEFAULT_WORKSPACE`. Workspace-parameterised methods (`addCollection(name, workspace)`, `getCollection(id, workspace)`, `getCollections(workspace)`) ignore the workspace argument (or reject anything other than the default — TBD, see Open Questions).

**Why**: Google Calendar has no equivalent of MS Graph's `calendarGroups`. The current stub maps "workspace" to calendar ID, which conflates two different abstractions (workspaces group collections; a collection is itself the unit of CRUD). That mapping would silently break any cross-provider code that uses workspace IDs.

**Alternatives considered**:
- *Workspace = account / delegated identity*: richer but speculative; no current driver and Google's API for delegated access is a separate code path.
- *Keep current behaviour*: rejected — it is the bug.

### Decision 2: Auth stays caller-side
**Choice**: `GoogleCalendarStore` takes a pre-constructed `com.google.api.services.calendar.Calendar` client in its constructor. `connect()` / `connect(String, char[])` return `false` and `disconnect()` is a no-op, matching MSGraph.

**Why**: The Google OAuth installed-app flow (`google-oauth-client-jetty`) opens a local web server and a browser — embedding that into `ObjectStore.connect()` would require pulling UI concerns into a connector library that is otherwise headless. Callers in different deployment contexts (web app, daemon, service account) construct the client differently; pushing that decision out is correct.

**Alternatives considered**:
- *Internal OAuth flow*: convenient demo, wrong default for a library. Could revisit as a separate helper in `service/` later.

### Decision 3: `getCollections()` uses `calendarList()`, CRUD uses `calendars()`
**Choice**: Listing a user's collections goes via `calendarList().list()` (returns owned + subscribed). Mutations go via `calendars()` (which only operates on owned calendars). `getCollection(id)` wraps the id without an eager fetch — same as MSGraph.

**Why**: A user expects "list my calendars" to include the calendars they actually see in Google Calendar, which includes subscribed ones. But they only have permission to mutate owned calendars; surfacing the failure when they try to delete a subscribed calendar is fine — the Google API will return a 403 and we'll let it propagate as an `ObjectStoreException`.

**Alternatives considered**:
- *Use `calendars()` for everything*: hides subscribed calendars from listing — surprising.
- *Filter `calendarList()` to owned-only for `getCollections`*: pretends the subscribed ones don't exist; unhelpful.

### Decision 4: No `AbstractGoogleObjectStore` base class
**Choice**: `GoogleCalendarStore` holds the `Calendar` client directly. No abstract intermediate class.

**Why**: MSGraph has `AbstractMSGraphObjectStore` because it anticipates a `CardStore` sibling (and possibly Tasks). Google currently has no second store class on the horizon (Keep/Tasks would map to other collection types, not `CalendarCollection`). Introducing an abstract base with one concrete subclass is premature.

**Alternatives considered**:
- *Mirror MSGraph exactly*: nice symmetry; rejected as premature abstraction.

### Decision 5: Rename constructor field `calendarService` → `client`
**Choice**: Rename the field in `GoogleCalendarStore` from `calendarService` to `client` and expose it to the package via a `Calendar getClient()` method (mirroring `AbstractMSGraphObjectStore.getClient()`).

**Why**: Three distinct things are called "Calendar" in scope (`com.google.api.services.calendar.Calendar`, `net.fortuna.ical4j.model.Calendar`, the empty `GoogleCalendarService` class). Calling the field `client` removes one source of confusion. **BREAKING** to anyone instantiating `GoogleCalendarStore` today — but the constructor signature is unchanged, only the internal field name and the new accessor are new.

### Decision 6: Two builder classes in the same package
**Choice**: `GoogleEventBuilder` (VEvent → Google Event) and `ICalCalendarBuilder` (Google Event → iCal4j Calendar) both live in `org.ical4j.connector.google`, mirroring MSGraph's `MSGraphEventBuilder` + `ICalCalendarBuilder` pair.

**Why**: Naming consistency across modules. Note `ICalCalendarBuilder` is the same simple name as MSGraph's — different package, no clash.

### Decision 7: Mapping coverage for V1
The iCal4j ↔ Google Event mapping covers the following fields. Anything not listed is silently ignored on conversion (consistent with MSGraph's current behaviour).

```
   iCal4j VEvent              Google Event
   ─────────────────────────  ─────────────────────────────
   UID                        iCalUID
   SUMMARY                    summary
   DESCRIPTION                description
   LOCATION                   location
   DTSTART (date)             start.date  (LocalDate)
   DTSTART (date-time + TZ)   start.dateTime + start.timeZone
   DTEND   (date)             end.date
   DTEND   (date-time + TZ)   end.dateTime + end.timeZone
   ORGANIZER                  organizer.email / displayName
   ATTENDEE (each)            attendees[].email / displayName /
                              responseStatus (mapped from PARTSTAT)
   RRULE / EXDATE / RDATE     recurrence[]  (Google returns these
                              as raw iCal lines — pass through)
   CREATED                    created
   LAST-MODIFIED              updated
```

PARTSTAT ↔ Google `responseStatus` mapping:
```
   iCal PARTSTAT     Google responseStatus
   ───────────────   ─────────────────────
   NEEDS-ACTION      needsAction
   ACCEPTED          accepted
   DECLINED          declined
   TENTATIVE         tentative
```

## Risks / Trade-offs

- **[All-day vs timed event ambiguity in builders]** → Google's `EventDateTime` uses `date` for all-day and `dateTime` for timed. Code must check which is set; getting it wrong silently corrupts dates. Mitigation: unit tests for both cases with explicit assertions on which field is populated.

- **[Timezone handling]** → Google requires `timeZone` alongside `dateTime` for events that recur or are displayed cross-zone. iCal4j carries TZ as a parameter on DTSTART. Mitigation: extract the `TZID` parameter explicitly in `GoogleEventBuilder`; default to UTC if absent.

- **[Recurrence overrides not in V1]** → Google represents recurring-event exceptions as separate `Event` instances pointing at a parent via `recurringEventId`. The V1 mapping treats the parent's `recurrence[]` as authoritative and does not reconstruct overrides into VEVENT instances. A recurring series with edited single instances will round-trip lossily. Mitigation: documented as a known limitation in the spec; deferred to follow-up.

- **[`listObjectUIDs` may paginate]** → Google's `events().list()` returns up to ~250 by default and uses page tokens. The naive single-call implementation will silently truncate large calendars. Mitigation: implement pagination from day one — loop on `getNextPageToken()` until null.

- **[Subscribed-calendar mutation failure]** → Deleting a subscribed (non-owned) calendar via `calendars().delete(id)` returns 403. The exception propagates as `ObjectStoreException`. Mitigation: documented; acceptable for V1.

- **[No integration tests]** → Live Google API tests need credentials and a service account / OAuth flow. Mitigation: unit tests with mocked `Calendar` client cover the wiring; round-trip tests on builders cover the mapping.

## Open Questions

1. Should `addCollection(name, workspace)` etc. **reject** non-default workspaces, or **silently ignore** the workspace argument? Rejecting surfaces the impedance mismatch; ignoring is more forgiving for cross-provider code. Leaning *reject with `ObjectStoreException`* if `workspace != DEFAULT_WORKSPACE`.
2. The two-arg `addCollection(name, ...)` signatures accept `id`, `description`, `supportedComponents`, `timezone`. Google's API takes only `summary`, `description`, `location`, `timeZone`. Drop unsupported fields silently, or throw? Leaning *silently drop `supportedComponents`* (the iCal-side abstraction over what Google fundamentally supports), *honour `description` and `timezone`*, *ignore the caller-supplied `id`* (Google generates IDs).
3. Authentication helpers — is there appetite for a thin `GoogleAuthHelper` (separate from the store) wrapping the jetty OAuth flow for ease of demo/test? Or keep the module purely calendar-focused and let callers handle auth? Currently leaning toward *defer*.

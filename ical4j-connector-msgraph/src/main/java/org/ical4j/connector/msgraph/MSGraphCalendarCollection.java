package org.ical4j.connector.msgraph;

import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.EventCollectionResponse;
import com.microsoft.kiota.ApiException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.MediaType;
import org.ical4j.connector.ObjectStoreException;
import org.ical4j.connector.event.ListenerList;
import org.ical4j.connector.event.ObjectCollectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Copyright (c) 2026, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class MSGraphCalendarCollection implements CalendarCollection {

    private static final Logger LOG = LoggerFactory.getLogger(MSGraphCalendarCollection.class);

    private final MSGraphCalendarStore store;

    private final String calendarId;

    private final String calendarGroupId;

    public MSGraphCalendarCollection(MSGraphCalendarStore store, String calendarId) {
        this(store, calendarId, null);
    }

    public MSGraphCalendarCollection(MSGraphCalendarStore store, String calendarId, String calendarGroupId) {
        this.store = store;
        this.calendarId = calendarId;
        this.calendarGroupId = calendarGroupId;
    }

    @Override
    public Calendar getTimeZone() {
        return null;
    }

    @Override
    public String[] getSupportedComponentTypes() {
        return new String[0];
    }

    @Override
    public MediaType[] getSupportedMediaTypes() {
        return new MediaType[0];
    }

    @Override
    public long getMaxResourceSize() {
        return 0;
    }

    @Override
    public Instant getMinDateTime() {
        return null;
    }

    @Override
    public Instant getMaxDateTime() {
        return null;
    }

    @Override
    public Integer getMaxInstances() {
        return 0;
    }

    @Override
    public Integer getMaxAttendeesPerInstance() {
        return 0;
    }

    @Override
    public Uid[] merge(Calendar calendar) throws FailedOperationException, ObjectStoreException {
        List<Uid> uids = new ArrayList<>();
        for (Calendar object : Calendars.split(calendar)) {
            if (object.getComponents(Component.VEVENT).isEmpty()) {
                // skip non-event objects (e.g. VTODO) rather than failing after earlier objects are added..
                LOG.warn("Skipping object with no VEVENT; Graph calendars only hold events");
                continue;
            }
            // Graph assigns iCalUId server-side, so return the identifier from add() rather than the submitted UID..
            uids.add(new Uid(add(object)));
        }
        return uids.toArray(new Uid[0]);
    }

    @Override
    public Calendar export() {
        Calendar aggregate = new Calendar();
        for (Event event : listAllEvents()) {
            try {
                Calendar built = new ICalCalendarBuilder().build(event);
                for (CalendarComponent component : built.<CalendarComponent>getComponents()) {
                    aggregate.add(component);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to convert event", e);
            }
        }
        return aggregate;
    }

    @Override
    public String getDisplayName() {
        return getCalendar().getName();
    }

    @Override
    public String getDescription() {
        // Microsoft Graph calendar resources have no description field.
        return "";
    }

    @Override
    public List<String> listObjectUIDs() {
        return listAllEvents().stream().map(Event::getICalUId).collect(Collectors.toList());
    }

    @Override
    public Optional<Calendar> get(String uid) {
        return findByUid(uid).map(event -> {
            try {
                return new ICalCalendarBuilder().build(event);
            } catch (IOException e) {
                throw new RuntimeException("Failed to convert event", e);
            }
        });
    }

    @Override
    public String add(Calendar object) throws ObjectStoreException {
        List<VEvent> vevents = object.getComponents(Component.VEVENT);
        if (vevents.isEmpty()) {
            throw new ObjectStoreException("Calendar contains no VEVENT component");
        }
        // post the series master; RECURRENCE-ID overrides can appear before it and have no Graph equivalent on create..
        VEvent icalEvent = vevents.stream()
                .filter(e -> e.getProperty(Property.RECURRENCE_ID).isEmpty())
                .findFirst()
                .orElseThrow(() -> new ObjectStoreException(
                        "Calendar contains only recurrence overrides (RECURRENCE-ID) and no series VEVENT"));
        if (vevents.size() > 1) {
            LOG.warn("Adding the series VEVENT only; {} other VEVENT(s) (e.g. recurrence overrides) are not applied",
                    vevents.size() - 1);
        }
        Event event = new MSGraphEventBuilder().vevent(icalEvent).build();
        Event result;
        if (calendarGroupId != null) {
            result = store.getClient().me().calendarGroups().byCalendarGroupId(calendarGroupId)
                    .calendars().byCalendarId(calendarId).events().post(event);
        } else {
            result = store.getClient().me().calendars().byCalendarId(calendarId).events().post(event);
        }
        return result.getICalUId();
    }

    @Override
    public List<Calendar> removeAll(String... uid) throws FailedOperationException {
        List<Calendar> removed = new ArrayList<>();
        for (String u : uid) {
            Optional<Event> found = findByUid(u);
            if (found.isEmpty()) {
                continue; // skip UIDs that resolve to no event (best-effort bulk delete)
            }
            Event event = found.get();
            try {
                removed.add(new ICalCalendarBuilder().build(event));
            } catch (IOException e) {
                throw new FailedOperationException("Failed to convert event " + u, e);
            }
            deleteEvent(event.getId());
        }
        return removed;
    }

    @Override
    public void delete() throws ObjectStoreException {
        if (calendarGroupId != null) {
            store.getClient().me().calendarGroups().byCalendarGroupId(calendarGroupId)
                    .calendars().byCalendarId(calendarId).delete();
        } else {
            store.getClient().me().calendars().byCalendarId(calendarId).delete();
        }
    }

    @Override
    public ListenerList<ObjectCollectionListener<Calendar>> getObjectCollectionListeners() {
        return null;
    }

    private com.microsoft.graph.models.Calendar getCalendar() {
        if (calendarGroupId != null) {
            return store.getClient().me().calendarGroups().byCalendarGroupId(calendarGroupId)
                    .calendars().byCalendarId(calendarId).get();
        } else {
            return store.getClient().me().calendars().byCalendarId(calendarId).get();
        }
    }

    /**
     * Lists all master events for this calendar, transparently following {@code @odata.nextLink}
     * pagination so the full event set is returned (not just the first page).
     */
    private List<Event> listAllEvents() {
        List<Event> events = new ArrayList<>();
        EventCollectionResponse response = getEventsPage(null);
        while (response != null) {
            if (response.getValue() != null) {
                events.addAll(response.getValue());
            }
            String next = response.getOdataNextLink();
            if (next == null) {
                break;
            }
            response = getEventsPage(next);
        }
        return events;
    }

    private EventCollectionResponse getEventsPage(String nextUrl) {
        if (calendarGroupId != null) {
            var builder = store.getClient().me().calendarGroups().byCalendarGroupId(calendarGroupId)
                    .calendars().byCalendarId(calendarId).events();
            return nextUrl != null ? builder.withUrl(nextUrl).get() : builder.get();
        } else {
            var builder = store.getClient().me().calendars().byCalendarId(calendarId).events();
            return nextUrl != null ? builder.withUrl(nextUrl).get() : builder.get();
        }
    }

    /**
     * Resolves an event by its {@code iCalUId}, filtering server-side where supported and falling
     * back to a paged scan if the filter is rejected by the service.
     */
    private Optional<Event> findByUid(String uid) {
        // escape single quotes per OData string literal rules so the UID can't alter the filter expression..
        String filter = "iCalUId eq '" + uid.replace("'", "''") + "'";
        try {
            EventCollectionResponse response;
            if (calendarGroupId != null) {
                response = store.getClient().me().calendarGroups().byCalendarGroupId(calendarGroupId)
                        .calendars().byCalendarId(calendarId).events()
                        .get(cfg -> cfg.queryParameters.filter = filter);
            } else {
                response = store.getClient().me().calendars().byCalendarId(calendarId).events()
                        .get(cfg -> cfg.queryParameters.filter = filter);
            }
            if (response != null && response.getValue() != null) {
                return response.getValue().stream()
                        .filter(event -> Objects.equals(event.getICalUId(), uid))
                        .findFirst();
            }
            return Optional.empty();
        } catch (ApiException e) {
            if (e.getResponseStatusCode() != 400) {
                throw e;
            }
            // filter rejected by the service, fall back to a paged scan..
            return listAllEvents().stream()
                    .filter(event -> Objects.equals(event.getICalUId(), uid))
                    .findFirst();
        }
    }

    private void deleteEvent(String eventId) {
        if (calendarGroupId != null) {
            store.getClient().me().calendarGroups().byCalendarGroupId(calendarGroupId)
                    .calendars().byCalendarId(calendarId).events().byEventId(eventId).delete();
        } else {
            store.getClient().me().calendars().byCalendarId(calendarId).events().byEventId(eventId).delete();
        }
    }
}

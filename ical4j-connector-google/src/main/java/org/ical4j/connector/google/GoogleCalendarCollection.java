package org.ical4j.connector.google;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Uid;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.MediaType;
import org.ical4j.connector.ObjectStoreException;
import org.ical4j.connector.event.ListenerList;
import org.ical4j.connector.event.ObjectCollectionListener;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
public class GoogleCalendarCollection implements CalendarCollection {

    private final GoogleCalendarStore store;

    private final String calendarId;

    public GoogleCalendarCollection(GoogleCalendarStore store, String calendarId) {
        this.store = store;
        this.calendarId = calendarId;
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
        return new Uid[0];
    }

    @Override
    public Calendar export() {
        return null;
    }

    @Override
    public String getDisplayName() {
        try {
            return store.getClient().calendars().get(calendarId).execute().getSummary();
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch calendar", e);
        }
    }

    @Override
    public String getDescription() {
        try {
            return store.getClient().calendars().get(calendarId).execute().getDescription();
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch calendar", e);
        }
    }

    @Override
    public List<String> listObjectUIDs() {
        List<String> uids = new ArrayList<>();
        for (Event event : listAllEvents()) {
            uids.add(event.getICalUID());
        }
        return uids;
    }

    @Override
    public Optional<Calendar> get(String uid) {
        for (Event event : listAllEvents()) {
            if (Objects.equals(event.getICalUID(), uid)) {
                try {
                    return Optional.of(new ICalCalendarBuilder().build(event));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to convert event", e);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public String add(Calendar object) throws ObjectStoreException {
        VEvent icalEvent = (VEvent) object.getComponent(Component.VEVENT)
                .orElseThrow(() -> new ObjectStoreException("Calendar contains no VEVENT component"));
        Event event = new GoogleEventBuilder().vevent(icalEvent).build();
        try {
            Event result = store.getClient().events().insert(calendarId, event).execute();
            return result.getICalUID();
        } catch (IOException e) {
            throw new ObjectStoreException("Failed to add event", e);
        }
    }

    @Override
    public List<Calendar> removeAll(String... uid) throws FailedOperationException {
        return List.of();
    }

    @Override
    public void delete() throws ObjectStoreException {
        try {
            store.getClient().calendars().delete(calendarId).execute();
        } catch (IOException e) {
            throw new ObjectStoreException("Failed to delete calendar", e);
        }
    }

    @Override
    public ListenerList<ObjectCollectionListener<Calendar>> getObjectCollectionListeners() {
        return null;
    }

    private List<Event> listAllEvents() {
        List<Event> events = new ArrayList<>();
        String pageToken = null;
        try {
            do {
                Events page = store.getClient().events().list(calendarId).setPageToken(pageToken).execute();
                if (page.getItems() != null) {
                    events.addAll(page.getItems());
                }
                pageToken = page.getNextPageToken();
            } while (pageToken != null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to list events", e);
        }
        return events;
    }
}

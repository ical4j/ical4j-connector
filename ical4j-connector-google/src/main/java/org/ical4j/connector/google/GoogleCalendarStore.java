package org.ical4j.connector.google;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.property.TzId;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import org.ical4j.connector.event.ListenerList;
import org.ical4j.connector.event.ObjectStoreListener;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
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
public class GoogleCalendarStore implements ObjectStore<CalendarCollection> {

    private final Calendar client;

    public GoogleCalendarStore(Calendar client) {
        this.client = client;
    }

    Calendar getClient() {
        return client;
    }

    @Override
    public boolean connect() throws ObjectStoreException {
        return false;
    }

    @Override
    public boolean connect(String username, char[] password) throws ObjectStoreException {
        return false;
    }

    @Override
    public void disconnect() throws ObjectStoreException {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public CalendarCollection addCollection(String name) throws ObjectStoreException {
        try {
            com.google.api.services.calendar.model.Calendar calendar =
                    new com.google.api.services.calendar.model.Calendar().setSummary(name);
            com.google.api.services.calendar.model.Calendar result = client.calendars().insert(calendar).execute();
            return new GoogleCalendarCollection(this, result.getId());
        } catch (IOException e) {
            throw new ObjectStoreException("Failed to add collection", e);
        }
    }

    @Override
    public CalendarCollection addCollection(String name, String workspace) throws ObjectStoreException {
        assertDefaultWorkspace(workspace);
        return addCollection(name);
    }

    @Override
    public CalendarCollection addCollection(String id, String name, String description, String[] supportedComponents,
                                            net.fortuna.ical4j.model.Calendar timezone) throws ObjectStoreException {
        try {
            com.google.api.services.calendar.model.Calendar calendar =
                    new com.google.api.services.calendar.model.Calendar()
                            .setSummary(name)
                            .setDescription(description)
                            .setTimeZone(extractTzId(timezone));
            com.google.api.services.calendar.model.Calendar result = client.calendars().insert(calendar).execute();
            return new GoogleCalendarCollection(this, result.getId());
        } catch (IOException e) {
            throw new ObjectStoreException("Failed to add collection", e);
        }
    }

    @Override
    public CalendarCollection addCollection(String id, String name, String description, String[] supportedComponents,
                                            net.fortuna.ical4j.model.Calendar timezone, String workspace) throws ObjectStoreException {
        assertDefaultWorkspace(workspace);
        return addCollection(id, name, description, supportedComponents, timezone);
    }

    @Override
    public CalendarCollection removeCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        try {
            client.calendars().delete(id).execute();
            return null;
        } catch (IOException e) {
            throw new ObjectStoreException("Failed to remove collection", e);
        }
    }

    @Override
    public CalendarCollection getCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        return new GoogleCalendarCollection(this, id);
    }

    @Override
    public CalendarCollection getCollection(String id, String workspace) throws ObjectStoreException, ObjectNotFoundException {
        assertDefaultWorkspace(workspace);
        return getCollection(id);
    }

    @Override
    public List<CalendarCollection> getCollections() throws ObjectStoreException, ObjectNotFoundException {
        try {
            return Objects.requireNonNullElse(client.calendarList().list().execute().getItems(), List.<CalendarListEntry>of())
                    .stream()
                    .map(e -> (CalendarCollection) new GoogleCalendarCollection(this, e.getId()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ObjectStoreException("Failed to list collections", e);
        }
    }

    @Override
    public List<CalendarCollection> getCollections(String workspace) throws ObjectStoreException, ObjectNotFoundException {
        assertDefaultWorkspace(workspace);
        return getCollections();
    }

    @Override
    public List<String> listWorkspaceIds() {
        return List.of(ObjectStore.DEFAULT_WORKSPACE);
    }

    @Override
    public ListenerList<ObjectStoreListener<CalendarCollection>> getObjectStoreListeners() {
        return null;
    }

    private void assertDefaultWorkspace(String workspace) throws ObjectStoreException {
        if (workspace != null && !ObjectStore.DEFAULT_WORKSPACE.equals(workspace)) {
            throw new ObjectStoreException("Google Calendar does not support workspaces other than '"
                    + ObjectStore.DEFAULT_WORKSPACE + "'");
        }
    }

    private static String extractTzId(net.fortuna.ical4j.model.Calendar timezone) {
        if (timezone == null) {
            return null;
        }
        var vtz = timezone.getComponent(Component.VTIMEZONE);
        if (vtz.isEmpty()) {
            return null;
        }
        return vtz.get().<TzId>getProperty(net.fortuna.ical4j.model.Property.TZID)
                .map(TzId::getValue).orElse(null);
    }
}

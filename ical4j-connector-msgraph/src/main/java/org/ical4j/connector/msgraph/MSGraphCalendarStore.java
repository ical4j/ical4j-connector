package org.ical4j.connector.msgraph;

import com.microsoft.graph.models.CalendarCollectionResponse;
import com.microsoft.graph.models.CalendarGroupCollectionResponse;
import com.microsoft.graph.models.Entity;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import org.ical4j.connector.event.ListenerList;
import org.ical4j.connector.event.ObjectStoreListener;

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
public class MSGraphCalendarStore extends AbstractMSGraphObjectStore implements ObjectStore<CalendarCollection> {

    public MSGraphCalendarStore(GraphServiceClient client) {
        super(client);
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
        com.microsoft.graph.models.Calendar calendar = new com.microsoft.graph.models.Calendar();
        calendar.setName(name);
        com.microsoft.graph.models.Calendar result = getClient().me().calendars().post(calendar);
        return new MSGraphCalendarCollection(this, result.getId());
    }

    @Override
    public CalendarCollection addCollection(String name, String workspace) throws ObjectStoreException {
        com.microsoft.graph.models.Calendar calendar = new com.microsoft.graph.models.Calendar();
        calendar.setName(name);
        com.microsoft.graph.models.Calendar result = getClient().me().calendarGroups().
                byCalendarGroupId(workspace).calendars().post(calendar);
        return new MSGraphCalendarCollection(this, result.getId(), workspace);
    }

    @Override
    public CalendarCollection addCollection(String id, String name, String description, String[] supportedComponents,
                                            Calendar timezone) throws ObjectStoreException {
        // A Graph Calendar resource only carries a name; id is server-generated and description,
        // supportedComponents and timezone have no equivalent field, so they are dropped.
        return addCollection(name);
    }

    @Override
    public CalendarCollection addCollection(String id, String name, String description, String[] supportedComponents,
                                            Calendar timezone, String workspace) throws ObjectStoreException {
        return addCollection(name, workspace);
    }

    @Override
    public CalendarCollection removeCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        getClient().me().calendars().byCalendarId(id).delete();
        return null;
    }

    @Override
    public CalendarCollection getCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        return new MSGraphCalendarCollection(this, id);
    }

    @Override
    public CalendarCollection getCollection(String id, String workspace) throws ObjectStoreException, ObjectNotFoundException {
        return new MSGraphCalendarCollection(this, id, workspace);
    }

    @Override
    public List<CalendarCollection> getCollections() throws ObjectStoreException, ObjectNotFoundException {
        CalendarCollectionResponse response = getClient().me().calendars().get();
        return Objects.requireNonNull(response.getValue()).stream().map(c ->
                new MSGraphCalendarCollection(this, c.getId())).collect(Collectors.toList());
    }

    @Override
    public List<CalendarCollection> getCollections(String workspace) throws ObjectStoreException, ObjectNotFoundException {
        CalendarCollectionResponse response = getClient().me().calendarGroups()
                .byCalendarGroupId(workspace).calendars().get();
        return Objects.requireNonNull(response.getValue()).stream().map(c ->
                new MSGraphCalendarCollection(this, c.getId(), workspace)).collect(Collectors.toList());
    }

    @Override
    public List<String> listWorkspaceIds() {
        CalendarGroupCollectionResponse response = getClient().me().calendarGroups().get();
        return Objects.requireNonNull(response.getValue()).stream().map(Entity::getId).collect(Collectors.toList());
    }

    @Override
    public ListenerList<ObjectStoreListener<CalendarCollection>> getObjectStoreListeners() {
        return null;
    }
}

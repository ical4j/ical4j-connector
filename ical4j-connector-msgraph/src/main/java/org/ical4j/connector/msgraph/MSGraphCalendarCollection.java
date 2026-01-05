package org.ical4j.connector.msgraph;

import com.microsoft.graph.models.Event;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.Uid;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.MediaType;
import org.ical4j.connector.ObjectStoreException;
import org.ical4j.connector.event.ListenerList;
import org.ical4j.connector.event.ObjectCollectionListener;

import java.io.IOException;
import java.time.Instant;
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

    private final com.microsoft.graph.models.Calendar calendar;

    public MSGraphCalendarCollection(com.microsoft.graph.models.Calendar calendar) {
        this.calendar = calendar;
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
        return calendar.getName();
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public List<String> listObjectUIDs() {
        return Objects.requireNonNull(calendar.getCalendarView()).stream()
                .map(Event::getICalUId).collect(Collectors.toList());
    }

    @Override
    public Optional<Calendar> get(String uid) {
        return Objects.requireNonNull(calendar.getEvents()).stream().filter(e -> Objects.equals(e.getICalUId(), uid)).findFirst().map(e -> {
            try {
                return new MSGraphCalendarBuilder().build(e);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public String add(Calendar object) throws ObjectStoreException {
        return "";
    }

    @Override
    public List<Calendar> removeAll(String... uid) throws FailedOperationException {
        return List.of();
    }

    @Override
    public void delete() throws ObjectStoreException {

    }

    @Override
    public ListenerList<ObjectCollectionListener<Calendar>> getObjectCollectionListeners() {
        return null;
    }
}

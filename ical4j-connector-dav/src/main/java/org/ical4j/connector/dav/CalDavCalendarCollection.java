/**
 * Copyright (c) 2012, Ben Fortuna
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
package org.ical4j.connector.dav;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.ical4j.connector.*;
import org.ical4j.connector.dav.property.CalDavPropertyName;
import org.ical4j.connector.dav.property.DavPropertyBuilder;
import org.ical4j.connector.dav.property.ICalPropertyName;
import org.ical4j.connector.dav.property.PropertyNameSets;
import org.ical4j.connector.dav.request.CalendarQuery;
import org.ical4j.connector.dav.request.EventQuery;
import org.ical4j.connector.dav.response.GetCalendarData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.jackrabbit.webdav.property.DavPropertyName.DISPLAYNAME;

/**
 * $Id$
 * 
 * Created on 24/02/2008
 * 
 * @author Ben
 * 
 */
public class CalDavCalendarCollection extends AbstractDavObjectCollection<Calendar> implements CalendarCollection {
    
    /**
     * Only {@link CalDavCalendarStore} should be calling this, so default modifier is applied.
     * 
     * @param calDavCalendarStore
     * @param id
     */
    CalDavCalendarCollection(CalDavCalendarStore calDavCalendarStore, String id) {
        this(calDavCalendarStore, id, id, "");
    }

    /**
     * Only {@link CalDavCalendarStore} should be calling this, so default modifier is applied.
     * 
     * @param calDavCalendarStore
     * @param id
     * @param displayName
     * @param description
     */
    CalDavCalendarCollection(CalDavCalendarStore calDavCalendarStore, String id, String displayName, String description) {

        super(calDavCalendarStore, id);
        properties.add(new DavPropertyBuilder<>().name(DISPLAYNAME).value(displayName).build());
        properties.add(new DavPropertyBuilder<>().name(CalDavPropertyName.CALENDAR_DESCRIPTION).value(description).build());
    }

    CalDavCalendarCollection(CalDavCalendarStore calDavCalendarStore, String id, DavPropertySet _properties) {
        this(calDavCalendarStore, id, null, null);
        this.properties = _properties;
    }

    @Override
    String getPath() {
        return getStore().pathResolver.getCalendarPath(getId(), getStore().getSessionConfiguration().getWorkspace());
    }

    /**
     * Creates this collection on the CalDAV server.
     * 
     * @throws IOException
     * @throws ObjectStoreException
     */
    final void create() throws IOException, ObjectStoreException {
        try {
            getStore().getClient().mkCalendar(getPath(), properties);
        } catch (DavException e) {
            throw new ObjectStoreException("Failed to create calendar collection", e);
        }
    }

    @Override
    public List<String> listObjectUIDs() {
        //TODO: extract UIDs from calendar objects..
        return null;
    }

    /**
     * @return an array of calendar objects
     * @deprecated Use the getEvents() method
     * @see ObjectCollection#getAll(String...) 
     */
    @Deprecated
    public Iterable<Calendar> getCalendars() {
        return getComponentsByType(Component.VEVENT);
    }

    /**
     * @return and array of calendar objects
     */
    public Iterable<Calendar> getEvents() {
        return getComponentsByType(Component.VEVENT);
    }

    /**
     * @return and array of calendar objects
     */
    public Iterable<Calendar> getTasks() {
        return getComponentsByType(Component.VTODO);
    }

    /**
     * @param componentType
     *            the type of component
     * @return and array of calendar objects
     */
    public Iterable<Calendar> getComponentsByType(String componentType) {
        try {
            var info = new ReportInfo(CalDavPropertyName.CALENDAR_QUERY, 1,
                    PropertyNameSets.REPORT_CALENDAR);
            info.setContentElement(new CalendarQuery(componentType).build());

            List<Calendar> calendars = getStore().getClient().report(getPath(), info, new GetCalendarData());
            return calendars;
        } catch (IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provides a human-readable description of the calendar collection.
     */
    public String getDescription() {
        try {
            return getProperty(CalDavPropertyName.CALENDAR_DESCRIPTION, String.class);
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Human-readable name of the collection.
     */
    public String getDisplayName() {
        try {
            return getProperty(DISPLAYNAME, String.class);
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provides a numeric value indicating the maximum number of ATTENDEE properties in any instance of a calendar
     * object resource stored in a calendar collection.
     */
    public Integer getMaxAttendeesPerInstance() {
        try {
            return getProperty(CalDavPropertyName.MAX_ATTENDEES_PER_INSTANCE, Integer.class);
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provides a DATE-TIME value indicating the latest date and time (in UTC) that the server is willing to accept for
     * any DATE or DATE-TIME value in a calendar object resource stored in a calendar collection.
     */
    public Instant getMaxDateTime() {
        try {
            return Instant.from(TemporalAdapter.parse(
                    getProperty(CalDavPropertyName.MAX_DATE_TIME, String.class)).getTemporal());
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provides a numeric value indicating the maximum number of recurrence instances that a calendar object resource
     * stored in a calendar collection can generate.
     */
    public Integer getMaxInstances() {
        try {
            return getProperty(CalDavPropertyName.MAX_INSTANCES, Integer.class);
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provides a numeric value indicating the maximum size of a resource in octets that the server is willing to accept
     * when a calendar object resource is stored in a calendar collection. 0 = no limits.
     */
    public long getMaxResourceSize() {
        try {
            var size = getProperty(CalDavPropertyName.MAX_RESOURCE_SIZE, Long.class);
            if (size != null) {
                return size;
            }
            return 0;
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provides a DATE-TIME value indicating the earliest date and time (in UTC) that the server is willing to accept
     * for any DATE or DATE-TIME value in a calendar object resource stored in a calendar collection.
     */
    public Instant getMinDateTime() {
        try {
            return Instant.from(TemporalAdapter.parse(
                getProperty(CalDavPropertyName.MIN_DATE_TIME, String.class)).getTemporal());
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the list of calendar components (VEVENT, VTODO, etc.) that this collection supports.
     */
    public String[] getSupportedComponentTypes() {
        List<String> supportedComponents = new ArrayList<>();

        ArrayList<Node> supportedCalCompSetProp;
        try {
            supportedCalCompSetProp = getProperty(CalDavPropertyName.SUPPORTED_CALENDAR_COMPONENT_SET, ArrayList.class);
            if (supportedCalCompSetProp != null) {
                for (var child : supportedCalCompSetProp) {
                    if (child instanceof Element) {
                        var nameNode = child.getAttributes().getNamedItem("name");
                        if (nameNode != null) {
                            supportedComponents.add(nameNode.getTextContent());
                        }
                    }
                }
            }
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }

        return supportedComponents.toArray(new String[0]);
    }

    /**
     * The CALDAV:calendar-timezone property is used to specify the time zone the server should rely on to resolve
     * "date" values and "date with local time" values (i.e., floating time) to "date with UTC time" values.
     */
    public Calendar getTimeZone() {
        try {
            String calTimezoneProp = getProperty(CalDavPropertyName.CALENDAR_TIMEZONE, String.class);

            if (calTimezoneProp != null) {
                var builder = new CalendarBuilder();
                return builder.build(new StringReader(calTimezoneProp));
            }
            return new Calendar();
        } catch (ObjectStoreException | IOException | DavException | ParserException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getColor() {
        try {
            return getProperty(ICalPropertyName.CALENDAR_COLOR, String.class);
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
    }
    
    public int getOrder() {
        try {
            return getProperty(ICalPropertyName.CALENDAR_ORDER, Integer.class);
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add a new calendar object in the collection. Creation will be done on the server right away.
     */
    @Override
    public String add(Calendar calendar) throws ObjectStoreException, ConstraintViolationException {
        writeCalendarOnServer(calendar, true);
        return calendar.getRequiredProperty(Property.UID).getValue();
    }

    /**
     * Stores the specified calendar in this collection, using the specified URI.
     * @param uri the URI (relative to this collection's path) where the calendar is to be stored
     * @param calendar a calendar object instance to be added to the collection
     * @throws ObjectStoreException when an unexpected error occurs (implementation-specific)
     */
    public void addCalendar(String uri, Calendar calendar) throws ObjectStoreException {
        writeCalendarOnServer(uri, calendar, true);
    }

    /**
     * Update a calendar object in the collection. Update will be send to the server right away.
     * @param calendar
     * @throws ObjectStoreException
     * @throws ConstraintViolationException
     */
    public void updateCalendar(Calendar calendar) throws ObjectStoreException, ConstraintViolationException {
        writeCalendarOnServer(calendar, false);
    }

    /**
     * Update a calendar object in the collection. Update will be send to the server right away.
     * @param calendar
     * @throws ObjectStoreException
     */
    public void updateCalendar(String uri, Calendar calendar) throws ObjectStoreException {
        writeCalendarOnServer(uri, calendar, false);
    }

    public void writeCalendarOnServer(Calendar calendar, boolean isNew) throws ObjectStoreException, ConstraintViolationException {
        var uid = Calendars.getUid(calendar);
        writeCalendarOnServer(defaultUriFromUid(uid.getValue()), calendar, isNew);
    }

    public void writeCalendarOnServer(String uri, Calendar calendar, boolean isNew) throws ObjectStoreException {
        var path = getPath();
        if (!path.endsWith("/")) {
            path = path.concat("/");
        }

        try {
            // TODO: get ETag and Schedule-Tag headers and store them locally
            getStore().getClient().put(path + uri, calendar, isNew ? null : "*");
        } catch (IOException | FailedOperationException e) {
            throw new ObjectStoreException("Error creating calendar on server", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Calendar> get(String uid) {
        try {
            return Optional.of(getCalendarFromUri(defaultUriFromUid(uid)));
        } catch (ObjectNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the calendar object located at the specified URI.
     * @param uri the URI (relative to this collection's path) where the calendar is to be found
     * @return a calendar object or null if no calendar exists under the specified URI
     */
    public Calendar getCalendarFromUri(String uri) throws ObjectNotFoundException {
        var path = getPath();
        if (!path.endsWith("/")) {
            path = path.concat("/");
        }
        try {
            return getStore().getClient().getCalendar(path + uri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Calendar> removeAll(String... uid) {
        List<Calendar> result = new ArrayList<>();
        for (var u : uid) {
            try {
                result.add(removeCalendarFromUri(defaultUriFromUid(u)));
            } catch (FailedOperationException | ObjectStoreException | ObjectNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * @param uri the URI (relative to this collection's path) where the calendar is to be found
     * @return the calendar that was successfully removed from the collection
     * @throws ObjectStoreException where an unexpected error occurs
     */
    public Calendar removeCalendarFromUri(String uri) throws FailedOperationException, ObjectStoreException, ObjectNotFoundException {
        var calendar = getCalendarFromUri(uri);
        try {
            getStore().getClient().delete(getPath() + "/" + uri);
        } catch (IOException | DavException e) {
            throw new ObjectStoreException(e);
        }
        return calendar;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Uid[] merge(Calendar calendar) throws FailedOperationException, ObjectStoreException {
        List<Uid> uids = new ArrayList<>();
        try {
            var uidCalendars = Calendars.split(calendar);
            for (Calendar uidCalendar : uidCalendars) {
                add(uidCalendar);
                uids.add(uidCalendar.getRequiredProperty(Property.UID));
            }
        } catch (ConstraintViolationException cve) {
            throw new FailedOperationException("Invalid calendar format", cve);
        }
        return uids.toArray(new Uid[0]);
    }

    /**
     * {@inheritDoc}
     */
    public Calendar export() {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<Calendar> getAll() throws ObjectStoreException {
        return getComponentsByType(Component.VEVENT);
    }

    /**
     * Get a list of calendar objects of VEVENT type for a specific time period.
     *
     * @param startTime
     * @param endTime
     * @return
     * @throws IOException
     * @throws DavException
     * @throws ParserConfigurationException
     * @throws ParserException
     */
    public List<Calendar> getEventsForTimePeriod(DateTime startTime, DateTime endTime)
            throws IOException, DavException, ParserConfigurationException, ParserException {

        return getStore().getClient().report(this.getPath(), new EventQuery(1)
                .withStartTime(startTime).withEndTime(endTime), new GetCalendarData());
    }

    /**
     * TODO: implement calendar-multiget to fetch objects based on href
     * @param hrefs
     * @param calData
     * @return
     * @throws IOException
     * @throws DavException
     * @throws ParserConfigurationException
     * @throws ParserException
     */
    public Calendar[] getObjectsByMultiget(ArrayList<URI> hrefs, Element calData)
            throws IOException, DavException, ParserConfigurationException, ParserException {
        return new Calendar[0];
    }
    
    /**
     * TODO: implement free-busy-query
     * @return
     */
    public Calendar[] doFreeBusyQuery() {
        return new Calendar[0];
    }

    @Override
    public String toString() {
        return "Display Name: " +  getDisplayName() + ", id: " + getId();
    }


    private String defaultUriFromUid(String uid) {
        return uid + ".ics";
    }
}

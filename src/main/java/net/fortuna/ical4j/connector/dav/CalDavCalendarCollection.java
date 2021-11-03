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
package net.fortuna.ical4j.connector.dav;

import net.fortuna.ical4j.connector.CalendarCollection;
import net.fortuna.ical4j.connector.FailedOperationException;
import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.method.GetMethod;
import net.fortuna.ical4j.connector.dav.method.MkCalendarMethod;
import net.fortuna.ical4j.connector.dav.method.PutMethod;
import net.fortuna.ical4j.connector.dav.method.ReportMethod;
import net.fortuna.ical4j.connector.dav.property.BaseDavPropertyName;
import net.fortuna.ical4j.connector.dav.property.CSDavPropertyName;
import net.fortuna.ical4j.connector.dav.property.CalDavPropertyName;
import net.fortuna.ical4j.connector.dav.property.ICalPropertyName;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;
import org.apache.http.HttpResponse;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.*;
import org.apache.jackrabbit.webdav.client.methods.HttpDelete;
import org.apache.jackrabbit.webdav.client.methods.XmlEntity;
import org.apache.jackrabbit.webdav.property.*;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
        this(calDavCalendarStore, id, null, null);
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
        properties.add(new DefaultDavProperty<>(DavPropertyName.DISPLAYNAME, displayName));
        properties.add(new DefaultDavProperty<>(CalDavPropertyName.CALENDAR_DESCRIPTION, description));
    }

    CalDavCalendarCollection(CalDavCalendarStore calDavCalendarStore, String id, DavPropertySet _properties) {
        this(calDavCalendarStore, id, null, null);
        this.properties = _properties;
    }

    /**
     * Creates this collection on the CalDAV server.
     * 
     * @throws IOException
     * @throws ObjectStoreException
     */
    final void create() throws IOException, ObjectStoreException {
        MkCalendarMethod mkCalendarMethod = new MkCalendarMethod(getPath());

        MkCalendar mkcalendar = new MkCalendar();
        mkcalendar.setProperties(properties);
        System.out.println("properties: " + properties.getContentSize());
        mkCalendarMethod.setEntity(XmlEntity.create(mkcalendar));

        HttpResponse httpResponse = getStore().getClient().execute(mkCalendarMethod);
        if (!mkCalendarMethod.succeeded(httpResponse)) {
            throw new ObjectStoreException(httpResponse.getStatusLine().getStatusCode() + ": " + httpResponse.getStatusLine().getReasonPhrase());
        }
    }

    /**
     * @return an array of calendar objects
     * @deprecated Use the getEvents() method
     * @see CalendarCollection#getComponents()
     */
    @Deprecated
    public Calendar[] getCalendars() {
        return getComponentsByType(Component.VEVENT);
    }

    /**
     * @return and array of calendar objects
     */
    public Calendar[] getEvents() {
        return getComponentsByType(Component.VEVENT);
    }

    /**
     * @return and array of calendar objects
     */
    public Calendar[] getTasks() {
        return getComponentsByType(Component.VTODO);
    }

    /**
     * @param componentType
     *            the type of component
     * @return and array of calendar objects
     */
    public Calendar[] getComponentsByType(String componentType) {
        try {
            DavPropertyNameSet properties = new DavPropertyNameSet();
            properties.add(DavPropertyName.GETETAG);
            properties.add(CalDavPropertyName.CALENDAR_DATA);

            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element filter = DomUtil.createElement(document, CalDavConstants.PROPERTY_FILTER,
                    CalDavConstants.CALDAV_NAMESPACE);
            Element calFilter = DomUtil.createElement(document, CalDavConstants.PROPERTY_COMP_FILTER,
                    CalDavConstants.CALDAV_NAMESPACE);
            calFilter.setAttribute(CalDavConstants.ATTRIBUTE_NAME, Calendar.VCALENDAR);
            Element eventFilter = DomUtil.createElement(document, CalDavConstants.PROPERTY_COMP_FILTER,
                    CalDavConstants.CALDAV_NAMESPACE);
            eventFilter.setAttribute(CalDavConstants.ATTRIBUTE_NAME, componentType);
            calFilter.appendChild(eventFilter);
            filter.appendChild(calFilter);

            ReportInfo info = new ReportInfo(ReportMethod.CALENDAR_QUERY, 1, properties);
            info.setContentElement(filter);

            ReportMethod method = new ReportMethod(getPath(), info);
            HttpResponse httpResponse = getStore().getClient().execute(method);
            if (httpResponse.getStatusLine().getStatusCode() == DavServletResponse.SC_MULTI_STATUS) {
                return method.getCalendars(httpResponse);
            } else {
                return new Calendar[0];
            }
        } catch (DavException | IOException | ParserConfigurationException | ParserException e) {
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
            return getProperty(DavPropertyName.DISPLAYNAME, String.class);
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
    public String getMaxDateTime() {
        try {
            return getProperty(CalDavPropertyName.MAX_DATE_TIME, String.class);
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
            Long size = getProperty(CalDavPropertyName.MAX_RESOURCE_SIZE, Long.class);
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
    public String getMinDateTime() {
        try {
            return getProperty(CalDavPropertyName.MIN_DATE_TIME, String.class);
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the list of calendar components (VEVENT, VTODO, etc.) that this collection supports.
     */
    public String[] getSupportedComponentTypes() {
        List<String> supportedComponents = new ArrayList<String>();

        ArrayList<Node> supportedCalCompSetProp;
        try {
            supportedCalCompSetProp = getProperty(CalDavPropertyName.SUPPORTED_CALENDAR_COMPONENT_SET, ArrayList.class);
            if (supportedCalCompSetProp != null) {
                for (Node child : supportedCalCompSetProp) {
                    if (child instanceof Element) {
                        Node nameNode = child.getAttributes().getNamedItem("name");
                        if (nameNode != null) {
                            supportedComponents.add(nameNode.getTextContent());
                        }
                    }
                }
            }
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }

        return supportedComponents.toArray(new String[supportedComponents.size()]);
    }

    /**
     * The CALDAV:calendar-timezone property is used to specify the time zone the server should rely on to resolve
     * "date" values and "date with local time" values (i.e., floating time) to "date with UTC time" values.
     */
    public Calendar getTimeZone() {
        try {
            String calTimezoneProp = getProperty(CalDavPropertyName.CALENDAR_TIMEZONE, String.class);

            if (calTimezoneProp != null) {
                CalendarBuilder builder = new CalendarBuilder();
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
    public void addCalendar(Calendar calendar) throws ObjectStoreException, ConstraintViolationException {
        writeCalendarOnServer(calendar, true);
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
        Uid uid = Calendars.getUid(calendar);
        writeCalendarOnServer(defaultUriFromUid(uid.getValue()), calendar, isNew);
    }

    public void writeCalendarOnServer(String uri, Calendar calendar, boolean isNew) throws ObjectStoreException {
        String path = getPath();
        if (!path.endsWith("/")) {
            path = path.concat("/");
        }
        PutMethod putMethod = new PutMethod(path + uri);
        // putMethod.setAllEtags(true);
        if (isNew) {
            putMethod.addHeader("If-None-Match", "*");
        } else {
            putMethod.addHeader("If-Match", "*");
        }
        // putMethod.setRequestBody(calendar);

        try {
            putMethod.setCalendar(calendar);
        } catch (Exception e) {
            throw new ObjectStoreException("Invalid calendar", e);
        }

        try {
            // TODO: get ETag and Schedule-Tag headers and store them locally
            HttpResponse httpResponse = getStore().getClient().execute(putMethod);
            if ((httpResponse.getStatusLine().getStatusCode() != DavServletResponse.SC_CREATED)
                    && (httpResponse.getStatusLine().getStatusCode() != DavServletResponse.SC_NO_CONTENT)) {
                throw new ObjectStoreException("Error creating calendar on server: " + httpResponse.getStatusLine());
            }
        } catch (IOException ioe) {
            throw new ObjectStoreException("Error creating calendar on server", ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Calendar getCalendar(String uid) throws ObjectNotFoundException {
        return getCalendarFromUri(defaultUriFromUid(uid));
    }

    /**
     * Returns the calendar object located at the specified URI.
     * @param uri the URI (relative to this collection's path) where the calendar is to be found
     * @return a calendar object or null if no calendar exists under the specified URI
     */
    public Calendar getCalendarFromUri(String uri) throws ObjectNotFoundException {
        String path = getPath();
        if (!path.endsWith("/")) {
            path = path.concat("/");
        }
        GetMethod method = new GetMethod(path + uri);
        HttpResponse httpResponse;
        try {
            httpResponse = getStore().getClient().execute(method);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (httpResponse.getStatusLine().getStatusCode() == DavServletResponse.SC_OK) {
            try {
                return method.getCalendar(httpResponse);
            } catch (IOException | ParserException e) {
                throw new RuntimeException(e);
            }
        } else if (httpResponse.getStatusLine().getStatusCode() == DavServletResponse.SC_NOT_FOUND) {
            throw new ObjectNotFoundException(String.format("Calendar not found: %s", uri));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Calendar removeCalendar(String uid) throws FailedOperationException, ObjectStoreException, ObjectNotFoundException {
        return removeCalendarFromUri(defaultUriFromUid(uid));
    }

    /**
     * @param uri the URI (relative to this collection's path) where the calendar is to be found
     * @return the calendar that was successfully removed from the collection
     * @throws ObjectStoreException where an unexpected error occurs
     */
    public Calendar removeCalendarFromUri(String uri) throws FailedOperationException, ObjectStoreException, ObjectNotFoundException {
        Calendar calendar = getCalendarFromUri(uri);

        HttpDelete deleteMethod = new HttpDelete(getPath() + "/" + uri);
        HttpResponse httpResponse;
        try {
            httpResponse = getStore().getClient().execute(deleteMethod);
        } catch (IOException e) {
            throw new ObjectStoreException(e);
        }
        if (!deleteMethod.succeeded(httpResponse)) {
            throw new FailedOperationException(httpResponse.getStatusLine().toString());
        }

        return calendar;
    }

    /**
     * {@inheritDoc}
     */
    public final void merge(Calendar calendar) throws FailedOperationException, ObjectStoreException {
        try {
            Calendar[] uidCalendars = Calendars.split(calendar);
            for (int i = 0; i < uidCalendars.length; i++) {
                addCalendar(uidCalendars[i]);
            }
        } catch (ConstraintViolationException cve) {
            throw new FailedOperationException("Invalid calendar format", cve);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Calendar export() throws ObjectStoreException {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * {@inheritDoc}
     */
    public Calendar[] getComponents() throws ObjectStoreException {
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
    public Calendar[] getEventsForTimePeriod(DateTime startTime, DateTime endTime)
            throws IOException, DavException, ParserConfigurationException, ParserException {

        DocumentBuilderFactory BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        BUILDER_FACTORY.setNamespaceAware(true);
        BUILDER_FACTORY.setIgnoringComments(true);
        BUILDER_FACTORY.setIgnoringElementContentWhitespace(true);
        BUILDER_FACTORY.setCoalescing(true);

        Document document = BUILDER_FACTORY.newDocumentBuilder().newDocument();

        org.w3c.dom.Element calData = DomUtil.createElement(document, CalDavConstants.PROPERTY_CALENDAR_DATA,
                CalDavConstants.CALDAV_NAMESPACE);
        
        org.w3c.dom.Element calFilter = DomUtil.createElement(document, CalDavConstants.PROPERTY_COMP_FILTER,
                CalDavConstants.CALDAV_NAMESPACE);
        calFilter.setAttribute(CalDavConstants.ATTRIBUTE_NAME, Calendar.VCALENDAR);
        org.w3c.dom.Element eventFilter = DomUtil.createElement(document, CalDavConstants.PROPERTY_COMP_FILTER,
                CalDavConstants.CALDAV_NAMESPACE);
        eventFilter.setAttribute(CalDavConstants.ATTRIBUTE_NAME, Component.VEVENT);
        org.w3c.dom.Element timeRange = DomUtil.createElement(document, CalDavConstants.PROPERTY_TIME_RANGE,
                CalDavConstants.CALDAV_NAMESPACE);

        timeRange.setAttribute(CalDavConstants.ATTRIBUTE_START, startTime.toString());
        timeRange.setAttribute(CalDavConstants.ATTRIBUTE_END, endTime.toString());
        eventFilter.appendChild(timeRange);
        calFilter.appendChild(eventFilter);

        return getObjectsByFilter(calFilter, calData);
    }
    
    /**
     * Returns a list of calendar objects by calling a REPORT method with a filter. You must pass
     * a XML element as the argument, the element must contain the filter. For example, if you wish 
     * to filter objects to only get VEVENT objects you can build a filter like this:
     * 
     * org.w3c.dom.Element calData = DomUtil.createElement(document, CalDavConstants.PROPERTY_CALENDAR_DATA,
                CalDavConstants.CALDAV_NAMESPACE);
     * 
     * org.w3c.dom.Element calFilter = DomUtil.createElement(document, CalDavConstants.PROPERTY_COMP_FILTER,
                CalDavConstants.CALDAV_NAMESPACE);
     * calFilter.setAttribute(CalDavConstants.ATTRIBUTE_NAME, Calendar.VCALENDAR);
     * org.w3c.dom.Element eventFilter = DomUtil.createElement(document, CalDavConstants.PROPERTY_COMP_FILTER,
                CalDavConstants.CALDAV_NAMESPACE);
     * eventFilter.setAttribute(CalDavConstants.ATTRIBUTE_NAME, Component.VEVENT);
     * calFilter.appendChild(eventFilter);
     * collection.getObjectsByFilter(calFilter);
     * 
     * Check the examples in rfc4791
     * 
     * @param filter
     * @return
     * @throws IOException
     * @throws DavException
     * @throws ParserConfigurationException
     * @throws ParserException
     */
    public Calendar[] getObjectsByFilter(org.w3c.dom.Element filter, org.w3c.dom.Element calData)
            throws IOException, DavException, ParserConfigurationException, ParserException {
        ArrayList<Calendar> events = new ArrayList<Calendar>();

        ReportInfo rinfo = new ReportInfo(ReportType.register(CalDavConstants.PROPERTY_CALENDAR_QUERY,
                CalDavConstants.CALDAV_NAMESPACE,
                org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport.class), 1);

        DocumentBuilderFactory BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        BUILDER_FACTORY.setNamespaceAware(true);
        BUILDER_FACTORY.setIgnoringComments(true);
        BUILDER_FACTORY.setIgnoringElementContentWhitespace(true);
        BUILDER_FACTORY.setCoalescing(true);

        Document document = BUILDER_FACTORY.newDocumentBuilder().newDocument();
        org.w3c.dom.Element property = DomUtil
                .createElement(document, DavConstants.XML_PROP, CalDavConstants.NAMESPACE);
        property.appendChild(DomUtil.createElement(document, DavConstants.PROPERTY_GETETAG, CalDavConstants.NAMESPACE));

        Node importedCalData = document.importNode(calData, true);
        property.appendChild(importedCalData);

        document.appendChild(property);
        rinfo.setContentElement(property);

        org.w3c.dom.Element parentFilter = DomUtil.createElement(document, CalDavConstants.PROPERTY_FILTER,
                CalDavConstants.CALDAV_NAMESPACE);
        rinfo.setContentElement(parentFilter);

        Node importedFilter = document.importNode(filter, true);
        parentFilter.appendChild(importedFilter);

        ReportMethod method = new ReportMethod(this.getPath(), rinfo);
        HttpResponse httpResponse = this.getStore().getClient().execute(method);
        MultiStatus multiStatus = method.getResponseBodyAsMultiStatus(httpResponse);
        MultiStatusResponse[] responses = multiStatus.getResponses();
        for (int i = 0; i < responses.length; i++) {
            for (int j = 0; j < responses[i].getStatus().length; j++) {
                Status status = responses[i].getStatus()[j];
                for (DavPropertyIterator iNames = responses[i].getProperties(status.getStatusCode()).iterator(); iNames
                        .hasNext();) {
                    DavProperty name = iNames.nextProperty();
                    if (name.getValue() instanceof String) {
                        if ((name.getName().getNamespace().equals(CalDavConstants.CALDAV_NAMESPACE))
                                && (name.getName().getName().equals(CalDavConstants.PROPERTY_CALENDAR_DATA))) {
                            StringReader sin = new StringReader((String) name.getValue());
                            CalendarBuilder builder = new CalendarBuilder();
                            Calendar calendar = builder.build(sin);
                            events.add(calendar);
                        }
                    }
                }
            }
        }
        return events.toArray(new Calendar[events.size()]);
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
    public Calendar[] getObjectsByMultiget(ArrayList<URI> hrefs, org.w3c.dom.Element calData)
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
    
    public static final DavPropertyNameSet propertiesForFetch() {
        DavPropertyNameSet principalsProps = new DavPropertyNameSet();

        principalsProps.add(BaseDavPropertyName.QUOTA_AVAILABLE_BYTES);
        principalsProps.add(BaseDavPropertyName.QUOTA_USED_BYTES);
        principalsProps.add(BaseDavPropertyName.CURRENT_USER_PRIVILEGE_SET);
        principalsProps.add(BaseDavPropertyName.PROP);
        principalsProps.add(BaseDavPropertyName.RESOURCETYPE);
        principalsProps.add(DavPropertyName.DISPLAYNAME);
        principalsProps.add(SecurityConstants.OWNER);

        principalsProps.add(CalDavPropertyName.CALENDAR_DESCRIPTION);
        principalsProps.add(CalDavPropertyName.SUPPORTED_CALENDAR_COMPONENT_SET);
        principalsProps.add(CalDavPropertyName.FREE_BUSY_SET);
        principalsProps.add(CalDavPropertyName.SCHEDULE_CALENDAR_TRANSP);
        principalsProps.add(CalDavPropertyName.SCHEDULE_DEFAULT_CALENDAR_URL);
        principalsProps.add(CalDavPropertyName.CALENDAR_TIMEZONE);
        principalsProps.add(CalDavPropertyName.SUPPORTED_CALENDAR_DATA);
        principalsProps.add(CalDavPropertyName.MAX_ATTENDEES_PER_INSTANCE);
        principalsProps.add(CalDavPropertyName.MAX_DATE_TIME);
        principalsProps.add(CalDavPropertyName.MIN_DATE_TIME);
        principalsProps.add(CalDavPropertyName.MAX_INSTANCES);
        principalsProps.add(CalDavPropertyName.MAX_RESOURCE_SIZE);

        principalsProps.add(CSDavPropertyName.XMPP_SERVER);
        principalsProps.add(CSDavPropertyName.XMPP_URI);
        principalsProps.add(CSDavPropertyName.CTAG);
        principalsProps.add(CSDavPropertyName.SOURCE);
        principalsProps.add(CSDavPropertyName.SUBSCRIBED_STRIP_ALARMS);
        principalsProps.add(CSDavPropertyName.SUBSCRIBED_STRIP_ATTACHMENTS);
        principalsProps.add(CSDavPropertyName.SUBSCRIBED_STRIP_TODOS);
        principalsProps.add(CSDavPropertyName.REFRESHRATE);
        principalsProps.add(CSDavPropertyName.PUSH_TRANSPORTS);
        principalsProps.add(CSDavPropertyName.PUSHKEY);

        principalsProps.add(ICalPropertyName.CALENDAR_COLOR);
        principalsProps.add(ICalPropertyName.CALENDAR_ORDER);
        
        return principalsProps;
    }
    
    @Override
    public String toString() {
        return "Display Name: " +  getDisplayName() + ", id: " + getId();
    }


    private String defaultUriFromUid(String uid) {
        return uid + ".ics";
    }
}

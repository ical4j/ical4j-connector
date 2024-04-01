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

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableMethod;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import net.fortuna.ical4j.util.FixedUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.CalendarStore;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;
import org.ical4j.connector.dav.method.PrincipalPropertySearchInfo;
import org.ical4j.connector.dav.property.BaseDavPropertyName;
import org.ical4j.connector.dav.property.CalDavPropertyName;
import org.ical4j.connector.dav.property.PropertyNameSets;
import org.ical4j.connector.dav.request.ExpandPropertyQuery;
import org.ical4j.connector.dav.response.GetCalDavCollections;
import org.ical4j.connector.dav.response.GetCollections;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.ical4j.connector.dav.ResourceType.*;

/**
 * $Id$
 * 
 * Created on 24/02/2008
 * 
 * @author Ben
 * 
 */
public final class CalDavCalendarStore extends AbstractDavObjectStore<Calendar, CalDavCalendarCollection> implements
        CalendarStore<CalDavCalendarCollection> {

    private final String prodId;
    private String displayName;

    /**
     * @param prodId application product identifier
     * @param url the URL of a CalDAV server instance
     * @param pathResolver the path resolver for the CalDAV server type
     */
    public CalDavCalendarStore(String prodId, URL url, PathResolver pathResolver) {
        super(url, pathResolver);
        this.prodId = prodId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CalDavCalendarCollection addCollection(String id) throws ObjectStoreException {
        CalDavCalendarCollection collection = new CalDavCalendarCollection(this, id);
        try {
            collection.create();
        } catch (IOException e) {
            throw new ObjectStoreException(String.format("unable to add collection '%s'", id), e);
        }
        return collection;
    }

    @Override
    public CalDavCalendarCollection addCollection(String id, String workspace) throws ObjectStoreException {
        throw new UnsupportedOperationException("Workspaces not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CalDavCalendarCollection addCollection(String id, String displayName, String description,
            String[] supportedComponents, Calendar timezone) throws ObjectStoreException {

        CalDavCalendarCollection collection = new CalDavCalendarCollection(this, id, displayName, description);
        try {
            collection.create();
        } catch (IOException e) {
            throw new ObjectStoreException(String.format("unable to add collection '%s'", id), e);
        }
        return collection;
    }

    @Override
    public CalDavCalendarCollection addCollection(String id, String displayName, String description, String[] supportedComponents, Calendar timezone, String workspace) throws ObjectStoreException {
        throw new UnsupportedOperationException("Workspaces not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    public CalDavCalendarCollection addCollection(String id, DavPropertySet properties) throws ObjectStoreException {
        CalDavCalendarCollection collection = new CalDavCalendarCollection(this, id, properties);
        try {
            collection.create();
        } catch (IOException e) {
            throw new ObjectStoreException(String.format("unable to add collection '%s'", id), e);
        }
        return collection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CalDavCalendarCollection getCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        try {
            String resourcePath = pathResolver.getCalendarPath(id, "test");
            Map<String, DavPropertySet> res = getClient().propFind(resourcePath,
                    PropertyNameSets.PROPFIND_CALENDAR,
                    new GetCollections(CALENDAR, CALENDAR_PROXY_READ, CALENDAR_PROXY_WRITE));
            if (!res.isEmpty()) {
                DavPropertySet props = res.entrySet().iterator().next().getValue();
                return new CalDavCalendarCollection(this, id, props);
//            .entrySet().stream()
//                    .map(e -> new CalDavCalendarCollection(this, e.getKey(), e.getValue()))
//                    .collect(Collectors.toList()).get(0);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new ObjectStoreException(String.format("unable to get collection '%s'", id), e);
        }
    }

    @Override
    public CalDavCalendarCollection getCollection(String id, String workspace) throws ObjectStoreException, ObjectNotFoundException {
        throw new UnsupportedOperationException("Workspaces not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    public CalendarCollection merge(String id, CalendarCollection calendar) {
        throw new UnsupportedOperationException("not implemented");
    }

    public String findCalendarHomeSet() throws ParserConfigurationException, IOException, DavException {
        String propfindPath = pathResolver.getPrincipalPath(getSessionConfiguration().getUser());
        return findCalendarHomeSet(propfindPath);
    }

    /**
     * This method try to find the calendar-home-set attribute in the user's DAV principals. The calendar-home-set
     * attribute is the URI of the main collection of calendars for the user.
     * 
     * @return the URI for the main calendar collection
     * @author Pascal Robert
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws DavException
     */
    protected String findCalendarHomeSet(String propfindUri) throws IOException {
        DavPropertySet props = getClient().propFind(propfindUri, PropertyNameSets.PROPFIND_CALENDAR_HOME);
        if (props.contains(CalDavPropertyName.CALENDAR_HOME_SET)) {
            Element propElement = (Element) props.get(CalDavPropertyName.CALENDAR_HOME_SET).getValue();
            return propElement.getFirstChild().getNodeValue();
        } else {
            throw new RuntimeException("Property not found");
        }
    }

    /**
     * This method will try to find all calendar collections available at the calendar-home-set URI of the user.
     * 
     * @return An array of all available calendar collections
     * @author Pascal Robert
     * @throws ParserConfigurationException where the parse is not configured correctly
     * @throws IOException where a communications error occurs
     * @throws DavException where an error occurs calling the DAV method
     */
    @Override
    public List<CalDavCalendarCollection> getCollections() throws ObjectStoreException, ObjectNotFoundException {
        try {
            String calHomeSetPath = findCalendarHomeSet();
            if (calHomeSetPath == null) {
                throw new ObjectNotFoundException("No calendar-home-set attribute found for the user");
            }
            return getCollectionsForHomeSet(this, calHomeSetPath);
        } catch (DavException | IOException | ParserConfigurationException de) {
            throw new ObjectStoreException(de);
        }
    }

    @Override
    public List<CalDavCalendarCollection> getCollections(String workspace) throws ObjectStoreException, ObjectNotFoundException {
        throw new UnsupportedOperationException("Workspaces not yet implemented");
    }

    protected List<CalDavCalendarCollection> getCollectionsForHomeSet(CalDavCalendarStore store,
                                                                      String urlForcalendarHomeSet) throws IOException, DavException {

        return getClient().propFind(urlForcalendarHomeSet, PropertyNameSets.PROPFIND_CALENDAR,
                        new GetCollections(COLLECTION)).entrySet().stream()
                .map(e -> new CalDavCalendarCollection(this, e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Get the list of available delegated collections, Apple's iCal style
     * 
     * @return
     * @throws Exception
     */
    public List<CalDavCalendarCollection> getDelegatedCollections() throws Exception {
        List<CalDavCalendarCollection> collections = new ArrayList<CalDavCalendarCollection>();
        collections.addAll(getWriteDelegatedCollections());
        collections.addAll(getReadOnlyDelegatedCollections());
        return collections;
    }
    
    protected List<CalDavCalendarCollection> getDelegatedCollections(ExpandPropertyQuery.Type type) throws Exception {

        String methodUri = this.pathResolver.getPrincipalPath(getSessionConfiguration().getUser());

        ExpandPropertyQuery expandPropertyReport = new ExpandPropertyQuery(type)
                .withPropertyName(DavPropertyName.DISPLAYNAME)
                .withPropertyName(SecurityConstants.PRINCIPAL_URL)
                .withPropertyName(CalDavPropertyName.USER_ADDRESS_SET);

        ReportInfo rinfo = new ReportInfo(BaseDavPropertyName.EXPAND_PROPERTY, 0);
        rinfo.setContentElement(expandPropertyReport.build());

        return getClient().report(methodUri, rinfo, new GetCalDavCollections());
    }
    
    public List<CalDavCalendarCollection> getWriteDelegatedCollections() throws Exception {
        List<CalDavCalendarCollection> collections = getDelegatedCollections(ExpandPropertyQuery.Type.PROXY_WRITE_FOR);
        return collections;
    }
    
    public List<CalDavCalendarCollection> getReadOnlyDelegatedCollections() throws Exception {
        List<CalDavCalendarCollection> collections = getDelegatedCollections(ExpandPropertyQuery.Type.PROXY_READ_FOR);
        return collections;
    }

    /**
     * {@inheritDoc}
     */
    public CalDavCalendarCollection removeCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        CalDavCalendarCollection collection = getCollection(id);
        collection.delete();
        return collection;
    }

    @Override
    public List<String> listWorkspaces() {
        throw new UnsupportedOperationException("Workspaces not yet implemented");
    }

    // public CalendarCollection replace(String id, CalendarCollection calendar) {
    // // TODO Auto-generated method stub
    // return null;
    // }

    /**
     * @return the prodId
     */
    final String getProdId() {
        return prodId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String findScheduleOutbox() throws ParserConfigurationException, IOException, DavException {
        return findInboxOrOutbox(CalDavPropertyName.SCHEDULE_OUTBOX_URL);
    }

    public String findScheduleInbox() throws ParserConfigurationException, IOException, DavException {
        return findInboxOrOutbox(CalDavPropertyName.SCHEDULE_INBOX_URL);
    }

    protected String findInboxOrOutbox(DavPropertyName type) throws ParserConfigurationException, IOException, DavException {
        String propfindUri = pathResolver.getPrincipalPath(getSessionConfiguration().getUser());

        DavPropertyNameSet principalsProps = new DavPropertyNameSet();
        principalsProps.add(type);

        DavPropertySet props = getClient().propFind(propfindUri, principalsProps);
        return (String) props.get(type).getValue();
    }

    /**
     * This method will return free-busy information for each attendee. If the free-busy information can't be retrieve
     * (for example, users on an foreign server), check the isSuccess method to see if free-busy lookup was successful.
     *
     * @author probert
     */
    public List<ScheduleResponse> findFreeBusyInfoForAttendees(Organizer organizer, ArrayList<Attendee> attendees,
            DtStart startTime, DtEnd endTime) throws ParserConfigurationException, IOException, DavException,
            ParseException, ParserException, SAXException {
        Random ramdomizer = new Random();
        ArrayList<ScheduleResponse> responses = new ArrayList<ScheduleResponse>();

        Calendar calendar = new Calendar();
        calendar.add(new ProdId(getProdId()));
        calendar.add(ImmutableVersion.VERSION_2_0);
        calendar.add(ImmutableCalScale.GREGORIAN);
        calendar.add(ImmutableMethod.REQUEST);

        VFreeBusy fbComponent = new VFreeBusy();

        fbComponent.add(organizer);

        fbComponent.add(startTime);
        fbComponent.add(endTime);

        String strAttendee = "";
        if (attendees != null) {
            for (Iterator<Attendee> itrAttendee = attendees.iterator(); itrAttendee.hasNext();) {
                Attendee attendee = itrAttendee.next();
                fbComponent.add(attendee);
                strAttendee += attendee.getValue() + ",";
            }
            strAttendee = strAttendee.substring(0, strAttendee.length() - 1);
        }

        UidGenerator ug = new FixedUidGenerator(ramdomizer.nextInt() + "");
        fbComponent.add(ug.generateUid());
        calendar.getComponents().add(fbComponent);

        return getClient().freeBusy(findScheduleOutbox(), calendar, organizer);
    }

    public List<Attendee> getIndividuals(String nameToSearch) throws ParserConfigurationException, IOException, DavException, URISyntaxException {
        return getUserTypes(CuType.INDIVIDUAL, nameToSearch);
    }
    
    public List<Attendee> getRooms(String nameToSearch) throws ParserConfigurationException, IOException, DavException, URISyntaxException {
        return getUserTypes(CuType.ROOM, nameToSearch);
    }
    
    public List<Attendee> getAllRooms() throws ParserConfigurationException, IOException, DavException, URISyntaxException {
        return getAllPrincipalsForType(CuType.ROOM);
    }
    
    public List<Attendee> getAllResources() throws ParserConfigurationException, IOException, DavException, URISyntaxException {
        return getAllPrincipalsForType(CuType.RESOURCE);
    }

    public List<Attendee> getAllPrincipalsForType(CuType type) throws ParserConfigurationException, IOException, DavException, URISyntaxException {
        return executePrincipalPropSearch(new org.ical4j.connector.dav.request.PrincipalPropertySearch(type).build());
    }
    
    /**
     * Use this method to search for resources (individual, group, resource, room). 
     * For example, if you want to find all rooms that begins
     * with "Room" in their email or email address, call this method with:
     * 
     *   getUserTypes(CuType.ROOM, "Room");
     * 
     * If nameToSearch is null, it will find all resources for the desired type.
     * 
     * @param type
     * @param nameToSearch
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws DavException
     * @throws URISyntaxException 
     */
    protected List<Attendee> getUserTypes(CuType type, String nameToSearch) throws ParserConfigurationException, IOException, DavException, URISyntaxException {
        return executePrincipalPropSearch(new org.ical4j.connector.dav.request.PrincipalPropertySearch(type, nameToSearch).build());
    }
    
    protected List<Attendee> executePrincipalPropSearch(Element principalPropSearch) throws DavException, IOException, URISyntaxException {        
        PrincipalPropertySearchInfo rinfo = new PrincipalPropertySearchInfo(principalPropSearch, 0);
        String methodUri = this.pathResolver.getPrincipalPath(getSessionConfiguration().getUser());
        return getClient().findPrincipals(methodUri, rinfo);
    }
}

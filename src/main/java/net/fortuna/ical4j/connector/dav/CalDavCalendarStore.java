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
import net.fortuna.ical4j.connector.CalendarStore;
import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.method.PrincipalPropertySearchInfo;
import net.fortuna.ical4j.connector.dav.method.PrincipalPropertySearchMethod;
import net.fortuna.ical4j.connector.dav.property.CalDavPropertyName;
import net.fortuna.ical4j.connector.dav.request.PrincipalPropertySearch;
import net.fortuna.ical4j.connector.dav.request.XmlSupport;
import net.fortuna.ical4j.connector.dav.response.PropFindResponseHandler;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.FixedUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.client.methods.HttpReport;
import org.apache.jackrabbit.webdav.property.*;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static net.fortuna.ical4j.connector.dav.enums.ResourceType.*;

/**
 * $Id$
 * 
 * Created on 24/02/2008
 * 
 * @author Ben
 * 
 */
public final class CalDavCalendarStore extends AbstractDavObjectStore<CalDavCalendarCollection> implements
        CalendarStore<CalDavCalendarCollection>, XmlSupport {

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
    public CalDavCalendarCollection addCollection(String id) throws ObjectStoreException {
        CalDavCalendarCollection collection = new CalDavCalendarCollection(this, id);
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
    public CalDavCalendarCollection getCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        try {
            DavPropertyNameSet principalsProps = CalDavCalendarCollection.propertiesForFetch();
            HttpPropfind getMethod = new HttpPropfind(id, principalsProps, 0);

            PropFindResponseHandler responseHandler = new PropFindResponseHandler(getMethod);
            responseHandler.accept(this.getClient().execute(getMethod));
            if (!responseHandler.exists()) {
                throw new ObjectNotFoundException();
            }
            return responseHandler.getCollections(
                    Arrays.asList(CALENDAR, CALENDAR_PROXY_READ, CALENDAR_PROXY_WRITE)).entrySet().stream()
                    .map(e -> new CalDavCalendarCollection(this, e.getKey(), e.getValue()))
                    .collect(Collectors.toList()).get(0);
        } catch (IOException | DavException e) {
            throw new ObjectStoreException(String.format("unable to get collection '%s'", id), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public CalendarCollection merge(String id, CalendarCollection calendar) {
        throw new UnsupportedOperationException("not implemented");
    }

    public String findCalendarHomeSet() throws ParserConfigurationException, IOException, DavException {
        String propfindUri = getHostURL() + pathResolver.getPrincipalPath(getUserName());
        return findCalendarHomeSet(propfindUri);
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
    protected String findCalendarHomeSet(String propfindUri) throws IOException, DavException {
        DavPropertyNameSet principalsProps = new DavPropertyNameSet();
        principalsProps.add(CalDavPropertyName.CALENDAR_HOME_SET);
        principalsProps.add(DavPropertyName.DISPLAYNAME);

        HttpPropfind method = new HttpPropfind(propfindUri, principalsProps, 0);
        PropFindResponseHandler responseHandler = new PropFindResponseHandler(method);
        responseHandler.accept(getClient().execute(method));
        return responseHandler.getDavPropertyUri(CalDavPropertyName.CALENDAR_HOME_SET);
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
    public List<CalDavCalendarCollection> getCollections() throws ObjectStoreException, ObjectNotFoundException {
        try {
            String calHomeSetUri = findCalendarHomeSet();
            if (calHomeSetUri == null) {
                throw new ObjectNotFoundException("No calendar-home-set attribute found for the user");
            }
            String urlForcalendarHomeSet = getHostURL() + calHomeSetUri;
            return getCollectionsForHomeSet(this, urlForcalendarHomeSet);
        } catch (DavException de) {
            throw new ObjectStoreException(de);
        } catch (IOException ioe) {
            throw new ObjectStoreException(ioe);
        } catch (ParserConfigurationException pce) {
            throw new ObjectStoreException(pce);
        }
    }
    
    protected List<CalDavCalendarCollection> getCollectionsForHomeSet(CalDavCalendarStore store,
            String urlForcalendarHomeSet) throws IOException, DavException {
        List<CalDavCalendarCollection> collections = new ArrayList<CalDavCalendarCollection>();

        DavPropertyNameSet principalsProps = CalDavCalendarCollection.propertiesForFetch();

        HttpPropfind method = new HttpPropfind(urlForcalendarHomeSet, principalsProps, 1);
        PropFindResponseHandler responseHandler = new PropFindResponseHandler(method);
        responseHandler.accept(getClient().execute(method));

        return responseHandler.getCollections(
                Arrays.asList(CALENDAR, CALENDAR_PROXY_READ, CALENDAR_PROXY_WRITE)).entrySet().stream()
                .map(e -> new CalDavCalendarCollection(this, e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
	protected List<CalDavCalendarCollection> getDelegateCollections(DavProperty<?> proxyDavProperty)
            throws ParserConfigurationException, IOException, DavException {
        
        ArrayList<CalDavCalendarCollection> delegatedCollections = new ArrayList<CalDavCalendarCollection>();
        
        /*
         * Zimbra check: Zimbra advertise calendar-proxy, but it will return 404 in propstat if Enable delegation for
         * Apple iCal CalDAV client is not enabled
         */
        if (proxyDavProperty != null) {
            Object propertyValue = proxyDavProperty.getValue();
            ArrayList<Node> response;

            if (propertyValue instanceof ArrayList) {
                response = (ArrayList) proxyDavProperty.getValue();
                if (response != null) {
                    for (Node objectInArray: response) {
                        if (objectInArray instanceof Element) {
                            DefaultDavProperty<?> newProperty = DefaultDavProperty
                                    .createFromXml((Element) objectInArray);
                            if ((newProperty.getName().getName().equals((DavConstants.XML_RESPONSE)))
                                    && (newProperty.getName().getNamespace().equals(DavConstants.NAMESPACE))) {
                                ArrayList<Node> responseChilds = (ArrayList) newProperty.getValue();
                                for (Node responseChild : responseChilds) {
                                    if (responseChild instanceof Element) {
                                        DefaultDavProperty<?> responseChildElement = DefaultDavProperty
                                                .createFromXml((Element) responseChild);
                                        if (responseChildElement.getName().getName().equals(DavConstants.XML_PROPSTAT)) {
                                            ArrayList<Node> propStatChilds = (ArrayList) responseChildElement
                                                    .getValue();
                                            for (Node propStatChild : propStatChilds) {
                                                if (propStatChild instanceof Element) {
                                                    DefaultDavProperty<?> propStatChildElement = DefaultDavProperty
                                                            .createFromXml((Element) propStatChild);
                                                    if (propStatChildElement.getName().getName()
                                                            .equals(DavConstants.XML_PROP)) {
                                                        ArrayList<Node> propChilds = (ArrayList) propStatChildElement
                                                                .getValue();
                                                        for (Node propChild : propChilds) {
                                                            if (propChild instanceof Element) {
                                                                DefaultDavProperty<?> propChildElement = DefaultDavProperty
                                                                        .createFromXml((Element) propChild);
                                                                if (propChildElement.getName().equals(
                                                                        SecurityConstants.PRINCIPAL_URL)) {
                                                                    ArrayList<Node> principalUrlChilds = (ArrayList) propChildElement
                                                                            .getValue();
                                                                    for (Node principalUrlChild : principalUrlChilds) {
                                                                        if (principalUrlChild instanceof Element) {
                                                                            DefaultDavProperty<?> principalUrlElement = DefaultDavProperty
                                                                                    .createFromXml((Element) principalUrlChild);
                                                                            if (principalUrlElement.getName().getName()
                                                                                    .equals(DavConstants.XML_HREF)) {
                                                                                String principalsUri = (String) principalUrlElement
                                                                                        .getValue();
                                                                                String urlForcalendarHomeSet = findCalendarHomeSet(getHostURL()
                                                                                        + principalsUri);
                                                                                delegatedCollections.addAll(getCollectionsForHomeSet(this,urlForcalendarHomeSet));
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } 
                    }
                }
            }
        }
        return delegatedCollections;
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
    
    protected List<CalDavCalendarCollection> getDelegatedCollections(String type) throws Exception {
        List<CalDavCalendarCollection> collections = new ArrayList<CalDavCalendarCollection>();

        String methodUri = this.pathResolver.getPrincipalPath(getUserName());

        Document document = newXmlDocument();

        Element writeDisplayNameProperty = newDavProperty(document, DavConstants.PROPERTY_DISPLAYNAME);

        Element writePrincipalUrlProperty = newDavProperty(document, SecurityConstants.PRINCIPAL_URL.getName());

        Element writeUserAddressSetProperty = newDavProperty(document, CalDavConstants.PROPERTY_USER_ADDRESS_SET,
                CalDavConstants.CALDAV_NAMESPACE);

        Element proxyWriteForElement = newDavProperty(document, type, CalDavConstants.CS_NAMESPACE,
                writeDisplayNameProperty, writePrincipalUrlProperty, writeUserAddressSetProperty);

        ReportInfo rinfo = new ReportInfo(ReportType.register(DeltaVConstants.XML_EXPAND_PROPERTY,
                DeltaVConstants.NAMESPACE, org.apache.jackrabbit.webdav.version.report.ExpandPropertyReport.class), 0);
        rinfo.setContentElement(proxyWriteForElement);

        HttpReport method = new HttpReport(methodUri, rinfo);
        HttpResponse httpResponse = getClient().execute(method);

        if (httpResponse.getStatusLine().getStatusCode() == DavServletResponse.SC_MULTI_STATUS) {
            MultiStatus multiStatus = method.getResponseBodyAsMultiStatus(httpResponse);
            MultiStatusResponse[] responses = multiStatus.getResponses();
            for (MultiStatusResponse respons : responses) {
                DavPropertySet properties = respons.getProperties(DavServletResponse.SC_OK);
                DavProperty<?> writeForProperty = properties.get(CalDavConstants.PROPERTY_PROXY_WRITE_FOR,
                        CalDavConstants.CS_NAMESPACE);
                List<CalDavCalendarCollection> writeCollections = getDelegateCollections(writeForProperty);
                for (CalDavCalendarCollection writeCollection : writeCollections) {
                    writeCollection.setReadOnly(false);
                    collections.add(writeCollection);
                }
                DavProperty<?> readForProperty = properties.get(CalDavConstants.PROPERTY_PROXY_READ_FOR,
                        CalDavConstants.CS_NAMESPACE);
                List<CalDavCalendarCollection> readCollections = getDelegateCollections(readForProperty);
                for (CalDavCalendarCollection readCollection : readCollections) {
                    readCollection.setReadOnly(true);
                    collections.add(readCollection);
                }
            }
        }
        return collections;        
    }
    
    public List<CalDavCalendarCollection> getWriteDelegatedCollections() throws Exception {
        List<CalDavCalendarCollection> collections = getDelegatedCollections(CalDavConstants.PROPERTY_PROXY_WRITE_FOR);
        return collections;
    }
    
    public List<CalDavCalendarCollection> getReadOnlyDelegatedCollections() throws Exception {
        List<CalDavCalendarCollection> collections = getDelegatedCollections(CalDavConstants.PROPERTY_PROXY_READ_FOR);
        return collections;
    }

    /**
     * {@inheritDoc}
     */
    public CalDavCalendarCollection removeCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        CalDavCalendarCollection collection = getCollection(id);
        try {
            collection.delete();
        } catch (IOException e) {
            throw new ObjectStoreException(String.format("unable to remove collection '%s'", id), e);
        }
        return collection;
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
        String propfindUri = getClient().hostConfiguration.toURI() + pathResolver.getPrincipalPath(getUserName());

        DavPropertyNameSet principalsProps = new DavPropertyNameSet();
        principalsProps.add(type);

        HttpPropfind method = new HttpPropfind(propfindUri, principalsProps, 0);
        RequestConfig config = RequestConfig.copy(method.getConfig()).setAuthenticationEnabled(true).build();
        method.setConfig(config);

        PropFindResponseHandler responseHandler = new PropFindResponseHandler(method);
        responseHandler.accept(getClient().execute(method));
        return responseHandler.getDavPropertyUri(type);
    }

    /**
     * This method will return free-busy information for each attendee. If the free-busy information can't be retrieve
     * (for example, users on an foreign server), check the isSuccess method to see if free-busy lookup was successful.
     * 
     * @author probert
     */
    public ArrayList<ScheduleResponse> findFreeBusyInfoForAttendees(Organizer organizer, ArrayList<Attendee> attendees,
            DtStart startTime, DtEnd endTime) throws ParserConfigurationException, IOException, DavException,
            ParseException, ParserException, SAXException {
        Random ramdomizer = new Random();
        ArrayList<ScheduleResponse> responses = new ArrayList<ScheduleResponse>();

        HttpPost postMethod = new HttpPost(findScheduleOutbox());
        postMethod.addHeader(DavConstants.HEADER_CONTENT_TYPE, "text/calendar; charset=utf-8");

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId(getProdId()));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getProperties().add(Method.REQUEST);

        VFreeBusy fbComponent = new VFreeBusy();

        fbComponent.getProperties().add(organizer);
        // Was removed from the draft, but some servers still need it
        postMethod.addHeader("Originator", organizer.getValue());

        fbComponent.getProperties().add(startTime);
        fbComponent.getProperties().add(endTime);

        String strAttendee = "";
        if (attendees != null) {
            for (Iterator<Attendee> itrAttendee = attendees.iterator(); itrAttendee.hasNext();) {
                Attendee attendee = itrAttendee.next();
                fbComponent.getProperties().add(attendee);
                strAttendee += attendee.getValue() + ",";
            }
            strAttendee = strAttendee.substring(0, strAttendee.length() - 1);
            // Was removed from the draft, but some servers still need it
            postMethod.addHeader("Recipient", strAttendee);
        }

        UidGenerator ug = new FixedUidGenerator(ramdomizer.nextInt() + "");
        fbComponent.getProperties().add(ug.generateUid());
        calendar.getComponents().add(fbComponent);

        postMethod.setEntity(new StringEntity(calendar.toString()));
        HttpResponse httpResponse = getClient().execute(postMethod);
        if (httpResponse.getStatusLine().getStatusCode() < 300) {
            Document xmlDoc = parseXml(postMethod.getEntity().getContent());
            NodeList nodes = xmlDoc.getElementsByTagNameNS(CalDavConstants.CALDAV_NAMESPACE.getURI(),
                    DavPropertyName.XML_RESPONSE);
            for (int nodeItr = 0; nodeItr < nodes.getLength(); nodeItr++) {
                responses.add(new ScheduleResponse((Element) nodes.item(nodeItr)));
            }
        }
        return responses;
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
        return executePrincipalPropSearch(new PrincipalPropertySearch(type).build());
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
        return executePrincipalPropSearch(new PrincipalPropertySearch(type, nameToSearch).build());
    }
    
    protected List<Attendee> executePrincipalPropSearch(Element principalPropSearch) throws DavException, IOException, URISyntaxException {        
        PrincipalPropertySearchInfo rinfo = new PrincipalPropertySearchInfo(principalPropSearch, 0);
        
        String methodUri = this.pathResolver.getPrincipalPath(getUserName());
        PrincipalPropertySearchMethod method = new PrincipalPropertySearchMethod(methodUri, rinfo);
        HttpResponse httpResponse = getClient().execute(method);
        
        List<Attendee> resources = new ArrayList<Attendee>();
        
        if (httpResponse.getStatusLine().getStatusCode() == DavServletResponse.SC_MULTI_STATUS) {
            MultiStatus multiStatus = method.getResponseBodyAsMultiStatus(httpResponse);
            MultiStatusResponse[] responses = multiStatus.getResponses();
            for (int i = 0; i < responses.length; i++) {
                
                Attendee resource = new Attendee();
                DavPropertySet propertiesInResponse = responses[i].getProperties(DavServletResponse.SC_OK);

                DavProperty<?> displayNameFromResponse = propertiesInResponse.get("displayname",
                        CalDavConstants.NAMESPACE);
                if ((displayNameFromResponse != null) && (displayNameFromResponse.getValue() != null)) {
                    resource.getParameters().add(new Cn((String)displayNameFromResponse.getValue()));
                }
                
                DavProperty<?> emailSet = propertiesInResponse.get("email-address-set",
                        CalDavConstants.CS_NAMESPACE);
                
                if (emailSet != null && emailSet.getValue() != null) {
                    Object emailSetValue = emailSet.getValue();
                    if (emailSetValue instanceof java.util.ArrayList) {
                        for (Object email: (java.util.ArrayList)emailSetValue) {
                            if (email instanceof org.w3c.dom.Node) {
                                String emailAddress = ((org.w3c.dom.Node)email).getTextContent();
                                if (emailAddress != null && emailAddress.trim().length() > 0) {
                                    if (!emailAddress.startsWith("mailto:")) {
                                        emailAddress = "mailto:".concat(emailAddress);
                                    }
                                    resource.setCalAddress(new URI(emailAddress));
                                }
                            }
                        }
                    }
                } else {
                    DavProperty<?> calendarUserAddressSet = propertiesInResponse.get(CalDavConstants.PROPERTY_USER_ADDRESS_SET,
                            CalDavConstants.CALDAV_NAMESPACE);
                    if (calendarUserAddressSet != null && calendarUserAddressSet.getValue() != null) {
                        Object value = calendarUserAddressSet.getValue();
                        if (value instanceof java.util.ArrayList) {
                            for (Object addressSet: (java.util.ArrayList)value) {
                                if (addressSet instanceof org.w3c.dom.Node) {
                                    String url = ((org.w3c.dom.Node)addressSet).getTextContent();
                                    if (url.startsWith("urn:uuid")) {
                                        resource.setCalAddress(new URI(url));
                                    }
                                }
                            }
                        }
                    }
                }
                
                DavProperty<?> calendarUserType = propertiesInResponse.get(CalDavConstants.PROPERTY_USER_TYPE,
                        CalDavConstants.CALDAV_NAMESPACE);
                if ((calendarUserType != null) && (calendarUserType.getValue() != null)) {
                    resource.getParameters().add(new CuType((String)calendarUserType.getValue()));
                }
                
                resources.add(resource);
            }
        }
        return resources;
    }

}

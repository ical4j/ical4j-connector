/**
 * Copyright (c) 2009, Ben Fortuna
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.fortuna.ical4j.connector.CalendarCollection;
import net.fortuna.ical4j.connector.CalendarStore;
import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * $Id$
 * 
 * Created on 24/02/2008
 * 
 * @author Ben
 * 
 */
public final class CalDavCalendarStore extends AbstractDavObjectStore<CalDavCalendarCollection>
    implements CalendarStore<CalDavCalendarCollection> {

    private final String prodId;

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
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return collection;
    }

    /**
     * {@inheritDoc}
     */
    public CalDavCalendarCollection getCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        CalDavCalendarCollection collection = new CalDavCalendarCollection(this, id);
        try {
            if (collection.exists()) {
                return collection;
            }
        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        throw new ObjectNotFoundException("Collection with id: [" + id + "] not found");
    }

    /**
     * {@inheritDoc}
     */
    public CalendarCollection merge(String id, CalendarCollection calendar) {
        // TODO Auto-generated method stub
        return null;
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
    private String findCalendarHomeSet() throws ParserConfigurationException, IOException, DavException {
        String propfindUri = hostConfiguration.getHostURL() + pathResolver.getPrincipalPath(getUserName());
        // GAUTAM UPDATED FOLLOWING LINE with port configuration
//        String propfindUri = hostConfiguration.getHostURL() + ":" + hostConfiguration.getPort() + pathResolver.getPrincipalPath(getUserName());

        DavPropertyNameSet principalsProps = new DavPropertyNameSet();
        principalsProps.add(DavPropertyName.create(CalDavConstants.PROPERTY_HOME_SET, CalDavConstants.NAMESPACE));
        principalsProps.add(DavPropertyName.DISPLAYNAME);

        PropFindMethod method = new PropFindMethod(propfindUri, principalsProps, PropFindMethod.DEPTH_0);
        httpClient.executeMethod(hostConfiguration, method);

        MultiStatus multiStatus = method.getResponseBodyAsMultiStatus();
        MultiStatusResponse[] responses = multiStatus.getResponses();
        for (int i = 0; i < responses.length; i++) {
            for (int j = 0; j < responses[i].getStatus().length; j++) {
                Status status = responses[i].getStatus()[j];
                for (DavPropertyIterator iNames = responses[i].getProperties(status.getStatusCode()).iterator(); iNames
                        .hasNext();) {
                    DavProperty name = iNames.nextProperty();
                    if ((name.getName().getName().equals(CalDavConstants.PROPERTY_HOME_SET))
                            && (CalDavConstants.NAMESPACE.isSame(name.getName().getNamespace().getURI()))) {
                        if (name.getValue() instanceof ArrayList) {
                            for (Iterator<?> iter = ((ArrayList<?>) name.getValue()).iterator(); iter.hasNext();) {
                                Object child = iter.next();
                                if (child instanceof Element) {
                                    String calendarHomeSetUri = ((Element) child).getTextContent();
                                    /*
                                     * If the trailing slash is not there, CalendarServer will return a 301 status code
                                     * and we will get a nice DavException with "Moved Permanently" as the error
                                     */
                                    if (!(calendarHomeSetUri.endsWith("/"))) {
                                        calendarHomeSetUri += "/";
                                    }
                                    return calendarHomeSetUri;
                                }
                            }
                        }
                        /*
                         * This is for Kerio Mail Server implementation...
                         */
                        if (name.getValue() instanceof Node) {
                            Node child = (Node) name.getValue();
                            if (child instanceof Element) {
                                String calendarHomeSetUri = ((Element) child).getTextContent();
                                /*
                                 * If the trailing slash is not there, CalendarServer will return a 301 status code and
                                 * we will get a nice DavException with "Moved Permanently" as the error
                                 */
                                if (!(calendarHomeSetUri.endsWith("/"))) {
                                    calendarHomeSetUri += "/";
                                }
                                return calendarHomeSetUri;
                            }
                        }
                    }
                }
            }
        }
        return null;
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
    public List<CalDavCalendarCollection> getCollections() throws ParserConfigurationException,
        IOException, DavException {
        
        List<CalDavCalendarCollection> collections = new ArrayList<CalDavCalendarCollection>();

        DavPropertyNameSet principalsProps = new DavPropertyNameSet();
        principalsProps.add(DavPropertyName.DISPLAYNAME);
        principalsProps.add(DavPropertyName.RESOURCETYPE);
        principalsProps.add(DavPropertyName.create(CalDavConstants.PROPERTY_CTAG, CalDavConstants.CS_NAMESPACE));
        principalsProps.add(DavPropertyName.create(CalDavConstants.PROPERTY_CALENDAR_DESCRIPTION,
                CalDavConstants.NAMESPACE));
        principalsProps.add(DavPropertyName.create(CalDavConstants.PROPERTY_CALENDAR_COLOR,
                CalDavConstants.ICAL_NAMESPACE));
        principalsProps.add(DavPropertyName.create(CalDavConstants.PROPERTY_CALENDAR_ORDER,
                CalDavConstants.ICAL_NAMESPACE));
        principalsProps.add(DavPropertyName.create(CalDavConstants.PROPERTY_FREE_BUSY_SET, CalDavConstants.NAMESPACE));

        String calHomeSetUri = findCalendarHomeSet();
        if (calHomeSetUri == null) {
            throw new DavException(HttpStatus.SC_NOT_FOUND, "No calendar-home-set attribute found for the user");
        }
        String urlForcalendarHomeSet = hostConfiguration.getHostURL() + findCalendarHomeSet();
        PropFindMethod method = new PropFindMethod(urlForcalendarHomeSet, principalsProps, PropFindMethod.DEPTH_1);
        httpClient.executeMethod(hostConfiguration, method);

        MultiStatus multiStatus = method.getResponseBodyAsMultiStatus();
        MultiStatusResponse[] responses = multiStatus.getResponses();
        for (int i = 0; i < responses.length; i++) {
            String collectionUri = responses[i].getHref();
            for (int j = 0; j < responses[i].getStatus().length; j++) {
                Status status = responses[i].getStatus()[j];
                for (DavPropertyIterator iNames = responses[i].getProperties(status.getStatusCode()).iterator(); iNames
                        .hasNext();) {
                    DavProperty name = iNames.nextProperty();
                    if (name.getName().getName().equals("resourcetype")
                            && (DavConstants.NAMESPACE.isSame(name.getName().getNamespace().getURI()))) {
                        if (name.getValue() instanceof ArrayList) {
                            for (Iterator<?> iter = ((ArrayList<?>) name.getValue()).iterator(); iter.hasNext();) {
                                Object child = iter.next();
                                if (child instanceof Node) {
                                    Node node = ((Node) child);
                                    if (CalDavConstants.PROPERTY_RESOURCETYPE_CALENDAR.equals(node.getLocalName())
                                            && CalDavConstants.NAMESPACE.getURI().equals(node.getNamespaceURI())) {
                                        collections.add(new CalDavCalendarCollection(this, collectionUri));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return collections;
    }

    /**
     * {@inheritDoc}
     */
    public CalDavCalendarCollection removeCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        CalDavCalendarCollection collection = getCollection(id);
        try {
            collection.delete();
        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

}

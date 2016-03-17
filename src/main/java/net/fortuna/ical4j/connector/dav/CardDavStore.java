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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.fortuna.ical4j.connector.CalendarCollection;
import net.fortuna.ical4j.connector.CardStore;
import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.property.CardDavPropertyName;
import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.httpclient.HttpException;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.client.methods.DavMethodBase;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Document;
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
public final class CardDavStore extends AbstractDavObjectStore<CardDavCollection> implements
        CardStore<CardDavCollection> {

    private final String prodId;
    private String displayName;


    /**
     * @param prodId application product identifier
     * @param url    the URL of a CardDav server instance
     */
    public CardDavStore( String prodId, URL url ) {
        this( prodId, url, null );
    }


    /**
     * @param prodId application product identifier
     * @param url the URL of a CardDav server instance
     * @param pathResolver the path resolver for the CardDav server type
     */
    public CardDavStore(String prodId, URL url, PathResolver pathResolver) {
        super(url, pathResolver);
        this.prodId = prodId;
    }

    /**
     * {@inheritDoc}
     */
    public CardDavCollection addCollection(String id) throws ObjectStoreException {
        CardDavCollection collection = new CardDavCollection(this, id);
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
    public CardDavCollection addCollection(String id, DavPropertySet properties) throws ObjectStoreException {
        CardDavCollection collection = new CardDavCollection(this, id, properties);
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
    public CardDavCollection getCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        try {
            DavPropertyNameSet principalsProps = CardDavCollection.propertiesForFetch();
            PropFindMethod getMethod = new PropFindMethod(id, principalsProps, PropFindMethod.DEPTH_0);

            this.getClient().execute(getMethod);

            MultiStatus multiStatus = getMethod.getResponseBodyAsMultiStatus();
            MultiStatusResponse[] responses = multiStatus.getResponses();

            return CardDavCollection.collectionsFromResponse(this, responses).get(0);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DavException e) {
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

    protected String findAddressBookHomeSet() throws ParserConfigurationException, IOException, DavException {
        String propfindUri = getHostURL() + pathResolver.getPrincipalPath(getUserName());
        return findAddressBookHomeSet(propfindUri);
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
    protected String findAddressBookHomeSet(String propfindUri) throws ParserConfigurationException, IOException,
            DavException {
        DavPropertyNameSet principalsProps = new DavPropertyNameSet();
        principalsProps.add(CardDavPropertyName.ADDRESSBOOK_HOME_SET);
        principalsProps.add(DavPropertyName.DISPLAYNAME);

        PropFindMethod method = new PropFindMethod(propfindUri, principalsProps, PropFindMethod.DEPTH_0);
        getClient().execute(method);

        MultiStatus multiStatus = method.getResponseBodyAsMultiStatus();
        MultiStatusResponse[] responses = multiStatus.getResponses();
        for (int i = 0; i < responses.length; i++) {
            for (int j = 0; j < responses[i].getStatus().length; j++) {
                Status status = responses[i].getStatus()[j];
                for (DavPropertyIterator iNames = responses[i].getProperties(status.getStatusCode()).iterator(); iNames
                        .hasNext();) {
                    DavProperty name = iNames.nextProperty();
                    if ((name.getName().getName().equals(CalDavConstants.PROPERTY_ADDRESSBOOK_HOME_SET))
                            && (CalDavConstants.CARDDAV_NAMESPACE.isSame(name.getName().getNamespace().getURI()))) {
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
    public List<CardDavCollection> getCollections() throws ObjectStoreException, ObjectNotFoundException {
        try {
            String calHomeSetUri = findAddressBookHomeSet();
            if (calHomeSetUri == null) {
                throw new ObjectNotFoundException("No " + CalDavConstants.PROPERTY_ADDRESSBOOK_HOME_SET + " attribute found for the user");
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
    
    protected List<CardDavCollection> getCollectionsForHomeSet(CardDavStore store,
            String urlForcalendarHomeSet) throws IOException, DavException {
        List<CardDavCollection> collections = new ArrayList<CardDavCollection>();

        DavPropertyNameSet principalsProps = CardDavCollection.propertiesForFetch();

        PropFindMethod method = new PropFindMethod(urlForcalendarHomeSet, principalsProps, PropFindMethod.DEPTH_1);
        getClient().execute(method);

        MultiStatus multiStatus = method.getResponseBodyAsMultiStatus();
        MultiStatusResponse[] responses = multiStatus.getResponses();
        
        return CardDavCollection.collectionsFromResponse(store, responses);
    }

    protected List<CardDavCollection> getDelegateCollections(DavProperty<?> proxyDavProperty)
            throws ParserConfigurationException, IOException, DavException {
        /*
         * Zimbra check: Zimbra advertise calendar-proxy, but it will return 404 in propstat if Enable delegation for
         * Apple iCal CardDav client is not enabled
         */
        if (proxyDavProperty != null) {
            Object propertyValue = proxyDavProperty.getValue();
            ArrayList<Node> response;

            if (propertyValue instanceof ArrayList) {
                response = (ArrayList) proxyDavProperty.getValue();
                if (response != null) {
                    for (Node objectInArray : response) {
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
                                                                                String urlForcalendarHomeSet = findAddressBookHomeSet(getHostURL()
                                                                                        + principalsUri);
                                                                                return getCollectionsForHomeSet(this,
                                                                                        urlForcalendarHomeSet);
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
            } else if (propertyValue instanceof Element) {
                System.out.println(((Element)propertyValue).getNodeName());
                System.out.println(((Element)propertyValue).getChildNodes());
            }
        }
        return new ArrayList<CardDavCollection>();
    }

    /**
     * Get the list of available delegated collections, Apple's iCal style
     * 
     * @return
     * @throws Exception
     */
    public List<CardDavCollection> getDelegatedCollections() throws Exception {

        List<CardDavCollection> collections = new ArrayList<CardDavCollection>();

        String methodUri = this.pathResolver.getPrincipalPath(getUserName());

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element writeDisplayNameProperty = DomUtil.createElement(document, "property", DavConstants.NAMESPACE);
        writeDisplayNameProperty.setAttribute("name", DavConstants.PROPERTY_DISPLAYNAME);

        Element writePrincipalUrlProperty = DomUtil.createElement(document, "property", DavConstants.NAMESPACE);
        writePrincipalUrlProperty.setAttribute("name", SecurityConstants.PRINCIPAL_URL.getName());

        Element writeUserAddressSetProperty = DomUtil.createElement(document, "property", DavConstants.NAMESPACE);
        writeUserAddressSetProperty.setAttribute("name", CalDavConstants.PROPERTY_USER_ADDRESS_SET);
        writeUserAddressSetProperty.setAttribute("namespace", CalDavConstants.NAMESPACE.getURI());

        Element proxyWriteForElement = DomUtil.createElement(document, "property", DavConstants.NAMESPACE);
        proxyWriteForElement.setAttribute("name", CalDavConstants.PROPERTY_PROXY_WRITE_FOR);
        proxyWriteForElement.setAttribute("namespace", CalDavConstants.CS_NAMESPACE.getURI());
        proxyWriteForElement.appendChild(writeDisplayNameProperty);
        proxyWriteForElement.appendChild(writePrincipalUrlProperty);
        proxyWriteForElement.appendChild(writeUserAddressSetProperty);

        Element readDisplayNameProperty = DomUtil.createElement(document, "property", DavConstants.NAMESPACE);
        readDisplayNameProperty.setAttribute("name", DavConstants.PROPERTY_DISPLAYNAME);

        Element readPrincipalUrlProperty = DomUtil.createElement(document, "property", DavConstants.NAMESPACE);
        readPrincipalUrlProperty.setAttribute("name", SecurityConstants.PRINCIPAL_URL.getName());

        Element readUserAddressSetProperty = DomUtil.createElement(document, "property", DavConstants.NAMESPACE);
        readUserAddressSetProperty.setAttribute("name", CalDavConstants.PROPERTY_USER_ADDRESS_SET);
        readUserAddressSetProperty.setAttribute("namespace", CalDavConstants.NAMESPACE.getURI());

        Element proxyReadForElement = DomUtil.createElement(document, "property", DavConstants.NAMESPACE);
        proxyReadForElement.setAttribute("name", CalDavConstants.PROPERTY_PROXY_READ_FOR);
        proxyReadForElement.setAttribute("namespace", CalDavConstants.CS_NAMESPACE.getURI());
        proxyReadForElement.appendChild(readDisplayNameProperty);
        proxyReadForElement.appendChild(readPrincipalUrlProperty);
        proxyReadForElement.appendChild(readUserAddressSetProperty);

        ReportInfo rinfo = new ReportInfo(ReportType.register(DeltaVConstants.XML_EXPAND_PROPERTY,
                DeltaVConstants.NAMESPACE, org.apache.jackrabbit.webdav.version.report.ExpandPropertyReport.class), 0);
        rinfo.setContentElement(proxyWriteForElement);
        rinfo.setContentElement(proxyReadForElement);

        DavMethodBase method = new ReportMethod(methodUri, rinfo);
        getClient().execute(getClient().hostConfiguration, method);

        if (method.getStatusCode() == DavServletResponse.SC_MULTI_STATUS) {
            MultiStatus multiStatus = method.getResponseBodyAsMultiStatus();
            MultiStatusResponse[] responses = multiStatus.getResponses();
            for (int i = 0; i < responses.length; i++) {
                DavPropertySet properties = responses[i].getProperties(DavServletResponse.SC_OK);
                DavProperty<?> writeForProperty = properties.get(CalDavConstants.PROPERTY_PROXY_WRITE_FOR,
                        CalDavConstants.CS_NAMESPACE);
                collections.addAll(getDelegateCollections(writeForProperty));
                DavProperty<?> readForProperty = properties.get(CalDavConstants.PROPERTY_PROXY_READ_FOR,
                        CalDavConstants.CS_NAMESPACE);
                collections.addAll(getDelegateCollections(readForProperty));
            }
        }
        return collections;
    }

    /**
     * {@inheritDoc}
     */
    public CardDavCollection removeCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        CardDavCollection collection = getCollection(id);
        try {
            collection.delete();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#addCollection(java.lang.String, java.lang.String, java.lang.String, java.lang.String[], net.fortuna.ical4j.model.Calendar)
     */
    public CardDavCollection addCollection(String id, String displayName, String description,
            String[] supportedComponents, Calendar timezone) throws ObjectStoreException {
        // TODO Auto-generated method stub
        return null;
    }

}

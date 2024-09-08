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

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.vcard.VCard;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.CardStore;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;
import org.ical4j.connector.dav.property.BaseDavPropertyName;
import org.ical4j.connector.dav.property.CalDavPropertyName;
import org.ical4j.connector.dav.property.CardDavPropertyName;
import org.ical4j.connector.dav.property.PropertyNameSets;
import org.ical4j.connector.dav.request.ExpandPropertyQuery;
import org.ical4j.connector.dav.response.GetCardDavCollections;
import org.ical4j.connector.dav.response.GetCollections;
import org.ical4j.connector.dav.response.GetPropertyValue;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * $Id$
 * 
 * Created on 24/02/2008
 * 
 * @author Ben
 * 
 */
public final class CardDavStore extends AbstractDavObjectStore<VCard, CardDavCollection> implements
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
    @Override
    public CardDavCollection addCollection(String id) throws ObjectStoreException {
        CardDavCollection collection = new CardDavCollection(this, id);
        try {
            collection.create();
        } catch (IOException e) {
            throw new ObjectStoreException(String.format("unable to add collection '%s'", id), e);
        }
        return collection;
    }

    @Override
    public CardDavCollection addCollection(String id, String workspace) throws ObjectStoreException {
        throw new UnsupportedOperationException("Workspaces not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    public CardDavCollection addCollection(String id, DavPropertySet properties) throws ObjectStoreException {
        CardDavCollection collection = new CardDavCollection(this, id, properties);
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
    public CardDavCollection getCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        try {
            return getClient().propFind(id, PropertyNameSets.PROPFIND_CARD,
                            new GetCollections(ResourceType.ADRESSBOOK)).entrySet().stream()
                    .map(e -> new CardDavCollection(this, e.getKey(), e.getValue()))
                    .collect(Collectors.toList()).get(0);
        } catch (IOException e) {
            throw new ObjectStoreException(String.format("unable to get collection '%s'", id), e);
        }
    }

    @Override
    public CardDavCollection getCollection(String id, String workspace) throws ObjectStoreException, ObjectNotFoundException {
        throw new UnsupportedOperationException("Workspaces not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    public CalendarCollection merge(String id, CalendarCollection calendar) {
        throw new UnsupportedOperationException("not implemented");
    }

    protected String findAddressBookHomeSet() throws ParserConfigurationException, IOException, DavException {
        String propfindPath = pathResolver.getPrincipalPath(getSessionConfiguration().getUser());
        return findAddressBookHomeSet(propfindPath);
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
    protected String findAddressBookHomeSet(String propfindUri) throws IOException {
        return getClient().propFind(propfindUri, PropertyNameSets.PROPFIND_CARD_HOME, new GetPropertyValue<>());
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
            String calHomeSetPath = findAddressBookHomeSet();
            if (calHomeSetPath == null) {
                throw new ObjectNotFoundException("No " + CardDavPropertyName.ADDRESSBOOK_HOME_SET + " attribute found for the user");
            }
            return getCollectionsForHomeSet(this, calHomeSetPath);
        } catch (DavException | IOException | ParserConfigurationException e) {
            throw new ObjectStoreException(e);
        }
    }

    @Override
    public List<CardDavCollection> getCollections(String workspace) throws ObjectStoreException, ObjectNotFoundException {
        throw new UnsupportedOperationException("Workspaces not yet implemented");
    }

    protected List<CardDavCollection> getCollectionsForHomeSet(CardDavStore store,
                                                               String urlForcalendarHomeSet) throws IOException, DavException {

        return getClient().propFind(urlForcalendarHomeSet, PropertyNameSets.PROPFIND_CARD,
                        new GetCollections(ResourceType.ADRESSBOOK)).entrySet().stream()
                .map(e -> new CardDavCollection(this, e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Get the list of available delegated collections, Apple's iCal style
     * 
     * @return
     * @throws Exception
     */
    public List<CardDavCollection> getDelegatedCollections() throws Exception {


        String methodUri = this.pathResolver.getPrincipalPath(getSessionConfiguration().getUser());

        ExpandPropertyQuery expandPropertyWrite = new ExpandPropertyQuery(ExpandPropertyQuery.Type.PROXY_WRITE_FOR)
                .withPropertyName(DavPropertyName.DISPLAYNAME)
                .withPropertyName(SecurityConstants.PRINCIPAL_URL)
                .withPropertyName(CalDavPropertyName.USER_ADDRESS_SET);

        ExpandPropertyQuery expandPropertyRead = new ExpandPropertyQuery(ExpandPropertyQuery.Type.PROXY_READ_FOR)
                .withPropertyName(DavPropertyName.DISPLAYNAME)
                .withPropertyName(SecurityConstants.PRINCIPAL_URL)
                .withPropertyName(CalDavPropertyName.USER_ADDRESS_SET);

        ReportInfo rinfo = new ReportInfo(BaseDavPropertyName.EXPAND_PROPERTY, 0);
        rinfo.setContentElement(expandPropertyWrite.build());
        rinfo.setContentElement(expandPropertyRead.build());

        return getClient().report(methodUri, rinfo, new GetCardDavCollections());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardDavCollection removeCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        CardDavCollection collection = getCollection(id);
        collection.delete();
        return collection;
    }

    // public CalendarCollection replace(String id, CalendarCollection calendar) {
    // // TODO Auto-generated method stub
    // return null;
    // }


    @Override
    public List<String> listWorkspaces() {
        throw new UnsupportedOperationException("Workspaces not yet implemented");
    }

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
     * @see org.ical4j.connector.ObjectStore#addCollection(java.lang.String, java.lang.String, java.lang.String, java.lang.String[], net.fortuna.ical4j.model.Calendar)
     */
    public CardDavCollection addCollection(String id, String displayName, String description,
            String[] supportedComponents, Calendar timezone) throws ObjectStoreException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public CardDavCollection addCollection(String id, String displayName, String description, String[] supportedComponents, Calendar timezone, String workspace) throws ObjectStoreException {
        throw new UnsupportedOperationException("Workspaces not yet implemented");
    }
}

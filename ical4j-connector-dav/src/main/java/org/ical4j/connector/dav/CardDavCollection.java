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

import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.property.Uid;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.ObjectStoreException;
import org.ical4j.connector.dav.property.CalDavPropertyName;
import org.ical4j.connector.dav.property.CardDavPropertyName;
import org.ical4j.connector.dav.property.DavPropertyBuilder;
import org.ical4j.connector.dav.property.PropertyNameSets;
import org.ical4j.connector.dav.response.GetVCardData;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
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
public class CardDavCollection extends AbstractDavObjectCollection<VCard> implements CardCollection {

    /**
     * Only {@link CardDavStore} should be calling this, so default modifier is applied.
     * 
     * @param CardDavCalendarStore
     * @param id
     */
    CardDavCollection(CardDavStore CardDavCalendarStore, String id) {
        this(CardDavCalendarStore, id, id, "");
    }

    /**
     * Only {@link CardDavStore} should be calling this, so default modifier is applied.
     * 
     * @param cardDavStore
     * @param id
     * @param displayName
     * @param description
     */
    CardDavCollection(CardDavStore cardDavStore, String id, String displayName, String description) {

        super(cardDavStore, id);
        properties.add(new DavPropertyBuilder<>().name(DISPLAYNAME).value(displayName).build());
        properties.add(new DavPropertyBuilder<>().name(CardDavPropertyName.ADDRESSBOOK_DESCRIPTION).value(description).build());
    }

    CardDavCollection(CardDavStore cardDavStore, String id, DavPropertySet _properties) {
        this(cardDavStore, id, id, "");
        this.properties = _properties;
    }

    @Override
    String getPath() {
        return getStore().pathResolver.getCardPath(getId(), getStore().getSessionConfiguration().getWorkspace());
    }

    /**
     * Creates this collection on the CardDAV server.
     * 
     * @throws IOException
     * @throws ObjectStoreException
     */
    final void create() throws IOException, ObjectStoreException {
        try {
            getStore().getClient().mkCol(getPath(), properties);
        } catch (DavException e) {
            throw new ObjectStoreException("Failed to create collection", e);
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
     * Provides a numeric value indicating the maximum size of a resource in octets that the server is willing to accept
     * when a calendar object resource is stored in a calendar collection. 0 = no limits.
     */
    public long getMaxResourceSize() {
        try {
            var size = getProperty(CalDavPropertyName.MAX_RESOURCE_SIZE, Long.class);
            if (size != null) {
                return size;
            }
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see org.ical4j.connector.ObjectCollection#getDescription()
     */
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> listObjectUIDs() {
        //TODO: extract UIDs from vcards..
        return null;
    }

    /* (non-Javadoc)
     * @see org.ical4j.connector.ObjectCollection#getComponents()
     */
    public Iterable<VCard> getAll() throws ObjectStoreException {
        try {
            var info = new ReportInfo(CardDavPropertyName.ADDRESSBOOK_QUERY, 1,
                    PropertyNameSets.REPORT_CARD);

            return getStore().getClient().report(getPath(), info, new GetVCardData());
        } catch (IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.ical4j.connector.CardCollection#addCard(net.fortuna.ical4j.vcard.VCard)
     */
    public String add(VCard card) throws ObjectStoreException, ConstraintViolationException {
        var uid = card.getUid();
        save(card);
        return uid.getValue();
    }

    @Override
    public Optional<VCard> get(String uid) {
        try {
            return Optional.of(getStore().getClient().getVCard(uid));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<VCard> removeAll(String... uid) {
        return null;
    }

    @Override
    public Uid[] merge(VCard card) throws ObjectStoreException, ConstraintViolationException, FailedOperationException {
        List<Uid> uids = new ArrayList<>();
        try {
            var uidCalendars = card.split();
            for (int i = 0; i < uidCalendars.length; i++) {
                add(uidCalendars[i]);
                uids.add(uidCalendars[i].getUid());
            }
        } catch (ConstraintViolationException cve) {
            throw new FailedOperationException("Invalid card format", cve);
        }
        return uids.toArray(new Uid[0]);
    }

    private void save(VCard card) throws ObjectStoreException {
        var uid = card.getUid();

        var path = getPath();
        if (!path.endsWith("/")) {
            path = path.concat("/");
        }
        try {
            getStore().getClient().put(path + uid.getValue() + ".vcf", card, null);
        } catch (IOException | FailedOperationException e) {
            throw new ObjectStoreException("Error creating calendar on server", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public VCard export() {
        throw new UnsupportedOperationException("not implemented");
    }
}

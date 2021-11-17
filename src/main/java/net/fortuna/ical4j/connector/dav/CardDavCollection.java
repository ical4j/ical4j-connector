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

import net.fortuna.ical4j.connector.CardCollection;
import net.fortuna.ical4j.connector.FailedOperationException;
import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.method.MkCalendarMethod;
import net.fortuna.ical4j.connector.dav.method.PutMethod;
import net.fortuna.ical4j.connector.dav.method.ReportMethod;
import net.fortuna.ical4j.connector.dav.property.BaseDavPropertyName;
import net.fortuna.ical4j.connector.dav.property.CalDavPropertyName;
import net.fortuna.ical4j.connector.dav.property.CardDavPropertyName;
import net.fortuna.ical4j.connector.dav.response.ReportResponseHandler;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.VCard;
import org.apache.http.HttpResponse;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.client.methods.HttpReport;
import org.apache.jackrabbit.webdav.client.methods.XmlEntity;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;

import java.io.IOException;

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
     * @param path
     */
    CardDavCollection(CardDavStore CardDavCalendarStore, String id) {
        this(CardDavCalendarStore, id, null, null);
    }

    /**
     * Only {@link CardDavStore} should be calling this, so default modifier is applied.
     * 
     * @param CardDavCalendarStore
     * @param id
     * @param displayName
     * @param description
     */
    CardDavCollection(CardDavStore CardDavCalendarStore, String id, String displayName, String description) {

        super(CardDavCalendarStore, id);
        properties.add(new DefaultDavProperty(DavPropertyName.DISPLAYNAME, displayName));
        properties.add(new DefaultDavProperty(CalDavPropertyName.CALENDAR_DESCRIPTION, description));
    }

    CardDavCollection(CardDavStore CardDavCalendarStore, String id, DavPropertySet _properties) {
        this(CardDavCalendarStore, id, null, null);
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
            throw new ObjectStoreException(httpResponse.getStatusLine().getStatusCode() + ": "
                    + httpResponse.getStatusLine().getReasonPhrase());
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
     * Provides a numeric value indicating the maximum size of a resource in octets that the server is willing to accept
     * when a calendar object resource is stored in a calendar collection. 0 = no limits.
     */
    public long getMaxResourceSize() {
        try {
            Long size = getProperty(CalDavPropertyName.MAX_RESOURCE_SIZE, Long.class);
            if (size != null) {
                return size;
            }
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public Calendar export() throws ObjectStoreException {
        throw new UnsupportedOperationException("not implemented");
    }
        
    public static final DavPropertyNameSet propertiesForFetch() {
        DavPropertyNameSet principalsProps = new DavPropertyNameSet();

        /*
         * TODO : to add the following properties
            <C:me-card xmlns:C="http://calendarserver.org/ns/" /> 
            <C:push-transports xmlns:C="http://calendarserver.org/ns/" /> 
            <C:pushkey xmlns:C="http://calendarserver.org/ns/" /> 
            <D:bulk-requests xmlns:D="http://me.com/_namespace/" /> 
       */
        
        principalsProps.add(DavPropertyName.DISPLAYNAME);


        principalsProps.add(BaseDavPropertyName.CURRENT_USER_PRIVILEGE_SET);
        principalsProps.add(BaseDavPropertyName.RESOURCETYPE);
        principalsProps.add(SecurityConstants.OWNER);
        principalsProps.add(CardDavPropertyName.MAX_RESOURCE_SIZE);
        principalsProps.add(BaseDavPropertyName.RESOURCE_ID);
        principalsProps.add(BaseDavPropertyName.SUPPORTED_REPORT_SET);
        principalsProps.add(BaseDavPropertyName.SYNC_TOKEN);
        principalsProps.add(BaseDavPropertyName.ADD_MEMBER);
        principalsProps.add(CardDavPropertyName.MAX_IMAGE_SIZE);
        
        /**
         * FIXME jackrabbit generates an error when quota-used-bytes is sent.
         * I suspect the problem is that the response have this attribute: e:dt="int"
         */
        //principalsProps.add(BaseDavPropertyName.QUOTA_USED_BYTES);
        //principalsProps.add(BaseDavPropertyName.QUOTA_AVAILABLE_BYTES);
        
        /* In the absence of this property, the server MUST only accept data with the media type
         * "text/vcard" and vCard version 3.0, and clients can assume that is
         * all the server will accept.
         */
        principalsProps.add(CardDavPropertyName.SUPPORTED_ADDRESS_DATA);
        
        return principalsProps;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectCollection#getDescription()
     */
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectCollection#getComponents()
     */
    public VCard[] getComponents() throws ObjectStoreException {
        try {
            DavPropertyNameSet properties = new DavPropertyNameSet();
            properties.add(DavPropertyName.GETETAG);
            properties.add(CardDavPropertyName.ADDRESS_DATA);

            ReportInfo info = new ReportInfo(ReportMethod.ADDRESSBOOK_QUERY, 1, properties);

            HttpReport method = new HttpReport(getPath(), info);
            ReportResponseHandler responseHandler = new ReportResponseHandler(method);
            responseHandler.accept(getStore().getClient().execute(method));
            return responseHandler.getVCards();
        } catch (IOException | DavException e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CardCollection#addCard(net.fortuna.ical4j.vcard.VCard)
     */
    public void addCard(VCard card) throws ObjectStoreException, ConstraintViolationException {
        net.fortuna.ical4j.vcard.property.Uid uid = (net.fortuna.ical4j.vcard.property.Uid)card.getProperty(Id.UID);

        String path = getPath();
        if (!path.endsWith("/")) {
            path = path.concat("/");
        }
        PutMethod putMethod = new PutMethod(path + uid.getValue() + ".vcf");
        // putMethod.setAllEtags(true);
        // putMethod.setIfNoneMatch(true);
        // putMethod.setRequestBody(calendar);

        try {
            putMethod.setVCard(card);
        } catch (Exception e) {
            throw new ObjectStoreException("Invalid vcard", e);
        }

        try {
            HttpResponse httpResponse = getStore().getClient().execute(putMethod);
            if ((httpResponse.getStatusLine().getStatusCode() != DavServletResponse.SC_CREATED)
                    && (httpResponse.getStatusLine().getStatusCode() != DavServletResponse.SC_NO_CONTENT)) {
                throw new ObjectStoreException("Error creating calendar on server: " + httpResponse.getStatusLine());
            }
        } catch (IOException ioe) {
            throw new ObjectStoreException("Error creating calendar on server", ioe);
        }        
    }

    @Override
    public VCard removeCard(String uid) throws ObjectNotFoundException, FailedOperationException {
        return null;
    }
}

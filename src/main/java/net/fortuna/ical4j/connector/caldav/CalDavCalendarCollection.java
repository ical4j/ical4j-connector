/*
 * $Id$
 *
 * Created on 24/02/2008
 *
 * Copyright (c) 2008, Ben Fortuna
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
 *  o Neither the id of Ben Fortuna nor the names of any other contributors
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
package net.fortuna.ical4j.connector.caldav;

import java.io.IOException;

import net.fortuna.ical4j.connector.AbstractCalendarCollection;
import net.fortuna.ical4j.connector.MediaType;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.caldav.method.GetMethod;
import net.fortuna.ical4j.connector.caldav.method.MkCalendarMethod;
import net.fortuna.ical4j.connector.caldav.method.PutMethod;
import net.fortuna.ical4j.connector.caldav.property.CalDavPropertyName;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;

/**
 * @author Ben
 *
 */
public class CalDavCalendarCollection extends AbstractCalendarCollection {

    private Log log = LogFactory.getLog(CalDavCalendarCollection.class);

    private CalDavCalendarStore store;
    
    private String id;
    
    private String displayName;
    
    private String description;
    
//    private CalDAV4JMethodFactory methodFactory;
    
//    private CalDAVCalendarCollection delegate;
    
    /**
     * @param store
     * @param path
     */
    public CalDavCalendarCollection(CalDavCalendarStore store, String id) {
        this(store, id, null, null);
    }
    
    /**
     * @param store
     * @param id
     * @param displayName
     * @param description
     */
    public CalDavCalendarCollection(CalDavCalendarStore store, String id, String displayName, String description) {
        this.store = store;
        this.id = id;
        this.displayName = displayName;
        this.description = description;
//        methodFactory = new CalDAV4JMethodFactory();
//        methodFactory.setProdID(store.getProdId());
//        delegate = new CalDAVCalendarCollection(store.getPath() + id,
//                store.getHostConfig(), methodFactory, store.getProdId());
    }
    
    /**
     * Creates this collection on the CalDAV server.
     * @throws IOException 
     * @throws ObjectStoreException 
     */
    void create() throws IOException, ObjectStoreException {
//        delegate.createCalendar(store.getHttpClient());
        
        MkCalendarMethod mkCalendarMethod = new MkCalendarMethod(store.getPath() + id);
        
        DavPropertySet properties = new DavPropertySet();
        properties.add(new DefaultDavProperty(DavPropertyName.DISPLAYNAME, displayName));
        properties.add(new DefaultDavProperty(CalDavPropertyName.CALENDAR_DESCRIPTION, description));
        
        MkCalendar mkcalendar = new MkCalendar();
        mkcalendar.setProperties(properties);
        mkCalendarMethod.setRequestBody(mkcalendar);

        store.getHttpClient().executeMethod(store.getHostConfig(), mkCalendarMethod);
        if (!mkCalendarMethod.succeeded()) {
            throw new ObjectStoreException(mkCalendarMethod.getStatusCode() + ": " + mkCalendarMethod.getStatusText());
        }
    }
    
    /**
     * Removes this collection from the CalDAV server.
     * @throws IOException 
     * @throws HttpException 
     * @throws ObjectStoreException 
     * @throws CalDAV4JException
     */
    void delete() throws HttpException, IOException, ObjectStoreException {
        DeleteMethod deleteMethod = new DeleteMethod(store.getPath() + id);
        store.getHttpClient().executeMethod(store.getHostConfig(), deleteMethod);
        if (!deleteMethod.succeeded()){
            throw new ObjectStoreException(deleteMethod.getStatusCode() + ": " + deleteMethod.getStatusText());
        }
    }
    
    /**
     * @return
     * @throws IOException 
     * @throws HttpException 
     * @throws ObjectStoreException 
     * @throws CalDAV4JException
     */
    boolean exists() throws HttpException, IOException, ObjectStoreException {
        GetMethod getMethod = new GetMethod(store.getPath() + id);
        store.getHttpClient().executeMethod(store.getHostConfig(), getMethod);
        if (getMethod.getStatusCode() == DavServletResponse.SC_OK) {
            return true;
        }
        else if (getMethod.getStatusCode() == DavServletResponse.SC_NOT_FOUND) {
            return false;
        }
        else {
            throw new ObjectStoreException(getMethod.getStatusLine().toString());
        }
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getCalendars()
     */
    public Calendar[] getCalendars() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getDescription()
     */
    public String getDescription() {
        try {
            return getProperty(CalDavPropertyName.CALENDAR_DESCRIPTION);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ObjectStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getDisplayName()
     */
    public String getDisplayName() {
        try {
            return getProperty(DavPropertyName.DISPLAYNAME);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ObjectStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param propertyName
     * @return
     * @throws IOException 
     * @throws ObjectStoreException 
     */
    private String getProperty(DavPropertyName propertyName) throws IOException, ObjectStoreException {
//        Vector properties = new Vector();
//        properties.add(propertyName);
        
//        PropFind propFind = new PropFind();
//        propFind.setPropertyName(propertyName);
        DavPropertyNameSet set = new DavPropertyNameSet();
        set.add(propertyName);
        
        PropFindMethod propFindMethod = new PropFindMethod(store.getPath() + id, set, DavConstants.DEPTH_0);
//        propFindMethod.setPropertyNames(properties.elements());
//        propFindMethod.setDepth(0);
//        propFindMethod.setRequestBody(propFind);
        
        store.getHttpClient().executeMethod(store.getHostConfig(), propFindMethod);
        /*
        if (propFindMethod.getStatusCode() == DavServletResponse.SC_MULTI_STATUS) {
            Enumeration props = propFindMethod.getResponseProperties(store.getPath() + id + "/");
            if (props.hasMoreElements()) {
                return ((BaseProperty) props.nextElement()).getPropertyAsString();
            }
        }
        else {
            throw new CalDAV4JException(
                    "Unexpected Status returned from Server: "
                            + propFindMethod.getStatusCode());
        }
        */
        if (!propFindMethod.succeeded()) {
            throw new ObjectStoreException(propFindMethod.getStatusLine().toString());
        }
        return propFindMethod.getResponseBodyAsString();
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMaxAttendeesPerInstance()
     */
    public int getMaxAttendeesPerInstance() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMaxDateTime()
     */
    public String getMaxDateTime() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMaxInstances()
     */
    public int getMaxInstances() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMaxResourceSize()
     */
    public long getMaxResourceSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMinDateTime()
     */
    public String getMinDateTime() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getSupportedComponentTypes()
     */
    public String[] getSupportedComponentTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getSupportedMediaTypes()
     */
    public MediaType[] getSupportedMediaTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getTimeZone()
     */
    public Calendar getTimeZone() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#addCalendar(net.fortuna.ical4j.model.Calendar)
     */
    public void addCalendar(Calendar calendar) throws ObjectStoreException, ConstraintViolationException {
        Uid uid = Calendars.getUid(calendar);
        if (uid == null) {
            throw new ObjectStoreException("No UID specified in calendar");
        }

        PutMethod putMethod = new PutMethod(store.getPath() + id + "/" + uid.getValue() + ".ics");
//        putMethod.setAllEtags(true);
//        putMethod.setIfNoneMatch(true);
//        putMethod.setRequestBody(calendar);
        
        try {
            putMethod.setCalendar(calendar);
        }
        catch (Exception e) {
            throw new ObjectStoreException("Invalid calendar", e);
        }
        
        try {
            store.getHttpClient().executeMethod(store.getHostConfig(), putMethod);
            if (putMethod.getStatusCode() != DavServletResponse.SC_CREATED) {
                throw new ObjectStoreException("Error creating calendar on server: " + putMethod.getStatusLine());
            }
        }
        catch (IOException ioe) {
            throw new ObjectStoreException("Error creating calendar on server", ioe);
        }
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getCalendar(net.fortuna.ical4j.model.property.Uid)
     */
    public Calendar getCalendar(String uid) {
        /*
        try {
            return delegate.getCalendarForEventUID(store.getHttpClient(), uid);
        }
        catch (CalDAV4JException ce) {
            log.error("Error retrieving calendar [" + uid + "]", ce);
        }
        */
        GetMethod method = new GetMethod(store.getPath() + id + "/" + uid + ".ics");
        try {
            store.getHttpClient().executeMethod(store.getHostConfig(), method);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (method.getStatusCode() == DavServletResponse.SC_OK) {
            try {
                return method.getCalendar();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (ParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (method.getStatusCode() == DavServletResponse.SC_NOT_FOUND) {
            return null;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#removeCalendar(net.fortuna.ical4j.model.property.Uid)
     */
    public Calendar removeCalendar(String uid) throws ObjectStoreException {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#export()
     */
    public Calendar export() throws ObjectStoreException {
        // TODO Auto-generated method stub
        return null;
    }
}

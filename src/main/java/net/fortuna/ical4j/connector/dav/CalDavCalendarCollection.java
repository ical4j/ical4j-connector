/*
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
package net.fortuna.ical4j.connector.dav;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.connector.CalendarCollection;
import net.fortuna.ical4j.connector.MediaType;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.method.GetMethod;
import net.fortuna.ical4j.connector.dav.method.MkCalendarMethod;
import net.fortuna.ical4j.connector.dav.method.PutMethod;
import net.fortuna.ical4j.connector.dav.property.CalDavPropertyName;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.w3c.dom.Node;

/**
 * $Id$
 *
 * Created on 24/02/2008
 *
 * @author Ben
 *
 */
public class CalDavCalendarCollection extends AbstractDavObjectCollection implements CalendarCollection {

    private Log log = LogFactory.getLog(CalDavCalendarCollection.class);
    
    private String displayName;
    
    private String description;
    
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
    	super(store, id);
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Creates this collection on the CalDAV server.
     * @throws IOException 
     * @throws ObjectStoreException 
     */
    void create() throws IOException, ObjectStoreException {
        MkCalendarMethod mkCalendarMethod = new MkCalendarMethod(getPath());
        
        DavPropertySet properties = new DavPropertySet();
        properties.add(new DefaultDavProperty(DavPropertyName.DISPLAYNAME, displayName));
        properties.add(new DefaultDavProperty(CalDavPropertyName.CALENDAR_DESCRIPTION, description));
        
        MkCalendar mkcalendar = new MkCalendar();
        mkcalendar.setProperties(properties);
        mkCalendarMethod.setRequestBody(mkcalendar);

        getStore().execute(mkCalendarMethod);
        if (!mkCalendarMethod.succeeded()) {
            throw new ObjectStoreException(mkCalendarMethod.getStatusCode() + ": " + mkCalendarMethod.getStatusText());
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
            return getProperty(CalDavPropertyName.CALENDAR_DESCRIPTION, String.class);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ObjectStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (DavException e) {
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
            return getProperty(DavPropertyName.DISPLAYNAME, String.class);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ObjectStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (DavException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMaxAttendeesPerInstance()
     */
    public Integer getMaxAttendeesPerInstance() {
        try {
            return getProperty(CalDavPropertyName.MAX_ATTENDEES_PER_INSTANCE, Integer.class);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ObjectStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (DavException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMaxDateTime()
     */
    public String getMaxDateTime() {
        try {
            return getProperty(CalDavPropertyName.MAX_DATE_TIME, String.class);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ObjectStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (DavException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMaxInstances()
     */
    public Integer getMaxInstances() {
        try {
            return getProperty(CalDavPropertyName.MAX_INSTANCES, Integer.class);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ObjectStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (DavException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMaxResourceSize()
     */
    public long getMaxResourceSize() {
        try {
            return getProperty(CalDavPropertyName.MAX_RESOURCE_SIZE, Long.class);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ObjectStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (DavException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMinDateTime()
     */
    public String getMinDateTime() {
        try {
            return getProperty(CalDavPropertyName.MIN_DATE_TIME, String.class);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ObjectStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (DavException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getSupportedComponentTypes()
     */
    public String[] getSupportedComponentTypes() {
        try {
            List<Node> supportedComponentNodes = getProperty(CalDavPropertyName.SUPPORTED_CALENDAR_COMPONENT_SET, ArrayList.class);
            List<String> supportedComponents = new ArrayList<String>();
            for (Node node : supportedComponentNodes) {
                if (node.getAttributes() != null) {
                    Node nameNode = node.getAttributes().getNamedItemNS(CalDavConstants.NAMESPACE.getURI(), "name");
                    if (nameNode != null) {
                        supportedComponents.add(nameNode.getTextContent());
                    }
                }
            }
            return supportedComponents.toArray(new String[supportedComponents.size()]);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ObjectStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (DavException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

        PutMethod putMethod = new PutMethod(getPath() + "/" + uid.getValue() + ".ics");
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
        	getStore().execute(putMethod);
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
        GetMethod method = new GetMethod(getPath() + "/" + uid + ".ics");
        try {
            getStore().execute(method);
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
        Calendar calendar = getCalendar(uid);
        
        DeleteMethod deleteMethod = new DeleteMethod(getPath() + "/" + uid + ".ics");
        try {
            getStore().execute(deleteMethod);
        }
        catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (!deleteMethod.succeeded()) {
            throw new ObjectStoreException(deleteMethod.getStatusLine().toString());
        }
        
        return calendar;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#merge(net.fortuna.ical4j.model.Calendar)
     */
    public final void merge(Calendar calendar) throws ObjectStoreException {
        try {
            Calendar[] uidCalendars = Calendars.split(calendar);
            for (int i = 0; i < uidCalendars.length; i++) {
                addCalendar(uidCalendars[i]);
            }
        }
        catch (ConstraintViolationException cve) {
            throw new ObjectStoreException("Invalid calendar format", cve);
        }
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#export()
     */
    public Calendar export() throws ObjectStoreException {
        // TODO Auto-generated method stub
        return null;
    }
}

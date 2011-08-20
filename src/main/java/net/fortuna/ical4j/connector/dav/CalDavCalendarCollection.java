/**
 * Copyright (c) 2011, Ben Fortuna
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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.fortuna.ical4j.connector.CalendarCollection;
import net.fortuna.ical4j.connector.FailedOperationException;
import net.fortuna.ical4j.connector.MediaType;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.method.GetMethod;
import net.fortuna.ical4j.connector.dav.method.MkCalendarMethod;
import net.fortuna.ical4j.connector.dav.method.PutMethod;
import net.fortuna.ical4j.connector.dav.method.ReportMethod;
import net.fortuna.ical4j.connector.dav.property.CalDavPropertyName;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.DOMException;
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
public class CalDavCalendarCollection extends AbstractDavObjectCollection<Calendar> implements CalendarCollection {
    
    private String displayName;
    
    private String description;
    
    /**
     * Only {@link CalDavCalendarStore} should be calling this, so default modifier is applied.
     * @param calDavCalendarStore
     * @param path
     */
    CalDavCalendarCollection(CalDavCalendarStore calDavCalendarStore, String id) {
        this(calDavCalendarStore, id, null, null);
    }
    
    /**
     * Only {@link CalDavCalendarStore} should be calling this, so default modifier is applied.
     * @param calDavCalendarStore
     * @param id
     * @param displayName
     * @param description
     */
    CalDavCalendarCollection(CalDavCalendarStore calDavCalendarStore,
            String id, String displayName, String description) {
        
    	super(calDavCalendarStore, id);
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Creates this collection on the CalDAV server.
     * @throws IOException 
     * @throws ObjectStoreException 
     */
    final void create() throws IOException, ObjectStoreException {
        MkCalendarMethod mkCalendarMethod = new MkCalendarMethod(getPath());
        
        DavPropertySet properties = new DavPropertySet();
        properties.add(new DefaultDavProperty(DavPropertyName.DISPLAYNAME, displayName));
        properties.add(new DefaultDavProperty(CalDavPropertyName.CALENDAR_DESCRIPTION, description));
        
        MkCalendar mkcalendar = new MkCalendar();
        mkcalendar.setProperties(properties);
        mkCalendarMethod.setRequestBody(mkcalendar);

        getStore().getClient().execute(mkCalendarMethod);
        if (!mkCalendarMethod.succeeded()) {
            throw new ObjectStoreException(mkCalendarMethod.getStatusCode() + ": " + mkCalendarMethod.getStatusText());
        }
    }
    
    /**
     * @return an array of calendar objects
     * @deprecated Use the getEvents() method
     * @see net.fortuna.ical4j.connector.CalendarCollection#getCalendars()
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
     * @param componentType the type of component
     * @return and array of calendar objects
     */
    public Calendar[] getComponentsByType(String componentType) {
        try {
            DavPropertyNameSet properties = new DavPropertyNameSet();
            properties.add(DavPropertyName.GETETAG);
            properties.add(CalDavPropertyName.CALENDAR_DATA);
            
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element filter = DomUtil.createElement(document, "filter", CalDavConstants.NAMESPACE); 
            Element calFilter = DomUtil.createElement(document, "comp-filter", CalDavConstants.NAMESPACE); 
            calFilter.setAttribute("name", "VCALENDAR"); 
            Element eventFilter = DomUtil.createElement(document, "comp-filter", CalDavConstants.NAMESPACE); 
            eventFilter.setAttribute("name", componentType); 
            calFilter.appendChild(eventFilter); 
            filter.appendChild(calFilter);
            
            ReportInfo info = new ReportInfo(ReportMethod.CALENDAR_QUERY, 1, properties);
            info.setContentElement(filter);
            
            ReportMethod method = new ReportMethod(getPath(), info);
            getStore().getClient().execute(method);
            if (method.getStatusCode() == DavServletResponse.SC_MULTI_STATUS) {
                return method.getCalendars();
            }
            else if (method.getStatusCode() == DavServletResponse.SC_NOT_FOUND) {
                return new Calendar[0];
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (DOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (DavException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new Calendar[0];
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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
    
    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    public String[] getSupportedComponentTypes() {
        try {
            List<Node> supportedComponentNodes = getProperty(CalDavPropertyName.SUPPORTED_CALENDAR_COMPONENT_SET,
                    ArrayList.class);
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
        return new String[0];
    }

    /**
     * {@inheritDoc}
     */
    public MediaType[] getSupportedMediaTypes() {
        // TODO Auto-generated method stub
        return new MediaType[0];
    }

    /**
     * {@inheritDoc}
     */
    public Calendar getTimeZone() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void addCalendar(Calendar calendar) throws ObjectStoreException, ConstraintViolationException {
        Uid uid = Calendars.getUid(calendar);

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
            getStore().getClient().execute(putMethod);
            if (putMethod.getStatusCode() != DavServletResponse.SC_CREATED) {
                throw new ObjectStoreException("Error creating calendar on server: " + putMethod.getStatusLine());
            }
        }
        catch (IOException ioe) {
            throw new ObjectStoreException("Error creating calendar on server", ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Calendar getCalendar(String uid) {
        GetMethod method = new GetMethod(getPath() + "/" + uid + ".ics");
        try {
            getStore().getClient().execute(method);
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

    /**
     * {@inheritDoc}
     */
    public Calendar removeCalendar(String uid) throws FailedOperationException, ObjectStoreException {
        Calendar calendar = getCalendar(uid);
        
        DeleteMethod deleteMethod = new DeleteMethod(getPath() + "/" + uid + ".ics");
        try {
            getStore().getClient().execute(deleteMethod);
        }
        catch (IOException e) {
        	throw new ObjectStoreException(e);
        }
        if (!deleteMethod.succeeded()) {
            throw new FailedOperationException(deleteMethod.getStatusLine().toString());
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
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
	public Calendar[] getComponents() throws ObjectStoreException {
		return getComponentsByType("VEVENT");
	}
}

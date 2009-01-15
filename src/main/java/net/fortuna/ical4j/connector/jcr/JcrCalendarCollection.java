/*
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

package net.fortuna.ical4j.connector.jcr;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import net.fortuna.ical4j.connector.CalendarCollection;
import net.fortuna.ical4j.connector.MediaType;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jcrom.AbstractJcrEntity;
import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;
import org.jcrom.Jcrom;
import org.jcrom.JcrDataProvider.TYPE;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrProperty;

/**
 * $Id$
 *
 * Created on: 15/01/2009
 *
 * @author Ben
 *
 */
public class JcrCalendarCollection extends AbstractJcrEntity implements CalendarCollection {

    /**
     * 
     */
    private static final long serialVersionUID = -3063963527215302278L;

    private static final Log LOG = LogFactory.getLog(JcrCalendarCollection.class);
    
    private Session session;
    
    private Jcrom jcrom;
    
    @JcrFileNode(lazy=true) private Map<String, JcrFile> files;
    
    @JcrProperty private Integer maxAttendeesPerInstance;

    @JcrProperty private Date maxDateTime;
    
    @JcrProperty private Integer maxInstances;
    
    @JcrProperty private long maxResourceSize;

    @JcrProperty private Date minDateTime;

    @JcrProperty private String description;

    @JcrProperty private String displayName;
    
    /**
     * @param jcrom
     * @param node
     */
    public JcrCalendarCollection() {
        files = new HashMap<String, JcrFile>();
    }
    
    /**
     * @param session the session to set
     */
    public final void setSession(Session session) {
        this.session = session;
    }

    /**
     * @param jcrom the jcrom to set
     */
    public final void setJcrom(Jcrom jcrom) {
        this.jcrom = jcrom;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#addCalendar(net.fortuna.ical4j.model.Calendar)
     */
    @Override
    public void addCalendar(Calendar calendar) throws ObjectStoreException, ConstraintViolationException {
        addCalendar(calendar, true);
    }
    
    /**
     * @param calendar
     * @param saveChanges
     * @throws ObjectStoreException
     * @throws ConstraintViolationException
     */
    private void addCalendar(Calendar calendar, boolean saveChanges) throws ObjectStoreException, ConstraintViolationException {
        Uid uid = Calendars.getUid(calendar);
        JcrFile file = new JcrFile();
        file.setName("calendarData");
        file.setDataProvider(new JcrDataProviderImpl(TYPE.BYTES, calendar.toString().getBytes()));
        file.setMimeType(MediaType.ICALENDAR_2_0.getContentType());
        file.setLastModified(java.util.Calendar.getInstance());
        files.put(uid.getValue(), file);
        if (saveChanges) {
            saveChanges();
        }
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#export()
     */
    @Override
    public Calendar export() throws ObjectStoreException {
        Calendar exported = new Calendar();
        for (JcrFile file : files.values()) {
            CalendarBuilder builder = new CalendarBuilder();
            try {
                Calendar calendar = builder.build(file.getDataProvider().getInputStream());
                exported = Calendars.merge(exported, calendar);
            }
            catch (Exception e) {
                throw new ObjectStoreException("Unexpected error", e);
            }
        }
        return exported;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getCalendar(java.lang.String)
     */
    @Override
    public Calendar getCalendar(String uid) {
        CalendarBuilder builder = new CalendarBuilder();
        try {
            JcrFile file = files.get(uid);
            return builder.build(new ByteArrayInputStream(file.getDataProvider().getBytes()));
        }
        catch (Exception e) {
            LOG.error("Unexpected error", e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getCalendars()
     */
    @Override
    public Calendar[] getCalendars() {
        List<Calendar> calendars = new ArrayList<Calendar>();
        for (JcrFile file : files.values()) {
            CalendarBuilder builder = new CalendarBuilder();
            try {
                calendars.add(builder.build(new ByteArrayInputStream(file.getDataProvider().getBytes())));
            }
            catch (Exception e) {
                LOG.error("Unexpected error", e);
            }
        }
        return calendars.toArray(new Calendar[calendars.size()]);
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMaxAttendeesPerInstance()
     */
    @Override
    public Integer getMaxAttendeesPerInstance() {
        return maxAttendeesPerInstance;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMaxDateTime()
     */
    @Override
    public String getMaxDateTime() {
        return maxDateTime.toString();
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMaxInstances()
     */
    @Override
    public Integer getMaxInstances() {
        return maxInstances;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMaxResourceSize()
     */
    @Override
    public long getMaxResourceSize() {
        return maxResourceSize;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getMinDateTime()
     */
    @Override
    public String getMinDateTime() {
        return minDateTime.toString();
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getSupportedComponentTypes()
     */
    @Override
    public String[] getSupportedComponentTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getSupportedMediaTypes()
     */
    @Override
    public MediaType[] getSupportedMediaTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getTimeZone()
     */
    @Override
    public Calendar getTimeZone() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#removeCalendar(java.lang.String)
     */
    @Override
    public Calendar removeCalendar(String uid) throws ObjectStoreException {
        Calendar calendar = getCalendar(uid);
        files.remove(uid);
        saveChanges();
        return calendar;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectCollection#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectCollection#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#merge(net.fortuna.ical4j.model.Calendar)
     */
    @Override
    public void merge(Calendar calendar) throws ObjectStoreException {
        try {
            Calendar[] uidCalendars = Calendars.split(calendar);
            for (int i = 0; i < uidCalendars.length; i++) {
                addCalendar(uidCalendars[i], false);
            }
            saveChanges();
        }
        catch (ConstraintViolationException cve) {
            throw new ObjectStoreException("Invalid calendar format", cve);
        }
    }

    /**
     * @param maxAttendeesPerInstance the maxAttendeesPerInstance to set
     */
    final void setMaxAttendeesPerInstance(Integer maxAttendeesPerInstance) {
        this.maxAttendeesPerInstance = maxAttendeesPerInstance;
    }

    /**
     * @param maxDateTime the maxDateTime to set
     */
    final void setMaxDateTime(Date maxDateTime) {
        this.maxDateTime = maxDateTime;
    }

    /**
     * @param maxInstances the maxInstances to set
     */
    final void setMaxInstances(Integer maxInstances) {
        this.maxInstances = maxInstances;
    }

    /**
     * @param maxResourceSize the maxResourceSize to set
     */
    final void setMaxResourceSize(long maxResourceSize) {
        this.maxResourceSize = maxResourceSize;
    }

    /**
     * @param minDateTime the minDateTime to set
     */
    final void setMinDateTime(Date minDateTime) {
        this.minDateTime = minDateTime;
    }

    /**
     * @param description the description to set
     */
    final void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param displayName the displayName to set
     */
    final void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * @throws ObjectStoreException
     */
    private void saveChanges() throws ObjectStoreException {
        try {
            Node node = session.getRootNode().getNode(jcrom.getPath(this).substring(1));
            jcrom.updateNode(node, this);
            node.save();
        }
        catch (RepositoryException e) {
            throw new ObjectStoreException("Unexpected error", e);
        }
    }
}

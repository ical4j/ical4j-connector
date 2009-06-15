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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import net.fortuna.ical4j.connector.CalendarCollection;
import net.fortuna.ical4j.connector.MediaType;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jcrom.annotations.JcrProperty;

/**
 * $Id$
 *
 * Created on: 15/01/2009
 *
 * @author Ben
 *
 */
public class JcrCalendarCollection extends AbstractJcrObjectCollection<Calendar> implements CalendarCollection {

    /**
     * 
     */
    private static final long serialVersionUID = -3063963527215302278L;

    private static final Log LOG = LogFactory.getLog(JcrCalendarCollection.class);

//    @JcrChildNode private List<JcrCalendar> calendars;
    
    @JcrProperty private Integer maxAttendeesPerInstance;

    @JcrProperty private Date maxDateTime;
    
    @JcrProperty private Integer maxInstances;
    
    @JcrProperty private long maxResourceSize;

    @JcrProperty private Date minDateTime;
    
    private JcrCalendarDao calendarDao;
    
    private JcrCalendarCollectionDao collectionDao;
    
    /**
     * @param jcrom
     * @param node
     */
    public JcrCalendarCollection() {
//        calendars = new HashMap<String, Object>();
//        calendars = new ArrayList<JcrCalendar>();
    }
    
    /**
     * @return
     */
    private JcrCalendarDao getCalendarDao() {
        if (calendarDao == null) {
//            synchronized (this) {
//                if (calendarDao == null) {
                    calendarDao = new JcrCalendarDao(getStore().getSession(), getStore().getJcrom());
//                }
//            }
        }
        return calendarDao;
    }
    
    /**
     * @return
     */
    private JcrCalendarCollectionDao getCollectionDao() {
        if (collectionDao == null) {
//            synchronized (this) {
//                if (collectionDao == null) {
                    collectionDao = new JcrCalendarCollectionDao(getStore().getSession(), getStore().getJcrom());
//                }
//            }
        }
        return collectionDao;
    }
    
    /**
     * {@inheritDoc}
     */
    public void addCalendar(Calendar calendar) throws ObjectStoreException, ConstraintViolationException {
        addCalendar(calendar, true);
    }
    
    private void addCalendar(Calendar calendar, boolean saveChanges)
        throws ObjectStoreException, ConstraintViolationException {
//        calendars.put(jcrCal.getName(), jcrCal);
//        calendars.add(jcrCal);
        
        // initialise calendars node..
        try {
            try {
                getNode().getNode("calendars");
            }
            catch (PathNotFoundException e) {
                getNode().addNode("calendars");
            }
        }
        catch (RepositoryException e) {
            throw new ObjectStoreException("Unexpected error", e);
        }
        
        JcrCalendar jcrCal = null;
        boolean update = false;
        
        Uid uid = Calendars.getUid(calendar);
        if (uid != null) {
            List<JcrCalendar> jcrCalendars = getCalendarDao().findByUid(
                    getStore().getJcrom().getPath(this) + "/calendars", uid.getValue());
            if (!jcrCalendars.isEmpty()) {
                jcrCal = jcrCalendars.get(0);
                update = true;
            }
        }
        
        if (jcrCal == null) {
            jcrCal = new JcrCalendar();
        }
        
        jcrCal.setCalendar(calendar);
        
        if (update) {
            getCalendarDao().update(jcrCal);
        }
        else {
            getCalendarDao().create(getStore().getJcrom().getPath(this) + "/calendars", jcrCal);
        }
//        try {
//            store.getJcrom().addNode(getNode(), jcrCal);
//        }
//        catch (RepositoryException e) {
//            throw new ObjectStoreException("Unexpected error", e);
//        }
        if (saveChanges) {
            saveChanges();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Calendar export() throws ObjectStoreException {
        Calendar exported = new Calendar();
        List<JcrCalendar> calendars = getCalendarDao().findAll(getStore().getJcrom().getPath(this) + "/calendars");
        for (JcrCalendar jcrCal : calendars) {
            try {
//            NodeIterator childNodes = getNode().getNodes();
//            while (childNodes.hasNext()) {
//                JcrCalendar jcrCal = store.getJcrom().fromNode(JcrCalendar.class, (Node) childNodes.next());
                exported = Calendars.merge(exported, jcrCal.getCalendar());
//            }
            }
            catch (Exception e) {
                throw new ObjectStoreException("Unexpected error", e);
            }
        }
        return exported;
    }

    /**
     * {@inheritDoc}
     */
    public Calendar getCalendar(String uid) {
        try {
//            JcrCalendar jcrCal = (JcrCalendar) calendars.get(uid);
//            JcrCalendar jcrCal = getStore().getJcrom().fromNode(
//            JcrCalendar.class, getNode().getNode("calendars").getNode(uid));
//            if (jcrCal != null) {
//                return jcrCal.getCalendar();
//            }
            List<JcrCalendar> calendars = getCalendarDao().findByUid(
                    getStore().getJcrom().getPath(this) + "/calendars", uid);
//            for (JcrCalendar jcrCal : calendars) {
//                if (uid.equals(jcrCal.getUid().getValue())) {
//                    return jcrCal.getCalendar();
//                }
//            }
            if (!calendars.isEmpty()) {
                return calendars.get(0).getCalendar();
            }
        }
        catch (Exception e) {
            LOG.error("Unexpected error", e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Calendar[] getComponents() throws ObjectStoreException {
        List<Calendar> retVal = new ArrayList<Calendar>();
//        for (Object jcrCal : calendars.values()) {
//        NodeIterator childNodes;
//        try {
//            childNodes = getNode().getNodes();
//            while (childNodes.hasNext()) {
//                JcrCalendar jcrCal = store.getJcrom().fromNode(JcrCalendar.class, (Node) childNodes.next());
        List<JcrCalendar> calendars = getCalendarDao().findAll(getStore().getJcrom().getPath(this) + "/calendars");
        for (JcrCalendar jcrCal : calendars) {
            try {
                retVal.add(jcrCal.getCalendar());
            }
            catch (Exception e) {
                LOG.error("Unexpected error", e);
            }
        }
//        }
//        catch (RepositoryException e1) {
//            throw new ObjectStoreException("Unexpected error", e1);
//        }
        return retVal.toArray(new Calendar[retVal.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public Integer getMaxAttendeesPerInstance() {
        return maxAttendeesPerInstance;
    }

    /**
     * {@inheritDoc}
     */
    public String getMaxDateTime() {
        if (maxDateTime != null) {
            return maxDateTime.toString();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Integer getMaxInstances() {
        return maxInstances;
    }

    /**
     * {@inheritDoc}
     */
    public long getMaxResourceSize() {
        return maxResourceSize;
    }

    /**
     * {@inheritDoc}
     */
    public String getMinDateTime() {
        if (minDateTime != null) {
            return minDateTime.toString();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getSupportedComponentTypes() {
        // TODO Auto-generated method stub
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
    public Calendar removeCalendar(String uid) throws ObjectStoreException {
        Calendar calendar = getCalendar(uid);

        List<JcrCalendar> calendars = getCalendarDao().findByUid(
                getStore().getJcrom().getPath(this) + "/calendars", uid);
        if (calendars.size() > 0) {
            getCalendarDao().remove(getStore().getJcrom().getPath(calendars.get(0)));
        }
        saveChanges();
        return calendar;
    }

    /**
     * {@inheritDoc}
     */
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
     * @throws ObjectStoreException
     */
    private void saveChanges() throws ObjectStoreException {
        getCollectionDao().update(this);
    }
}

/*
 * $Id$
 *
 * Created on 20/02/2008
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
package net.fortuna.ical4j.connector.jcr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import net.fortuna.ical4j.connector.AbstractCalendarCollection;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.MediaType;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Ben
 *
 */
public class RepositoryCalendarCollection extends AbstractCalendarCollection {
    
    private Log log = LogFactory.getLog(RepositoryCalendarCollection.class);

    private static final String[] SUPPORTED_COMPONENT_TYPES = {
        Component.VEVENT, Component.VTODO
    };
    
    private static final MediaType[] SUPPORTED_MEDIA_TYPES = {
        MediaType.ICALENDAR_2_0
    };

    private Node node;
    
    private CalendarOutputter outputter;
    
    /**
     * @param node
     */
    public RepositoryCalendarCollection(Node node) {
        this.node = node;
        outputter = new CalendarOutputter();
    }

    /**
     * @return
     */
    private String getId() {
        try {
            return node.getProperty(NodeProperty.COLLECTION_ID.getPropertyName()).getString();
        }
        catch (RepositoryException re) {
            log.error("Error retieving collection description", re);
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarCollection#getDescription()
     */
    public String getDescription() {
        try {
            return node.getProperty(NodeProperty.DESCRIPTION.getPropertyName()).getString();
        }
        catch (RepositoryException re) {
            log.error("Error retieving collection description", re);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarCollection#getDisplayName()
     */
    public String getDisplayName() {
        try {
            return node.getProperty(NodeProperty.DISPLAY_NAME.getPropertyName()).getString();
        }
        catch (RepositoryException re) {
            log.error("Error retieving collection display id", re);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarCollection#getMaxAttendeesPerInstance()
     */
    public Integer getMaxAttendeesPerInstance() {
        try {
            return (int) node.getProperty(NodeProperty.MAX_ATTENDEES.getPropertyName()).getLong();
        }
        catch (RepositoryException re) {
            log.error("Error retieving maximum attendees", re);
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarCollection#getMaxDateTime()
     */
    public String getMaxDateTime() {
        try {
            return node.getProperty(NodeProperty.MAX_DATE_TIME.getPropertyName()).getString();
        }
        catch (RepositoryException re) {
            log.error("Error retieving maximum date-time", re);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarCollection#getMaxInstances()
     */
    public Integer getMaxInstances() {
        try {
            return (int) node.getProperty(NodeProperty.MAX_INSTANCES.getPropertyName()).getLong();
        }
        catch (RepositoryException re) {
            log.error("Error retieving maximum instances", re);
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarCollection#getMaxResourceSize()
     */
    public long getMaxResourceSize() {
        try {
            return node.getProperty(NodeProperty.MAX_RESOURCE_SIZE.getPropertyName()).getLong();
        }
        catch (RepositoryException re) {
            log.error("Error retieving maximum resource size", re);
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarCollection#getMinDateTime()
     */
    public String getMinDateTime() {
        try {
            return node.getProperty(NodeProperty.MIN_DATE_TIME.getPropertyName()).getString();
        }
        catch (RepositoryException re) {
            log.error("Error retieving minimum date-time", re);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarCollection#getSupportedComponentTypes()
     */
    public String[] getSupportedComponentTypes() {
        return SUPPORTED_COMPONENT_TYPES;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarCollection#getSupportedMediaTypes()
     */
    public MediaType[] getSupportedMediaTypes() {
        return SUPPORTED_MEDIA_TYPES;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarCollection#getTimeZone()
     */
    public Calendar getTimeZone() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarCollection#setDescription(java.lang.String)
     */
    void setDescription(String description) {
        try {
            node.setProperty(NodeProperty.DESCRIPTION.getPropertyName(), description);
//            node.save();
        }
        catch (RepositoryException re) {
            log.error("Error updating description", re);
        }
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarCollection#setDisplayName(java.lang.String)
     */
    void setDisplayName(String name) {
        try {
            node.setProperty(NodeProperty.DISPLAY_NAME.getPropertyName(), name);
//            node.save();
        }
        catch (RepositoryException re) {
            log.error("Error updating display id", re);
        }
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarCollection#setTimeZone(net.fortuna.ical4j.model.Calendar)
     */
    void setTimeZone(Calendar timezone) {
        // TODO Auto-generated method stub

    }

    /**
     * @param componentTypes
     */
    void setSupportedComponentTypes(String[] componentTypes) {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * @return the node
     */
    final Node getNode() {
        return node;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#addCalendar(net.fortuna.ical4j.model.Calendar)
     */
    public void addCalendar(Calendar calendar) throws ObjectStoreException, ConstraintViolationException {
        try {
            Uid uid = Calendars.getUid(calendar);
            if (uid == null) {
                throw new ConstraintViolationException("Calendar must specify a uniquie identifier (UID)");
            }
            Node calendarNode = node.addNode(NodeType.CALENDAR.getNodeName());
            calendarNode.setProperty(NodeProperty.UID.getPropertyName(), uid.getValue());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            outputter.output(calendar, out);
            calendarNode.setProperty(NodeType.CONTENT.getNodeName(), new ByteArrayInputStream(out.toByteArray()));
            node.save();
        }
        catch (IOException ioe) {
            throw new ObjectStoreException("Error serialising calendar", ioe);
        }
        catch (ValidationException ve) {
            throw new ObjectStoreException("Invalid calendar", ve);
        }
        catch (RepositoryException re) {
            throw new ObjectStoreException("Error adding calendar", re);
        }
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarCollection#getCalendar(net.fortuna.ical4j.model.property.Uid)
     */
    public Calendar getCalendar(String uid) {
        try {
            String queryString = "//" + NodeType.COLLECTION.getNodeName()
                + "[@" + NodeProperty.COLLECTION_ID.getPropertyName() + "='" + getId() + "']/" + NodeType.CALENDAR.getNodeName() + "[@" + NodeProperty.UID + "='" + uid + "']";

            QueryManager qm = node.getSession().getWorkspace().getQueryManager();
            Query folderQuery = qm.createQuery(queryString, Query.XPATH);
            NodeIterator nodes = folderQuery.execute().getNodes();
            if (nodes.hasNext()) {
                return buildCalendar(nodes.nextNode());
            }
        }
        catch (RepositoryException re) {
            log.error("Error retrieving calendar [" + uid + "]", re);
        }
        catch (ParserException pe) {
            log.error("Error deserialising calendar [" + uid + "]", pe);
        }
        catch (IOException ioe) {
            log.error("Error deserialising calendar [" + uid + "]", ioe);
        }
        return null;
    }

    /**
     * @param node
     * @return
     * @throws RepositoryException
     */
    private Calendar buildCalendar(Node node) throws RepositoryException, ParserException, IOException {
        CalendarBuilder b = new CalendarBuilder();
        return b.build(node.getProperty(NodeType.CONTENT.getNodeName()).getStream());
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
        Calendar exported = new Calendar();
        
        try {
            for (NodeIterator ni = node.getNodes(NodeType.CALENDAR.getNodeName()); ni.hasNext();) {
                Calendar c = buildCalendar(ni.nextNode());
                exported.getComponents().addAll(c.getComponents());
            }
        }
        catch (RepositoryException re) {
            throw new ObjectStoreException("Error exporting collection", re);
        }
        catch (IOException ioe) {
            throw new ObjectStoreException("Error exporting collection", ioe);
        }
        catch (ParserException pe) {
            throw new ObjectStoreException("Error exporting collection", pe);
        }
        return exported;
    }
}

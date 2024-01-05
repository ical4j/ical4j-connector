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
package org.ical4j.connector.jcr;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.parameter.FmtType;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Attach;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jcrom.AbstractJcrEntity;
import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrProperty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 
 *
 * @author Ben
 *
 * Created on: 17/01/2009
 *
 * $Id$
 */
//@JcrNode(classNameProperty="className")
public class JcrCalendar extends AbstractJcrEntity {

    private static final Log LOG = LogFactory.getLog(JcrCalendar.class);
    
    /**
     * 
     */
    private static final long serialVersionUID = -2711620225884358385L;
    
    @JcrFileNode private JcrFile file;
    
    @JcrProperty private String uid;
    
    @JcrProperty private String summary;
    
    @JcrFileNode private JcrFile description;
    
    @JcrFileNode private final List<JcrFile> attachments;

    private Calendar calendar;

    /**
     * 
     */
    public JcrCalendar() {
        attachments = new ArrayList<JcrFile>();
    }
    
    /**
     * @return the calendar
     * @throws ParserException where calendar parsing fails
     * @throws IOException where a communication error occurs
     */
    public final Calendar getCalendar() throws IOException, ParserException {
        if (calendar == null) {
            CalendarBuilder builder = new CalendarBuilder();
//            calendar = builder.build(new ByteArrayInputStream(file.getDataProvider().getBytes()));
            calendar = builder.build(file.getDataProvider().getInputStream());
        }
        return calendar;
    }

    /**
     * @param calendar the calendar to set
     */
    public final void setCalendar(final Calendar calendar) {
        this.calendar = calendar;
        
        try {
            Uid uidProp = Calendars.getUid(calendar);
            this.uid = uidProp.getValue();
            setName(uidProp.getValue());
        }
        catch (ConstraintViolationException e) {
            LOG.error("Invalid UID", e);
            setName("calendar");
        }
        
        file = new JcrFile();
        file.setName("data");
        file.setDataProvider(new JcrDataProviderImpl(calendar.toString().getBytes()));
//        file.setMimeType(MediaType.ICALENDAR_2_0.getContentType());
        file.setMimeType(Calendars.getContentType(calendar, null));
        file.setLastModified(java.util.Calendar.getInstance());
        
        for (Object component : calendar.getComponents()) {
            
            // save first available summary..
            if (summary == null) {
                Optional<Summary> summaryProp = ((Component) component).getProperty(Property.SUMMARY);
                if (summaryProp.isPresent()) {
                    this.summary = summaryProp.get().getValue();
                }
            }
            
            // save first available description..
            if (description == null) {
                Optional<Description> descriptionProp = ((Component) component).getProperty(Property.DESCRIPTION);
                if (descriptionProp.isPresent()) {
                    description = new JcrFile();
                    description.setName("text");
                    description.setMimeType("text/plain");
                    description.setDataProvider(new JcrDataProviderImpl(descriptionProp.get().getValue().getBytes()));
                    description.setLastModified(java.util.Calendar.getInstance());
                }
            }
            
            // save attachments..
            attachments.clear();
            List<Attach> attachments = ((Component) component).getProperties(Property.ATTACH);
            for (Attach attach : attachments) {
                try {
                    JcrFile attachment = new JcrFile();
                    attachment.setName("attachment");
                    if (Value.BINARY.equals(attach.getParameter(Parameter.VALUE))) {
                        attachment.setDataProvider(new JcrDataProviderImpl(((Attach) attach).getBinary()));
                        Optional<FmtType> contentType = attach.getParameter(Parameter.FMTTYPE);
                        if (contentType.isPresent()) {
                            attachment.setMimeType(contentType.get().getValue());
                        }
                    }
                    else {
                        ByteArrayOutputStream aout = new ByteArrayOutputStream();
                        IOUtils.copy(((Attach) attach).getUri().toURL().openStream(), aout);
                        attachment.setDataProvider(new JcrDataProviderImpl(aout.toByteArray()));
                    }
                    attachment.setLastModified(java.util.Calendar.getInstance());
                    this.attachments.add(attachment);
                }
                catch (Exception e) {
                    LOG.error("Error saving attachment", e);
                }
            }
        }
    }

    /**
     * @return the uid
     */
    public final String getUid() {
        return uid;
    }
}

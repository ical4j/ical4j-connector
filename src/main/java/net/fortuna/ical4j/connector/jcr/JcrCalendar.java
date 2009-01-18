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

import java.io.IOException;

import net.fortuna.ical4j.connector.MediaType;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jcrom.AbstractJcrEntity;
import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;
import org.jcrom.JcrDataProvider.TYPE;
import org.jcrom.annotations.JcrFileNode;

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

    private Calendar calendar;

    /**
     * @return
     */
    public final Uid getUid() {
        try {
            return Calendars.getUid(getCalendar());
        }
        catch (Exception e) {
            LOG.error("Unexcepted error", e);
        }
        return null;
    }
    
    /**
     * @return the calendar
     * @throws ParserException 
     * @throws IOException 
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
        
        file = new JcrFile();
        file.setName("calendarData");
        file.setDataProvider(new JcrDataProviderImpl(TYPE.BYTES, calendar.toString().getBytes()));
        file.setMimeType(MediaType.ICALENDAR_2_0.getContentType());
        file.setLastModified(java.util.Calendar.getInstance());
        setName(getUid().getValue());
    }
}

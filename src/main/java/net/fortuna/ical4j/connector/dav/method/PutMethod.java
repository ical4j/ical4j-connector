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
package net.fortuna.ical4j.connector.dav.method;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.validate.ValidationException;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardOutputter;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * $Id$
 *
 * Created on 19/11/2008
 *
 * @author Ben
 *
 */
public class PutMethod extends org.apache.http.client.methods.HttpPut {

    private final CalendarOutputter calendarOutputter;
    private final VCardOutputter vCardOutputter;
    
    /**
     * @param uri a calendar URI
     */
    public PutMethod(String uri) {
        super(uri);
        this.calendarOutputter = new CalendarOutputter();
        this.vCardOutputter = new VCardOutputter();
    }

    /**
     * @param calendar a calendar object instance
     * @throws IOException where communication fails
     * @throws ValidationException where the specified calendar is not valid
     */
    public void setCalendar(Calendar calendar) throws IOException, ValidationException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        calendarOutputter.output(calendar, bytes);
        setEntity(new ByteArrayEntity(bytes.toByteArray(), ContentType.create("text/calendar")));
    }
    
    public void setVCard(VCard card) throws IOException, ValidationException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        vCardOutputter.output(card, bytes);
        setEntity(new ByteArrayEntity(bytes.toByteArray(), ContentType.create("text/vcard")));
    }
}

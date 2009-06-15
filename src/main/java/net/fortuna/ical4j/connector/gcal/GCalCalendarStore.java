/**
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
package net.fortuna.ical4j.connector.gcal;

import net.fortuna.ical4j.connector.CalendarCollection;
import net.fortuna.ical4j.connector.CalendarStore;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import com.google.gdata.client.calendar.CalendarService;
//import com.google.gdata.util.AuthenticationException;

/**
 * $Id$
 *
 * Created on 14/03/2008
 *
 * @author Ben
 *
 */
public class GCalCalendarStore implements CalendarStore {

    private Log log = LogFactory.getLog(GCalCalendarStore.class);

//    private CalendarService service;
    
    /**
     * Default constructor.
     */
    public GCalCalendarStore() {
//        service = new CalendarService("ical4j-connector");
    }
    
    /**
     * {@inheritDoc}
     */
    public CalendarCollection addCollection(String id)
            throws ObjectStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public CalendarCollection addCollection(String id, String displayName,
            String description, String[] supportedComponents, Calendar timezone)
            throws ObjectStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean connect() throws ObjectStoreException {
        // Unauthenticated connections not supported..
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean connect(String username, char[] password)
            throws ObjectStoreException {
//        try {
//            service.setUserCredentials(username, new String(password));
//            return true;
//        }
//        catch (AuthenticationException ae) {
//            log.warn("Error authenticating user", ae);
//        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void disconnect() throws ObjectStoreException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    public CalendarCollection getCollection(String id)
            throws ObjectStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public CalendarCollection removeCollection(String id)
            throws ObjectStoreException {
        // TODO Auto-generated method stub
        return null;
    }

}

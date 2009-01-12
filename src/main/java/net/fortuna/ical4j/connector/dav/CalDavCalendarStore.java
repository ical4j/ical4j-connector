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
package net.fortuna.ical4j.connector.dav;

import java.io.IOException;

import net.fortuna.ical4j.connector.CalendarCollection;
import net.fortuna.ical4j.connector.CalendarStore;
import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * $Id$
 *
 * Created on 24/02/2008
 *
 * @author Ben
 *
 */
public class CalDavCalendarStore extends AbstractDavObjectStore<CalendarCollection> implements CalendarStore {

    private String prodId;
    
    /**
     * @param host
     * @param port
     */
    public CalDavCalendarStore(String prodId, String host, int port, Protocol protocol, String path) {
    	super(host, port, protocol, path);
        this.prodId = prodId;
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#add(java.lang.String)
     */
    public CalendarCollection addCollection(String id) throws ObjectStoreException {
        CalDavCalendarCollection collection = new CalDavCalendarCollection(this, id);
        try {
            collection.create();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return collection;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#addCollection(java.lang.String, java.lang.String, java.lang.String, java.lang.String[], net.fortuna.ical4j.model.Calendar)
     */
    public CalendarCollection addCollection(String id, String displayName,
            String description, String[] supportedComponents, Calendar timezone)
            throws ObjectStoreException {
        
        CalDavCalendarCollection collection = new CalDavCalendarCollection(this, id, displayName, description);
        try {
            collection.create();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return collection;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#get(java.lang.String)
     */
    public CalendarCollection getCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        CalDavCalendarCollection collection = new CalDavCalendarCollection(this, id);
        try {
            if (collection.exists()) {
                return collection;
            }
        }
        catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        throw new ObjectNotFoundException("Collection with id: [" + id + "] not found");
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#merge(java.lang.String, net.fortuna.ical4j.connector.CalendarCollection)
     */
    public CalendarCollection merge(String id, CalendarCollection calendar) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#remove(java.lang.String)
     */
    public CalendarCollection removeCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        CalDavCalendarCollection collection = (CalDavCalendarCollection) getCollection(id);
        try {
            collection.delete();
        }
        catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return collection;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#replace(java.lang.String, net.fortuna.ical4j.connector.CalendarCollection)
     */
    public CalendarCollection replace(String id, CalendarCollection calendar) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return the prodId
     */
    final String getProdId() {
        return prodId;
    }

}

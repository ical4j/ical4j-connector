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
package net.fortuna.ical4j.connector;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ConstraintViolationException;

/**
 * @author Ben
 *
 */
public interface CalendarCollection extends ObjectCollection {
    
    /**
     * Specified calendar must be an iCalendar object with exactly one VTIMEZONE component.
     * @param timezone
     */
//    void setTimeZone(Calendar timezone);
    
    /**
     * @return an iCalendar object with exactly one VTIMEZONE component.
     */
    Calendar getTimeZone();
    
    /**
     * @return an array of component names indicating the type of components supported
     * by the collection.
     */
    String[] getSupportedComponentTypes();
    
    /**
     * @return the media type supported for the calendar object resources contained
     *  in a given calendar collection (e.g., iCalendar version 2.0).
     */
    MediaType[] getSupportedMediaTypes();
    
    /**
     * @return a numeric value indicating the maximum size of a resource in octets
     * that the server is willing to accept when a calendar object resource is
     * stored in a calendar collection.
     */
    long getMaxResourceSize();
    
    /**
     * @return a DATE-TIME value indicating the earliest date and time (in UTC)
     * that the server is willing to accept for any DATE or DATE-TIME value in
     * a calendar object resource stored in a calendar collection.
     */
    String getMinDateTime();
    
    /**
     * @return a DATE-TIME value indicating the latest date and time (in UTC)
     * that the server is willing to accept for any DATE or DATE-TIME value in
     * a calendar object resource stored in a calendar collection.
     */
    String getMaxDateTime();
    
    /**
     * @return a numeric value indicating the maximum number of recurrence instances
     * that a calendar object resource stored in a calendar collection can generate.
     */
    Integer getMaxInstances();
    
    /**
     * @return a numeric value indicating the maximum number of ATTENDEE properties
     * in any instance of a calendar object resource stored in a calendar collection.
     */
    Integer getMaxAttendeesPerInstance();
    
    /**
     * Stores the specified calendar in this collection.
     * @param calendar
     * @throws ObjectStoreException when an unexpected error occurs (implementation-specific)
     * @throws ConstraintViolationException if the specified calendar has no single unique identifier (UID)
     */
    void addCalendar(Calendar calendar) throws ObjectStoreException, ConstraintViolationException;
    
    /**
     * @return
     */
    Calendar getCalendar(String uid);
    
    /**
     * @param uid
     * @return
     */
    Calendar removeCalendar(String uid) throws ObjectStoreException;
    
    /**
     * Merges the specified calendar object with this collecton. This is done by
     * decomposing the calendar object into a set of objects per unique identifier (UID)
     * and adding these objects to the collection.
     * @param calendar
     * @throws ObjectStoreException
     */
    void merge(Calendar calendar) throws ObjectStoreException;
    
    /**
     * Exports the entire collection as a single calendar object.
     * @return
     * @throws ObjectStoreException
     */
    Calendar export() throws ObjectStoreException;
}

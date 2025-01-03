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
package org.ical4j.connector;


import net.fortuna.ical4j.model.Calendar;

/**
 * Design contract for calendar collection stores.
 * 
 * @param <C> a calendar collection implementation supported by the calendar store
 * 
 * $Id$
 *
 * Created on 20/02/2008
 *
 * @author Ben
 *
 */
@Deprecated
public interface CalendarStore<C extends CalendarCollection> extends ObjectStore<Calendar, C> {
    
    /**
     * Merges the specified calendar with an existing calendar in the store with the
     * specified id. If a calendar with the specified id does not exist this method is
     * functionally equivalent to the {@link CalendarStore#add(String, Calendar)} operation.  
     * @param id
     * @param calendar
     * @return the calendar resulting from the merge is returned. 
     */
//    CalendarCollection merge(String id, CalendarCollection calendar);
    
    /**
     * Replace an existing calendar in the store with the specified id with the specified
     * calendar.
     * @param id
     * @param calendar
     * @return if a calendar exists with the specified id, it is returned. Otherwise returns null.
     */
//    CalendarCollection replace(String id, CalendarCollection calendar);
}

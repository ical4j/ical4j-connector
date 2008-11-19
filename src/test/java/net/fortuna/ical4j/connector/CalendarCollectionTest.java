/*
 * $Id$
 *
 * Created on 27/02/2008
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

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;

/**
 * @author Ben
 *
 */
public class CalendarCollectionTest extends TestCase {

    private CalendarStoreLifecycle lifecycle;
    
    private CalendarStore store;
    
    private String username;
    
    private char[] password;
    
    private CalendarCollection collection;
    
    private String collectionId = "myCalendars";
    
    private String description = "My collection of calendars";
    
    private String displayName = "My Calendars";
    
    private String[] supportedComponents = {Component.VEVENT};
    
    private String[] calendarUids;
    
    /**
     * @param store
     * @param username
     * @param password
     */
    public CalendarCollectionTest(String testMethod, CalendarStoreLifecycle lifecycle, String username, char[] password) {
        super(testMethod);
        this.lifecycle = lifecycle;
        this.username = username;
        this.password = password;
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        lifecycle.startup();
        store = lifecycle.getCalendarStore();
        store.connect(username, password);
        
        // ensure collection doesn't exist prior to tests..
        try {
            store.removeCollection("myCalendars");
        }
        catch (Exception e) {
        }
        
        collection = store.getCollection(collectionId);
        if (collection == null) {
            collection = store.addCollection(collectionId, displayName, description, supportedComponents, null);
//            collection.setDescription(description);
//            collection.setDisplayName(displayName);
            
            Calendar testCal = Calendars.load("etc/samples/valid/Australian32Holidays.ics");
            collection.merge(testCal);
            
            Set uidList = new HashSet();
            Calendar[] uidCals = Calendars.split(testCal);
            for (int i = 0; i < uidCals.length; i++) {
                Uid uid = Calendars.getUid(uidCals[i]);
                if (uid != null) {
                    uidList.add(uid.getValue());
                }
            }
            calendarUids = (String[]) uidList.toArray(new String[uidList.size()]);
        }
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
//        store.removeCollection(collectionId);
        store.disconnect();
        super.tearDown();
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarCollection#getDescription()}.
     */
    public void testGetDescription() {
        assertEquals(description, collection.getDescription());
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarCollection#getDisplayName()}.
     */
    public void testGetDisplayName() {
        assertEquals(displayName, collection.getDisplayName());
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarCollection#getMaxAttendeesPerInstance()}.
     */
    public void testGetMaxAttendeesPerInstance() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarCollection#getMaxDateTime()}.
     */
    public void testGetMaxDateTime() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarCollection#getMaxInstances()}.
     */
    public void testGetMaxInstances() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarCollection#getMaxResourceSize()}.
     */
    public void testGetMaxResourceSize() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarCollection#getMinDateTime()}.
     */
    public void testGetMinDateTime() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarCollection#getTimeZone()}.
     */
    public void testGetTimeZone() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarCollection#getCalendar(String)}.
     */
    public void testGetCalendar() {
        for (int i = 0; i < calendarUids.length; i++) {
            Calendar cal = collection.getCalendar(calendarUids[i]);
            assertNotNull("Calendar for uid: [" + calendarUids[i] + "] not found", cal);
        }
    }
}

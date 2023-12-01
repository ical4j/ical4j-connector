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
package org.ical4j.connector.dav;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;
import org.junit.Ignore;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * $Id$
 * 
 * Created on 27/02/2008
 * 
 * @author Ben
 * 
 */
@Ignore
public class CalendarCollectionTest<C extends CalendarCollection> extends ObjectCollectionTest<Calendar, C> {

    private static final String[] SUPPORTED_COMPONENTS = { Component.VAVAILABILITY, Component.VJOURNAL,
            Component.VEVENT, Component.VFREEBUSY, Component.VTODO };

    private String[] calendarUids;

    /**
     * @param store
     * @param username
     * @param password
     */
    public CalendarCollectionTest(String testMethod, ObjectStoreLifecycle<Calendar, C> lifecycle,
            String username, char[] password) {
        super(testMethod, lifecycle, username, password, SUPPORTED_COMPONENTS);
    }

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        // ensure collection doesn't exist prior to tests..
        try {
            removeCollection();
        } catch (Exception e) {
            if (!(e instanceof ObjectNotFoundException)) {
                throw e;
            }
        }

        Set<String> uidList = new HashSet<String>();

        File[] samples = new File("etc/samples/calendars/").listFiles((FilenameFilter) new NotFileFilter(
                DirectoryFileFilter.INSTANCE));
        for (File sample : samples) {
            Calendar testCal = Calendars.load(sample.getAbsolutePath());
            getCollection().merge(testCal);

            Calendar[] uidCals = Calendars.split(testCal);
            for (int i = 0; i < uidCals.length; i++) {
                Uid uid = Calendars.getUid(uidCals[i]);
                if (uid != null) {
                    uidList.add(uid.getValue());
                }
            }
        }

        calendarUids = (String[]) uidList.toArray(new String[uidList.size()]);

        // reconnect..
        reconnect();
    }

    /**
     * Test method for
     * {@link org.ical4j.connector.jcr.RepositoryCalendarCollection#getMaxAttendeesPerInstance()}.
     * 
     * @throws ObjectStoreException
     */
    public void testGetMaxAttendeesPerInstance() throws ObjectStoreException {
        // fail("Not yet implemented");
        assertEquals(Integer.valueOf(0), getCollection().getMaxAttendeesPerInstance());
    }

    /**
     * Test method for {@link org.ical4j.connector.jcr.RepositoryCalendarCollection#getMaxDateTime()}.
     * 
     * @throws ObjectStoreException
     */
    public void testGetMaxDateTime() throws ObjectStoreException {
        // fail("Not yet implemented");
        assertEquals(0, getCollection().getMaxDateTime());
    }

    /**
     * Test method for {@link org.ical4j.connector.jcr.RepositoryCalendarCollection#getMaxInstances()}.
     * 
     * @throws ObjectStoreException
     */
    public void testGetMaxInstances() throws ObjectStoreException {
        // fail("Not yet implemented");
        assertEquals(Integer.valueOf(0), getCollection().getMaxInstances());
    }

    /**
     * Test method for {@link org.ical4j.connector.jcr.RepositoryCalendarCollection#getMaxResourceSize()}.
     * 
     * @throws ObjectStoreException
     */
    public void testGetMaxResourceSize() throws ObjectStoreException {
        // fail("Not yet implemented");
        assertEquals(10485760, getCollection().getMaxResourceSize());
    }

    /**
     * Test method for {@link org.ical4j.connector.jcr.RepositoryCalendarCollection#getMinDateTime()}.
     * 
     * @throws ObjectStoreException
     */
    public void testGetMinDateTime() throws ObjectStoreException {
        // fail("Not yet implemented");
        assertEquals(0, getCollection().getMinDateTime());
    }

    /**
     * Test method for {@link org.ical4j.connector.jcr.RepositoryCalendarCollection#getTimeZone()}.
     */
    public void testGetTimeZone() {
        // fail("Not yet implemented");
    }

    /**
     * @throws ObjectStoreException
     * 
     */
    public void testGetSupportedComponentTypes() throws ObjectStoreException {
        assertTrue(Arrays.equals(SUPPORTED_COMPONENTS, getCollection().getSupportedComponentTypes()));
    }

    /**
     * Test method for {@link org.ical4j.connector.jcr.RepositoryCalendarCollection#getCalendar(String)}.
     * 
     * @throws ObjectStoreException
     */
    public void testGetCalendar() throws ObjectStoreException, ObjectNotFoundException {
        for (int i = 0; i < calendarUids.length; i++) {
            Calendar cal = getCollection().getCalendar(calendarUids[i]);
            assertNotNull("Calendar for uid: [" + calendarUids[i] + "] not found", cal);
        }
    }

    /**
     * @throws ObjectStoreException
     */
    public void testGetCalendars() throws ObjectStoreException {
        Iterable<Calendar> calendars = getCollection().getAll();
        assertNotNull(calendars);
    }
}

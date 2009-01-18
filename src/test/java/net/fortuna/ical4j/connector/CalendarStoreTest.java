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
package net.fortuna.ical4j.connector;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * $Id$
 *
 * Created on 24/02/2008
 *
 * @author Ben
 *
 */
public class CalendarStoreTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(CalendarStoreTest.class);
    
    private CalendarStoreLifecycle lifecycle;
    
    private CalendarStore<CalendarCollection> store;
    
    private String username;
    
    private char[] password;
    
    /**
     * @param store
     * @param username
     * @param password
     */
    public CalendarStoreTest(String testMethod, CalendarStoreLifecycle lifecycle, String username, char[] password) {
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
        
        LOG.info("Store connected");
        
        // ensure collection doesn't exist prior to tests..
        try {
            store.removeCollection("myCalendars");
        }
        catch (Exception e) {
        }
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        store.disconnect();
        lifecycle.shutdown();
        super.tearDown();
    }
    
    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarStore#addCollection(java.lang.String)}.
     */
    public void testAddCollection() throws ObjectStoreException {
        CalendarCollection cc = store.addCollection("myCalendars");
        assertNotNull(cc);
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarStore#getCollection(java.lang.String)}.
     * @throws ObjectNotFoundException 
     */
    public void testGetCollection() throws ObjectStoreException, ObjectNotFoundException {
        CalendarCollection cc = null;
        try {
            store.getCollection("myCalendars");
            fail("Should throw " + ObjectNotFoundException.class.getSimpleName());
        }
        catch (ObjectNotFoundException onfe) {
            LOG.debug("Caught exception: " + onfe.getMessage());
        }
        
        store.addCollection("myCalendars");
        cc = store.getCollection("myCalendars");
        assertNotNull(cc);
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarStore#removeCollection(java.lang.String)}.
     * @throws ObjectNotFoundException 
     */
    public void testRemoveCollection() throws ObjectStoreException, ObjectNotFoundException {
        store.addCollection("myCalendars");
        CalendarCollection cc = store.getCollection("myCalendars");
        assertNotNull(cc);
        cc = store.removeCollection("myCalendars");
        assertNotNull(cc);
        
        try {
            store.getCollection("myCalendars");
            fail("Should throw " + ObjectNotFoundException.class.getSimpleName());
        }
        catch (ObjectNotFoundException onfe) {
            LOG.debug("Caught exception: " + onfe.getMessage());
        }
    }

}

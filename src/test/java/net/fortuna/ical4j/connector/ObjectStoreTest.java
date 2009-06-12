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

package net.fortuna.ical4j.connector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * 
 * 
 * @author Ben
 * 
 *         Created on: 24/02/2009
 * 
 *         $Id$
 */
public class ObjectStoreTest<T extends ObjectCollection> extends TestCase {

    private static final Log LOG = LogFactory.getLog(ObjectStoreTest.class);

    private ObjectStoreLifecycle<T> lifecycle;

    private ObjectStore<T> store;

    private String username;

    private char[] password;

    private String collectionName = "myCollection";

    /**
     * @param testMethod
     * @param lifecycle
     * @param username
     * @param password
     */
    public ObjectStoreTest(String testMethod, ObjectStoreLifecycle<T> lifecycle, String username, char[] password) {
        super(testMethod);
        this.lifecycle = lifecycle;
        this.username = username;
        this.password = password;
    }

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        lifecycle.startup();
        store = lifecycle.getObjectStore();
        store.connect(username, password);

        LOG.info("Store connected");

        // ensure collection doesn't exist prior to tests..
        try {
            store.removeCollection(collectionName);
        } catch (Exception e) {
        }
    }

    /*
     * (non-Javadoc)
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
        T collection = store.addCollection(collectionName);
        assertNotNull(collection);
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarStore#getCollection(java.lang.String)}.
     * 
     * @throws ObjectNotFoundException
     */
    public void testGetCollection() throws ObjectStoreException, ObjectNotFoundException {
        T collection = null;
        try {
            store.getCollection(collectionName);
            fail("Should throw " + ObjectNotFoundException.class.getSimpleName());
        } catch (ObjectNotFoundException onfe) {
            LOG.debug("Caught exception: " + onfe.getMessage());
        }

        store.addCollection(collectionName);
        collection = store.getCollection(collectionName);
        assertNotNull(collection);
    }

    /**
     * Test method for
     * {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarStore#removeCollection(java.lang.String)}.
     * 
     * @throws ObjectNotFoundException
     */
    public void testRemoveCollection() throws ObjectStoreException, ObjectNotFoundException {
        store.addCollection(collectionName);
        T collection = store.getCollection(collectionName);
        assertNotNull(collection);
        collection = store.removeCollection(collectionName);
        assertNotNull(collection);

        try {
            store.getCollection(collectionName);
            fail("Should throw " + ObjectNotFoundException.class.getSimpleName());
        } catch (ObjectNotFoundException onfe) {
            LOG.debug("Caught exception: " + onfe.getMessage());
        }
    }

}

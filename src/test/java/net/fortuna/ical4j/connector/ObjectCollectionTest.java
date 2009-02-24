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

import junit.framework.TestCase;

/**
 * 
 *
 * @author Ben
 *
 * Created on: 24/02/2009
 *
 * $Id$
 */
public class ObjectCollectionTest<T extends ObjectCollection> extends TestCase {

    private ObjectStoreLifecycle<T> lifecycle;
    
    private ObjectStore<T> store;
    
    private String username;
    
    private char[] password;
    
    private T collection;
    
    private String collectionId = "myCollection";
    
    private String description = "My collection of objects";
    
    private String displayName = "My Collection";
    
    private String[] supportedComponents;
    
    /**
     * @param testMethod
     * @param lifecycle
     * @param username
     * @param password
     */
    public ObjectCollectionTest(String testMethod, ObjectStoreLifecycle<T> lifecycle,
            String username, char[] password, String[] supportedComponents) {
        super(testMethod);
        this.lifecycle = lifecycle;
        this.username = username;
        this.password = password;
        this.supportedComponents = supportedComponents;
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        lifecycle.startup();
        store = lifecycle.getObjectStore();
        store.connect(username, password);
        
        try {
            collection = getStore().getCollection(collectionId);
        }
        catch (ObjectNotFoundException onfe) {
            collection = getStore().addCollection(collectionId, displayName, description, supportedComponents, null);
//          collection.setDescription(description);
//          collection.setDisplayName(displayName);
        }
    }

    /**
     * @throws ObjectNotFoundException 
     * @throws ObjectStoreException 
     * 
     */
    protected void removeCollection() throws ObjectStoreException, ObjectNotFoundException {
        getStore().removeCollection(collectionId);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
//      store.removeCollection(collectionId);
        store.disconnect();
        super.tearDown();
    }

    /**
     * @throws ObjectStoreException
     * @throws ObjectNotFoundException
     */
    protected void reconnect() throws ObjectStoreException, ObjectNotFoundException {
        store.disconnect();
        store = lifecycle.getObjectStore();
        store.connect(username, password);
        
        collection = store.getCollection(collectionId);
    }
    /**
     * @return the store
     */
    protected final ObjectStore<T> getStore() {
        return store;
    }

    /**
     * @return the collection
     */
    protected final T getCollection() {
        return collection;
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarCollection#getDescription()}.
     */
    public void testGetDescription() {
        assertEquals(description, getCollection().getDescription());
    }

    /**
     * Test method for {@link net.fortuna.ical4j.connector.jcr.RepositoryCalendarCollection#getDisplayName()}.
     */
    public void testGetDisplayName() {
        assertEquals(displayName, getCollection().getDisplayName());
    }
}

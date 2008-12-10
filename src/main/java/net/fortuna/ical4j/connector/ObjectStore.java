/*
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

/**
 * $Id$
 *
 * Created on 27/09/2008
 *
 * @author Ben
 *
 */
public interface ObjectStore<T extends ObjectCollection> {

    /**
     * Connect to a object store anonymously.
     * @return
     * @throws ObjectStoreException
     */
    boolean connect() throws ObjectStoreException;
    
    /**
     * Connect to a object store using the specified credentials.
     * @param username
     * @param password
     * @return
     * @throws ObjectStoreException
     */
    boolean connect(String username, char[] password) throws ObjectStoreException;
    
    /**
     * @throws ObjectStoreException
     */
    void disconnect() throws ObjectStoreException;
    
    /**
     * Adds the specified collection to the store.
     * @param id
     * @throws ObjectStoreException if a calendar with the specified id already
     * exists in the store
     */
    T addCollection(String id) throws ObjectStoreException;
    
    /**
     * @param id
     * @param displayName
     * @param description
     * @param supportedComponents
     * @param timezone
     * @return
     * @throws ObjectStoreException
     */
    T addCollection(String id, String displayName, String description,
            String[] supportedComponents, Calendar timezone) throws ObjectStoreException;
    
    /**
     * Removes the collection with specified id from the store.
     * @param id
     * @return if a collection with the specified id exists in the store it is
     * returned. Otherwise returns null.
     * @throws ObjectNotFoundException 
     */
    T removeCollection(String id) throws ObjectStoreException, ObjectNotFoundException;
    
    /**
     * @param id
     * @return an object collection with the specified id. If no collection with the specified id
     * is found in this store, an {@link ObjectNotFoundException} is thrown.
     */
    T getCollection(String id) throws ObjectStoreException, ObjectNotFoundException;

}

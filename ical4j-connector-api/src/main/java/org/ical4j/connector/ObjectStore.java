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

import java.util.List;

/**
 * Implementors provide support for management of persistent object collections. Typically, this will include
 * most CRUD operations as well as caller authentication.
 *
 * @param <C> the type of collection supported by the store
 * 
 * $Id$
 *
 * Created on 27/09/2008
 *
 * @author Ben
 *
 */
public interface ObjectStore<T, C extends ObjectCollection<T>> extends ObjectStoreListenerSupport<T> {

    String DEFAULT_WORKSPACE = "default";

    /**
     * Connect to a object store anonymously.
     * @return true if connection is successful, otherwise false
     * @throws ObjectStoreException where an unexpected error occurs
     */
    boolean connect() throws ObjectStoreException;
    
    /**
     * Connect to a object store using the specified credentials.
     * @param username connection username
     * @param password connection password
     * @return true if connection is successful, otherwise false
     * @throws ObjectStoreException where an unexpected error occurs
     */
    boolean connect(String username, char[] password) throws ObjectStoreException;
    
    /**
     * @throws ObjectStoreException where an unexpected error occurs
     */
    void disconnect() throws ObjectStoreException;
    
    boolean isConnected();
    
    /**
     * Adds the specified collection to the store.
     * @param id a collection identifier
     * @return the new collection instance
     * @throws ObjectStoreException if a calendar with the specified id already
     * exists in the store
     */
    C addCollection(String id) throws ObjectStoreException;

    C addCollection(String id, String workspace) throws ObjectStoreException;

    /**
     * @param id a collection identifier
     * @param displayName the collection name
     * @param description the collection description
     * @param supportedComponents supported collection objects
     * @param timezone collection timezone
     * @return the new collection instance
     * @throws ObjectStoreException where an unexpected error occurs
     */
    C addCollection(String id, String displayName, String description,
            String[] supportedComponents, Calendar timezone) throws ObjectStoreException;

    C addCollection(String id, String displayName, String description,
            String[] supportedComponents, Calendar timezone, String workspace) throws ObjectStoreException;

    /**
     * Removes the collection with specified id from the store.
     * @param id a collection identifier
     * @return if a collection with the specified id exists in the store it is
     * returned. Otherwise returns null.
     * @throws ObjectStoreException where an unexpected error occurs
     * @throws ObjectNotFoundException if a collection with the specified identifier doesn't exist
     * @deprecated use {@link ObjectCollection#delete()} instead
     */
    @Deprecated
    C removeCollection(String id) throws ObjectStoreException, ObjectNotFoundException;

    /**
     * @param id a collection identifier
     * @return an object collection with the specified id. If no collection with the specified id
     * is found in this store, an {@link ObjectNotFoundException} is thrown.
     * @throws ObjectStoreException where an unexpected error occurs
     * @throws ObjectNotFoundException if a collection with the specified identifier doesn't exist
     */
    C getCollection(String id) throws ObjectStoreException, ObjectNotFoundException;

    C getCollection(String id, String workspace) throws ObjectStoreException, ObjectNotFoundException;

    List<C> getCollections() throws ObjectStoreException, ObjectNotFoundException;

    List<C> getCollections(String workspace) throws ObjectStoreException, ObjectNotFoundException;

    List<String> listWorkspaceIds();
}

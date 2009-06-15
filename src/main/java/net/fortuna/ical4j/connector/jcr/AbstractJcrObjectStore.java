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

package net.fortuna.ical4j.connector.jcr;

import java.util.List;

import javax.jcr.LoginException;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStore;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.model.Calendar;

import org.jcrom.Jcrom;

/**
 * @param <T> the supported collection type
 *
 * @author Ben
 *
 * Created on: 23/01/2009
 *
 * $Id$
 */
public abstract class AbstractJcrObjectStore<T extends AbstractJcrObjectCollection> implements ObjectStore<T> {

    private Repository repository;

    private String path;
    
    private Session session;

    private Jcrom jcrom;

    /**
     * @param repository a repository instance
     * @param path a repository store path
     * @param jcrom a JCROM instance
     */
    public AbstractJcrObjectStore(Repository repository, String path, Jcrom jcrom) {
        this.repository = repository;
        this.path = path;
        this.jcrom = jcrom;
    }
    
    /**
     * {@inheritDoc}
     */
    public final boolean connect() throws ObjectStoreException {
        if (repository == null) {
            throw new ObjectStoreException("Repository not configured");
        }

        try {
            session = repository.login();
        }
        catch (LoginException le) {
            throw new ObjectStoreException("Unable to login", le);
        }
        catch (RepositoryException re) {
            throw new ObjectStoreException("Error connecting", re);
        }
        return session != null;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean connect(String username, char[] password) throws ObjectStoreException {
        if (repository == null) {
            throw new ObjectStoreException("Repository not configured");
        }

        try {
            session = repository.login(new SimpleCredentials(username, password));
        }
        catch (LoginException le) {
            throw new ObjectStoreException("Unable to login", le);
        }
        catch (RepositoryException re) {
            throw new ObjectStoreException("Error connecting", re);
        }
        return session != null;
    }

    /**
     * {@inheritDoc}
     */
    public final void disconnect() throws ObjectStoreException {
        assertConnected();
        session.logout();
    }
    
    /**
     * {@inheritDoc}
     */
    public final T addCollection(String name) throws ObjectStoreException {
        assertConnected();
        
        // initialise store..
        try {
            try {
                session.getRootNode().getNode(path).getNode("collections");
            }
            catch (PathNotFoundException e) {
                session.getRootNode().addNode(path).addNode("collections");
            }
        }
        catch (RepositoryException e) {
            throw new ObjectStoreException("Unexpected error", e);
        }
        
        T collection = null;
        boolean update = false;
        List<T> collections = getCollectionDao().findByCollectionName("/" + path + "/collections", name);
        if (!collections.isEmpty()) {
            collection = collections.get(0);
            update = true;
            
            // if collection exists throw exception..
            throw new ObjectStoreException("Collection already exists: " + name);
        }
        else {
            collection = newCollection();
        }
        collection.setStore(this);
        collection.setName(name);
        collection.setCollectionName(name);

        if (update) {
            getCollectionDao().update(collection);
        }
        else {
            getCollectionDao().create("/" + path + "/collections", collection);
        }
        return collection;
    }

    /**
     * {@inheritDoc}
     */
    public final T addCollection(String name, String displayName,
            String description, String[] supportedComponents, Calendar timezone) throws ObjectStoreException {
        
        T collection = addCollection(name);
        collection.setDisplayName(displayName);
        collection.setDescription(description);
        getCollectionDao().update(collection);
        return collection;
    }

    /**
     * {@inheritDoc}
     */
    public final T getCollection(String name) throws ObjectStoreException, ObjectNotFoundException {
        List<T> collections = getCollectionDao().findByCollectionName("/" + path + "/collections", name);
        if (!collections.isEmpty()) {
            T collection = collections.get(0);
            collection.setStore(this);
            return collection;
        }
        throw new ObjectNotFoundException("Collection doesn't exist: " + name);
    }

    /**
     * {@inheritDoc}
     */
    public final T removeCollection(String name) throws ObjectStoreException, ObjectNotFoundException {
        T collection = getCollection(name);
        getCollectionDao().remove(getJcrom().getPath(collection));
        return collection;
    }

    /**
     * @return the session
     */
    final Session getSession() {
        return session;
    }

    /**
     * @return the jcrom
     */
    final Jcrom getJcrom() {
        return jcrom;
    }

    /**
     * @throws ObjectStoreException where the store is not connected
     */
    protected final void assertConnected() throws ObjectStoreException {
        if (session == null) {
            throw new ObjectStoreException("Not connected");
        }
    }
    
    /**
     * @return a new collection instance
     */
    protected abstract T newCollection();
    
    /**
     * @return the underlying collection DAO
     */
    protected abstract AbstractJcrObjectCollectionDao<T> getCollectionDao();
}

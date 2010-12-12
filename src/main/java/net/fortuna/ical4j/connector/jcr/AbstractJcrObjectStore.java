/**
 * Copyright (c) 2010, Ben Fortuna
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
import javax.jcr.Node;
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
 * @param <C> the supported collection type
 *
 * @author Ben
 *
 * Created on: 23/01/2009
 *
 * $Id$
 */
public abstract class AbstractJcrObjectStore<C extends AbstractJcrObjectCollection<?>> implements ObjectStore<C> {

    private final Repository repository;

    private final String path;
    
    private Session session;

    private final Jcrom jcrom;

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
    public final C addCollection(String name) throws ObjectStoreException {
        assertConnected();
        
        // initialise store..
        try {
        	Node pathNode;
        	if (!session.nodeExists(path)) {
        		pathNode = session.getRootNode().addNode(path.substring(1));
        	}
        	else {
        		pathNode = session.getNode(path);
        	}
        	
        	if (!pathNode.hasNode("collections")) {
        		pathNode.addNode("collections");
        	}
        }
        catch (RepositoryException e) {
            throw new ObjectStoreException("Unexpected error", e);
        }
        
        C collection = null;
        boolean update = false;
        List<C> collections = getCollectionDao().findByCollectionName(path + "/collections", name);
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
            getCollectionDao().create(path + "/collections", collection);
        }
        return collection;
    }

    /**
     * {@inheritDoc}
     */
    public final C addCollection(String name, String displayName,
            String description, String[] supportedComponents, Calendar timezone) throws ObjectStoreException {
        
        C collection = addCollection(name);
        collection.setDisplayName(displayName);
        collection.setDescription(description);
        getCollectionDao().update(collection);
        return collection;
    }

    /**
     * {@inheritDoc}
     */
    public final C getCollection(String name) throws ObjectStoreException, ObjectNotFoundException {
        List<C> collections = getCollectionDao().findByCollectionName(path + "/collections", name);
        if (!collections.isEmpty()) {
            C collection = collections.get(0);
            collection.setStore(this);
            return collection;
        }
        throw new ObjectNotFoundException("Collection doesn't exist: " + name);
    }

    /**
     * {@inheritDoc}
     */
    public final C removeCollection(String name) throws ObjectStoreException, ObjectNotFoundException {
        C collection = getCollection(name);
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
    protected abstract C newCollection();
    
    /**
     * @return the underlying collection DAO
     */
    protected abstract AbstractJcrObjectCollectionDao<C> getCollectionDao();
}

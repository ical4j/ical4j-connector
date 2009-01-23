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

import javax.jcr.LoginException;
import javax.jcr.Node;
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
 * 
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
     * @param repoitory
     * @param path
     * @param jcrom
     */
    public AbstractJcrObjectStore(Repository repoitory, String path, Jcrom jcrom) {
        this.repository = repoitory;
        this.path = path;
        this.jcrom = jcrom;
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#connect()
     */
    @Override
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

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#connect(java.lang.String, char[])
     */
    @Override
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

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#disconnect()
     */
    @Override
    public final void disconnect() throws ObjectStoreException {
        assertConnected();
        session.logout();
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#addCollection(java.lang.String)
     */
    @Override
    public final T addCollection(String id) throws ObjectStoreException {
        assertConnected();
        T collection = newCollection();
        collection.setStore(this);
        collection.setName(id);
        try {
            getJcrom().addNode(getNode(), collection);
            getSession().save();
        }
        catch (RepositoryException re) {
            throw new ObjectStoreException("Error creating collection", re);
        }
        return collection;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#addCollection(java.lang.String, java.lang.String, java.lang.String, java.lang.String[], net.fortuna.ical4j.model.Calendar)
     */
    @Override
    public final T addCollection(String id, String displayName,
            String description, String[] supportedComponents, Calendar timezone)
            throws ObjectStoreException {
        T collection = newCollection();
        collection.setStore(this);
        collection.setName(id);
        collection.setDisplayName(displayName);
        collection.setDescription(description);
        try {
            getJcrom().addNode(getNode(), collection);
            getSession().save();
        }
        catch (RepositoryException re) {
            throw new ObjectStoreException("Error creating collection", re);
        }
        return collection;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#getCollection(java.lang.String)
     */
    /*
    @Override
    public T getCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        try {
            T collection = getJcrom().fromNode(Class<T>, getNode().getNode(id));
            collection.setStore(this);
            return collection;
        }
        catch (PathNotFoundException e) {
            throw new ObjectNotFoundException("Collection not found", e);
        }
        catch (RepositoryException e) {
            throw new ObjectNotFoundException("Error retrieving collection", e);
        }
    }
    */

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#removeCollection(java.lang.String)
     */
    @Override
    public final T removeCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        T collection = getCollection(id);
        Node collectionNode;
        try {
            collectionNode = getNode().getNode(id);
            collectionNode.remove();
            getSession().save();
        }
        catch (PathNotFoundException e) {
            throw new ObjectNotFoundException("Collection not found", e);
        }
        catch (RepositoryException e) {
            throw new ObjectNotFoundException("Error retrieving collection", e);
        }
        return collection;
    }
    
    /**
     * @return
     * @throws RepositoryException 
     * @throws PathNotFoundException 
     * @throws ObjectStoreException 
     */
    final Node getNode() throws PathNotFoundException, RepositoryException, ObjectStoreException {
        assertConnected();
        try {
            return session.getRootNode().getNode(path);
        }
        catch (PathNotFoundException pnfe) {
            session.getRootNode().addNode(path);
            session.save();
        }
        return session.getRootNode().getNode(path);
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
     * @throws ObjectStoreException
     */
    protected final void assertConnected() throws ObjectStoreException {
        if (session == null) {
            throw new ObjectStoreException("Not connected");
        }
    }
    
    protected abstract T newCollection();
}

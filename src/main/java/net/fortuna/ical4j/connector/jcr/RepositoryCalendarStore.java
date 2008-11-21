/*
 * $Id$
 *
 * Created on 20/02/2008
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
package net.fortuna.ical4j.connector.jcr;

import java.util.Arrays;
import java.util.Hashtable;

import javax.jcr.AccessDeniedException;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.fortuna.ical4j.connector.CalendarCollection;
import net.fortuna.ical4j.connector.CalendarStore;
import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Ben
 */
public class RepositoryCalendarStore implements CalendarStore {

    public static final String NAMESPACE = "ical4j";

    public static final String NAMESPACE_URL = "http://ical4j.sourceforge.net/ical4j-connector/1.0";

    private Log log = LogFactory.getLog(RepositoryCalendarStore.class);

    private Repository repository;

    private Session session;

    private Workspace workspace;

    private QueryManager queryManager;

    /**
     * @param url
     */
    public RepositoryCalendarStore(String url, String name) throws NamingException {
        Hashtable env = new Hashtable();
        env.put(Context.PROVIDER_URL, url);
        Context context = new InitialContext(env);
        repository = (Repository) context.lookup(name);
    }

    /**
     * @param repository
     */
    public RepositoryCalendarStore(Repository repository) {
        this.repository = repository;
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#connect()
     */
    public boolean connect() throws ObjectStoreException {
        try {
            session = repository.login();
            getWorkspace();
            return session != null;
        }
        catch (LoginException le) {
            throw new ObjectStoreException("Unable to login", le);
        }
        catch (RepositoryException re) {
            throw new ObjectStoreException("Error connecting", re);
        }
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#connect(java.lang.String, char[])
     */
    public boolean connect(String username, char[] password) throws ObjectStoreException {
        if (repository == null) {
            throw new ObjectStoreException("Repository not configured");
        }

        try {
            session = repository.login(new SimpleCredentials(username, password));
            getWorkspace();
            return session != null;
        }
        catch (LoginException le) {
            log.warn("Unable to login", le);
        }
        catch (RepositoryException re) {
            throw new ObjectStoreException("Error connecting", re);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#disconnect()
     */
    public void disconnect() throws ObjectStoreException {
        assertConnected();
        session.logout();
    }

    /**
     * @return
     * @throws ObjectStoreException
     */
    private Workspace getWorkspace() throws ObjectStoreException {
        if (workspace == null) {
            assertConnected();
            workspace = session.getWorkspace();
            try {
                if (!Arrays.asList(
                        workspace.getNamespaceRegistry().getPrefixes())
                        .contains(NAMESPACE)) {
                    workspace.getNamespaceRegistry().registerNamespace(
                            NAMESPACE, NAMESPACE_URL);
                }
                queryManager = workspace.getQueryManager();
            }
            catch (UnsupportedRepositoryOperationException uroe) {
                throw new ObjectStoreException("Unsupported repository", uroe);
            }
            catch (AccessDeniedException ade) {
                throw new ObjectStoreException("Unsupported repository", ade);
            }
            catch (RepositoryException re) {
                throw new ObjectStoreException("Unsupported repository", re);
            }
        }
        return workspace;
    }

    /**
     * @throws ObjectStoreException
     */
    private void assertConnected() throws ObjectStoreException {
        if (session == null) {
            throw new ObjectStoreException("Not connected");
        }
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarStore#add(java.lang.String)
     */
    public CalendarCollection addCollection(String id)
            throws ObjectStoreException {
        try {
            try {
                getCollection(id);
                throw new ObjectStoreException("Collection already exists");
            }
            catch (ObjectNotFoundException onfe) {
                // expected..
            }
            Node collectionNode = session.getRootNode().addNode(
                    NodeType.COLLECTION.getNodeName());
            collectionNode.setProperty(NodeProperty.COLLECTION_ID.getPropertyName(), id);
            session.save();
            return new RepositoryCalendarCollection(collectionNode);
        }
        catch (RepositoryException re) {
            throw new ObjectStoreException("Error creating collection", re);
        }
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#addCollection(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String[], net.fortuna.ical4j.model.Calendar)
     */
    public CalendarCollection addCollection(String id, String displayName,
            String description, String[] supportedComponents, Calendar timezone)
            throws ObjectStoreException {
        
        RepositoryCalendarCollection collection = (RepositoryCalendarCollection) addCollection(id);
        collection.setDisplayName(displayName);
        collection.setDescription(description);
        collection.setTimeZone(timezone);
        return collection;
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarStore#get(java.lang.String)
     */
    public CalendarCollection getCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        try {
            // return new RepositoryCalendarCollection(session.getRootNode().getNode(id));
            // String queryString = session.getRootNode().getPath() + '/' + NodeNames.COLLECTION
            String queryString = "//" + NodeType.COLLECTION.getNodeName() + "[@"
                    + NodeProperty.COLLECTION_ID.getPropertyName() + "='" + id + "']";

            Query folderQuery = queryManager.createQuery(queryString,
                    Query.XPATH);
            NodeIterator nodes = folderQuery.execute().getNodes();
            if (nodes.hasNext()) {
                return new RepositoryCalendarCollection(nodes.nextNode());
            }
        }
        catch (RepositoryException re) {
            log.warn("Error retrieving collection [" + id + "]", re);
        }
        throw new ObjectNotFoundException("Collection with id: [" + id + "] not found");
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.protocol.CalendarStore#remove(java.lang.String)
     */
    public CalendarCollection removeCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        try {
            RepositoryCalendarCollection collection = (RepositoryCalendarCollection) getCollection(id);
            if (collection != null) {
                collection.getNode().remove();
                session.save();
                return collection;
            }
        }
        catch (RepositoryException re) {
            throw new ObjectStoreException("Error removing collection", re);
        }
        return null;
    }
}

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

import net.fortuna.ical4j.util.Configurator;
import org.ical4j.connector.AbstractObjectStore;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStoreException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * @param <C>
 *            the supported collection type
 * 
 *            Created: [20/11/2008]
 * 
 * @author fortuna
 */
public abstract class AbstractDavObjectStore<T, C extends ObjectCollection<T>> extends AbstractObjectStore<T, C> {

    /**
     * Factory used to create new client instances on connect..
     */
    private final DavClientFactory clientFactory;

    /**
     * URL of the target DAV server.
     */
    private final URL rootUrl;

    /**
     * Server implementation-specific path resolution.
     */
    protected final PathResolver pathResolver;

    /**
     * DAV client instance initialised on connect. A new instance is created each time a connect
     * method is invoked.
     */
	private DefaultDavClient davClient;
	
	private DavSessionConfiguration sessionConfiguration;

	private List<SupportedFeature> supportedFeatures;

    /**
     * @param url the URL of a CalDAV server instance
     * @param pathResolver the path resolver for the CalDAV server type
     */
    public AbstractDavObjectStore(URL url, PathResolver pathResolver) {
    	this.rootUrl = url;
        this.pathResolver = pathResolver;
        this.clientFactory = new DavClientFactory()
                .withPreemptiveAuth("true".equals(Configurator.getProperty("ical4j.connector.dav.preemptiveauth")
                        .orElse("false")));
    }

    /**
     * Reconnect with an existing session configuration.
     */
    public final boolean connect() throws ObjectStoreException {
        return connect(sessionConfiguration);
    }

    /**
     *
     * @param bearerAuth
     * @return
     * @throws ObjectStoreException
     *
     * @deprecated use {@link AbstractDavObjectStore#connect(DavSessionConfiguration)} instead.
     */
    @Deprecated
    public final boolean connect(String bearerAuth) throws ObjectStoreException {
        return connect(new DavSessionConfiguration().withBearerAuth(bearerAuth));
    }


    /**
     * {@inheritDoc}
     * @throws FailedOperationException 
     * @throws IOException
     *
     * @deprecated use {@link AbstractDavObjectStore#connect(DavSessionConfiguration)} instead.
     */
    @Deprecated
    public final boolean connect(String username, char[] password) throws ObjectStoreException {
        return connect(new DavSessionConfiguration().withUser(username).withPassword(password));
    }

    public final boolean connect(DavSessionConfiguration sessionConfiguration) throws ObjectStoreException {
        this.sessionConfiguration = sessionConfiguration;
    	try {
        	davClient = clientFactory.newInstance(rootUrl);
            if (sessionConfiguration.getCredentialsProvider() != null) {
                davClient.begin(sessionConfiguration.getCredentialsProvider());
            }
            supportedFeatures = davClient.getSupportedFeatures();
    	} catch (IOException ioe) {
    		throw new ObjectStoreException(ioe);
    	}
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public final void disconnect() {
    	davClient = null;
        supportedFeatures = null;
    }

    /**
     * @return true if connected to the server, otherwise false
     */
    public final boolean isConnected() {
    	return davClient != null;
    }

    /**
     * This method is needed to "propfind" the user's principals.
     * 
     * @return the username stored in the HTTP credentials
     * @author Pascal Robert
     *
     * @deprecated use {@link AbstractDavObjectStore#getSessionConfiguration()} instead.
     */
    @Deprecated
    protected String getUserName() {
    	return sessionConfiguration.getUser();
    }
    
    public DefaultDavClient getClient() {
    	return davClient;
    }

    public DavSessionConfiguration getSessionConfiguration() {
        return sessionConfiguration;
    }

    /**
     * Returns a list of supported features, based on the DAV header in the response 
     * of the connect call.
     * @return
     */
    public List<SupportedFeature> supportedFeatures() {
        return supportedFeatures;
    }
    
    public boolean isSupportCalendarProxy() {
        return supportedFeatures.contains(SupportedFeature.CALENDAR_PROXY);
    }
}

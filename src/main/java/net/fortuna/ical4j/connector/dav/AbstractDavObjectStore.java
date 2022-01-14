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
package net.fortuna.ical4j.connector.dav;

import net.fortuna.ical4j.connector.FailedOperationException;
import net.fortuna.ical4j.connector.ObjectCollection;
import net.fortuna.ical4j.connector.ObjectStore;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.util.Configurator;

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
public abstract class AbstractDavObjectStore<C extends ObjectCollection<?>> implements ObjectStore<C> {

    private final DavClientFactory clientFactory;

	private DefaultDavClient davClient;
	
	private String username;
    private String bearerAuth;
	
	private final URL rootUrl;
	
	private List<SupportedFeature> supportedFeatures;
	
    /**
     * Server implementation-specific path resolution.
     */
    protected final PathResolver pathResolver;

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
     * @return the path
     */
    public final String getPath() {
        return pathResolver == null ? rootUrl.getFile() : pathResolver.getRootPath();
    }

    /**
     * {@inheritDoc}
     */
    public final boolean connect() throws ObjectStoreException {
//        DavClientConfiguration configuration = new DavClientConfiguration().withRepositoryUrl(rootUrl)
//                .withPrincipalPath(pathResolver.getPrincipalPath(getUserName()))
//                .withUserPath(pathResolver.getUserPath(getUserName()));

        davClient = clientFactory.newInstance(rootUrl);
        davClient.begin();

        return true;
    }


    public final boolean connect( String bearerAuth ) throws ObjectStoreException {
        try {
//            DavClientConfiguration configuration = new DavClientConfiguration().withRepositoryUrl(rootUrl)
//                    .withPrincipalPath(rootUrl.getFile())
//                    .withUserPath(rootUrl.getFile());

            davClient = clientFactory.newInstance(rootUrl);
            davClient.begin( bearerAuth );

            this.bearerAuth = bearerAuth;
        } catch (IOException | FailedOperationException ioe) {
            throw new ObjectStoreException( ioe );
        }

        return true;
    }


    /**
     * {@inheritDoc}
     * @throws FailedOperationException 
     * @throws IOException 
     */
    public final boolean connect(String username, char[] password) throws ObjectStoreException {
    	try {
            this.username = username;

//            DavClientConfiguration configuration = new DavClientConfiguration().withRepositoryUrl(rootUrl)
//                    .withPrincipalPath(pathResolver.getPrincipalPath(getUserName()))
//                    .withUserPath(pathResolver.getUserPath(getUserName()));

        	davClient = clientFactory.newInstance(rootUrl);
        	supportedFeatures = davClient.begin(username, password);
    	}
    	catch (IOException | FailedOperationException ioe) {
    		throw new ObjectStoreException(ioe);
    	}

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public final void disconnect() {
    	davClient = null;
    	username = null;
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
     */
    protected String getUserName() {
    	return username;
    }
    
    public DefaultDavClient getClient() {
    	return davClient;
    }
    
    public URL getHostURL() {
    	return rootUrl;
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

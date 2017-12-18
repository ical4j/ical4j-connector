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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import net.fortuna.ical4j.connector.FailedOperationException;
import net.fortuna.ical4j.connector.ObjectCollection;
import net.fortuna.ical4j.connector.ObjectStore;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.enums.SupportedFeature;

/**
 * @param <C>
 *            the supported collection type
 * 
 *            Created: [20/11/2008]
 * 
 * @author fortuna
 */
public abstract class AbstractDavObjectStore<C extends ObjectCollection<?>> implements ObjectStore<C> {

	private DavClient davClient;
	
	private String username;
    private String bearerAuth;
	
	private final URL rootUrl;
	
	private ArrayList<SupportedFeature> supportedFeatures;
	
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
    }

    /**
     * @return the path
     */
    public final String getPath() {
        return pathResolver == null ? rootUrl.getFile() : pathResolver.getUserPath( getUserName() );
    }

    /**
     * {@inheritDoc}
     */
    public final boolean connect() throws ObjectStoreException {
//    	try {
//        	davClient = SardineFactory.begin();
        	
        	final String principalPath = pathResolver.getPrincipalPath(getUserName());
        	final String userPath = pathResolver.getUserPath(getUserName());
        	davClient = new DavClient(rootUrl, principalPath, userPath);
        	davClient.begin();
//    	}
//    	catch (SardineException se) {
//    		throw new ObjectStoreException(se);
//    	}
        return true;
    }


    public final boolean connect( String bearerAuth ) throws ObjectStoreException {
        try {
            davClient = new DavClient( rootUrl, rootUrl.getFile(), rootUrl.getFile() );
            davClient.begin( bearerAuth );

            this.bearerAuth = bearerAuth;
        } catch (IOException ioe) {
            throw new ObjectStoreException( ioe );
        } catch (FailedOperationException foe) {
            throw new ObjectStoreException( foe );
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
//        	davClient = SardineFactory.begin(username, new String(password));
            this.username = username;
        	
        	final String principalPath = pathResolver.getPrincipalPath(username);
        	final String userPath = pathResolver.getUserPath(username);
        	davClient = new DavClient(rootUrl, principalPath, userPath);
        	supportedFeatures = davClient.begin(username, password);


    	}
    	catch (IOException ioe) {
    		throw new ObjectStoreException(ioe);
    	}
    	catch (FailedOperationException foe) {
    		throw new ObjectStoreException(foe);
    	}
//    	catch (SardineException se) {
//    		throw new ObjectStoreException(se);
//    	}

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
    
    public DavClient getClient() {
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
    public ArrayList<SupportedFeature> supportedFeatures() {
        return supportedFeatures;
    }
    
    public boolean isSupportCalendarProxy() {
        return supportedFeatures.contains(SupportedFeature.CALENDAR_PROXY) ? true: false;
    }
    
}

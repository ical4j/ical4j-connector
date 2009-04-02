/**
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
package net.fortuna.ical4j.connector.dav;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.connector.ObjectCollection;
import net.fortuna.ical4j.connector.ObjectStore;
import net.fortuna.ical4j.connector.ObjectStoreException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;

/**
 * Created: [20/11/2008]
 * 
 * @author fortuna
 */
public abstract class AbstractDavObjectStore<T extends ObjectCollection> implements ObjectStore<T> {

    protected HttpClient httpClient;

    protected HostConfiguration hostConfiguration;

    protected PathResolver pathResolver;

    /**
     * @param host
     * @param port
     * @param protocol
     */
    public AbstractDavObjectStore(String host, int port, Protocol protocol, PathResolver pathResolver) {
        hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost(host, port, protocol);
        this.pathResolver = pathResolver;
    }

    /**
     * @return the path
     */
    public final String getPath() {
        return pathResolver.getUserPath(getUserName());
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#connect()
     */
    public final boolean connect() throws ObjectStoreException {
        httpClient = new HttpClient();
        httpClient.getParams().setAuthenticationPreemptive(false);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#connect(java.lang.String, char[])
     */
    public final boolean connect(String username, char[] password) throws ObjectStoreException {
        connect();
        // httpClient = new HttpClient();
        Credentials credentials = new UsernamePasswordCredentials(username, new String(password));
        httpClient.getState().setCredentials(AuthScope.ANY, credentials);
        
        // Added to support iCal Server, who don't support Basic auth at all, only Kerberos and Digest
        List<String> authPrefs = new ArrayList<String>(2);
        authPrefs.add(org.apache.commons.httpclient.auth.AuthPolicy.DIGEST);
        authPrefs.add(org.apache.commons.httpclient.auth.AuthPolicy.BASIC);
        httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY,authPrefs);
        
        // This is to get the Digest from the user
        try {
        	PropFindMethod aGet = new PropFindMethod(pathResolver.getPrincipalPath(username));
        	aGet.setDoAuthentication(true);
        	int status = httpClient.executeMethod(hostConfiguration,aGet);
        	if (status >= 300) {
        		throw new ObjectStoreException("Principals not found");
        	}
        } catch (Exception ex) {
        	throw new ObjectStoreException(ex.getMessage());
        }
        
        // httpClient.getParams().setAuthenticationPreemptive(true);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#disconnect()
     */
    public final void disconnect() throws ObjectStoreException {
        httpClient = null;
    }

    /**
     * @return
     */
    public final boolean isConnected() {
        return httpClient != null;
    }

    /**
     * @param method
     * @throws HttpException
     * @throws IOException
     */
    int execute(HttpMethodBase method) throws HttpException, IOException {
        return httpClient.executeMethod(hostConfiguration, method);
    }    
    
    /**
     * This method is needed to "propfind" the user's principals
     * @return the username stored in the HTTP credentials
     * @author Pascal Robert
     */
    protected String getUserName() {
    	return ((UsernamePasswordCredentials)httpClient.getState().getCredentials(AuthScope.ANY)).getUserName();
    }
}

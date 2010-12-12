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
package net.fortuna.ical4j.connector.dav;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.connector.ObjectCollection;
import net.fortuna.ical4j.connector.ObjectStore;
import net.fortuna.ical4j.connector.ObjectStoreException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;

/**
 * @param <C>
 *            the supported collection type
 * 
 *            Created: [20/11/2008]
 * 
 * @author fortuna
 */
public abstract class AbstractDavObjectStore<C extends ObjectCollection<?>> implements ObjectStore<C> {

    /**
     * The underlying HTTP client.
     */
    protected HttpClient httpClient;

    /**
     * The HTTP client configuration.
     */
    protected HostConfiguration hostConfiguration;

    /**
     * Server implementation-specific path resolution.
     */
    protected PathResolver pathResolver;

    /**
     * @param url the URL of a CalDAV server instance
     * @param pathResolver the path resolver for the CalDAV server type
     */
    public AbstractDavObjectStore(URL url, PathResolver pathResolver) {
        final Protocol protocol = Protocol.getProtocol(url.getProtocol());
        hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost(url.getHost(), url.getPort(), protocol);
        this.pathResolver = pathResolver;
    }

    /**
     * @return the path
     */
    public final String getPath() {
        return pathResolver.getUserPath(getUserName());
    }

    /**
     * {@inheritDoc}
     */
    public final boolean connect() throws ObjectStoreException {
        httpClient = new HttpClient();
        httpClient.getParams().setAuthenticationPreemptive(false);
        return true;
    }

    /**
     * {@inheritDoc}
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
        httpClient.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);

        // This is to get the Digest from the user
        try {
            PropFindMethod aGet = new PropFindMethod(pathResolver.getPrincipalPath(username),
                    DavConstants.PROPFIND_ALL_PROP, 0);
            aGet.setDoAuthentication(true);
            int status = httpClient.executeMethod(hostConfiguration, aGet);
            if (status >= 300) {
                throw new ObjectStoreException("Principals not found");
            }
        } catch (Exception ex) {
            if (ex instanceof ObjectStoreException) {
                throw (ObjectStoreException) ex;
            }
            else {
                throw new ObjectStoreException(ex.getMessage(), ex);
            }
        }

        // httpClient.getParams().setAuthenticationPreemptive(true);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public final void disconnect() throws ObjectStoreException {
        httpClient = null;
    }

    /**
     * @return true if connected to the server, otherwise false
     */
    public final boolean isConnected() {
        return httpClient != null;
    }

    int execute(HttpMethodBase method) throws IOException {
        return httpClient.executeMethod(hostConfiguration, method);
    }

    /**
     * This method is needed to "propfind" the user's principals.
     * 
     * @return the username stored in the HTTP credentials
     * @author Pascal Robert
     */
    protected String getUserName() {
        return ((UsernamePasswordCredentials) httpClient.getState().getCredentials(AuthScope.ANY)).getUserName();
    }
}

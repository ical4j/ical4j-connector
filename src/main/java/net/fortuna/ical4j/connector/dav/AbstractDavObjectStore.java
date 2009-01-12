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

import net.fortuna.ical4j.connector.ObjectCollection;
import net.fortuna.ical4j.connector.ObjectStore;
import net.fortuna.ical4j.connector.ObjectStoreException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * Created: [20/11/2008]
 * 
 * @author fortuna
 */
public abstract class AbstractDavObjectStore<T extends ObjectCollection> implements ObjectStore<T> {

    private HttpClient httpClient;

    private HostConfiguration hostConfiguration;

    private String path;

    /**
     * @param host
     * @param port
     * @param protocol
     */
    public AbstractDavObjectStore(String host, int port, Protocol protocol, String path) {
        hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost(host, port, protocol);
        this.path = path;
    }

    /**
     * @return the path
     */
    public final String getPath() {
        return path;
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#connect()
     */
    @Override
    public final boolean connect() throws ObjectStoreException {
        httpClient = new HttpClient();
        httpClient.getParams().setAuthenticationPreemptive(true);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#connect(java.lang.String, char[])
     */
    @Override
    public final boolean connect(String username, char[] password) throws ObjectStoreException {
        connect();
        // httpClient = new HttpClient();
        Credentials credentials = new UsernamePasswordCredentials(username, new String(password));
        httpClient.getState().setCredentials(AuthScope.ANY, credentials);
        // httpClient.getParams().setAuthenticationPreemptive(true);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStore#disconnect()
     */
    @Override
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
}

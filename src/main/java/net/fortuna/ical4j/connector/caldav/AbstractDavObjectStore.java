/*
 * This file is part of Touchbase.
 *
 * Created: [20/11/2008]
 *
 * Copyright (c) 2008, Ben Fortuna
 *
 * Touchbase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Touchbase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Touchbase.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.fortuna.ical4j.connector.caldav;

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

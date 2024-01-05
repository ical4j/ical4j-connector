package org.ical4j.connector.dav;

import org.apache.http.client.CredentialsProvider;

import java.net.MalformedURLException;
import java.net.URL;

public class DavClientFactory {

    private DavClientConfiguration clientConfiguration;

    private CredentialsProvider credentialsProvider;

    public DavClientFactory() {
        clientConfiguration = new DavClientConfiguration();
    }

    public DavClientFactory withPreemptiveAuth(boolean preemptiveAuth) {
        this.clientConfiguration = clientConfiguration.withPreemptiveAuth(preemptiveAuth);
        return this;
    }

    public DavClientFactory withFollowRedirects(boolean followRedirects) {
        this.clientConfiguration = clientConfiguration.withFollowRedirects(followRedirects);
        return this;
    }

    public DavClientFactory withDefaultHeader(String name, String value) {
        this.clientConfiguration = clientConfiguration.withDefaultHeader(name, value);
        return this;
    }

    public DavClientFactory withCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    /**
     * Create a new client instance. Note that if a path is specified it should include a trailing
     * forward slash.
     * @param url the URL of the DAV repository
     * @return a new client instance
     */
    public DefaultDavClient newInstance(URL url) {
        DefaultDavClient client = new DefaultDavClient(url, clientConfiguration);
        if (credentialsProvider != null) {
            client.begin(credentialsProvider);
        } else {
            client.begin();
        }
        return client;
    }

    /**
     * Create a new client instance. Note that if a path is specified it should include a trailing
     * forward slash.
     * @param url the URL of the DAV repository
     * @return a new client instance
     */
    public DefaultDavClient newInstance(String url) throws MalformedURLException {
        DefaultDavClient client = new DefaultDavClient(url, clientConfiguration);
        if (credentialsProvider != null) {
            client.begin(credentialsProvider);
        } else {
            client.begin();
        }
        return client;
    }
}

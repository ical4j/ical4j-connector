package net.fortuna.ical4j.connector.dav;

import java.net.MalformedURLException;
import java.net.URL;

public class DavClientFactory {

    private DavClientConfiguration clientConfiguration;

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

    /**
     * Create a new client instance. Note that if a path is specified it should include a trailing
     * forward slash.
     * @param url the URL of the DAV repository
     * @return a new client instance
     */
    public DefaultDavClient newInstance(URL url) {
        return new DefaultDavClient(url, clientConfiguration);
    }

    /**
     * Create a new client instance. Note that if a path is specified it should include a trailing
     * forward slash.
     * @param url the URL of the DAV repository
     * @return a new client instance
     */
    public DefaultDavClient newInstance(String url) throws MalformedURLException {
        return new DefaultDavClient(url, clientConfiguration);
    }
}

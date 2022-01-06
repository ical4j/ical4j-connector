package net.fortuna.ical4j.connector.dav;

import org.apache.jackrabbit.webdav.DavLocatorFactory;

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
     *
     * @param url
     * @return
     */
    public DefaultDavClient newInstance(URL url, PathResolver pathResolver) {
        return new DefaultDavClient(url, new CalDavLocatorFactory(pathResolver),
                clientConfiguration);
    }

    public DefaultDavClient newInstance(URL url, DavLocatorFactory locatorFactory) {
        return new DefaultDavClient(url, locatorFactory, clientConfiguration);
    }

    public DefaultDavClient newInstance(String url, DavLocatorFactory locatorFactory) throws MalformedURLException {
        return new DefaultDavClient(url, locatorFactory, clientConfiguration);
    }

//    public DavClient newInstance(DavClientConfiguration configuration) {
//        return new DavClient(configuration, preemptiveAuth);
//    }
}

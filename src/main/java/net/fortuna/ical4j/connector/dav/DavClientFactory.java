package net.fortuna.ical4j.connector.dav;

import java.net.URL;

public class DavClientFactory {

    private final boolean preemptiveAuth;

    public DavClientFactory(boolean preemptiveAuth) {
        this.preemptiveAuth = preemptiveAuth;
    }

    public DavClient newInstance(URL url, String principalPath, String userPath) {
        return new DavClient(url, principalPath, userPath, preemptiveAuth);
    }
}

package org.ical4j.connector.dav;

import java.util.HashMap;
import java.util.Map;

public class DavClientConfiguration {

    private boolean preemptiveAuth;

    private boolean followRedirects;

    private final Map<String, String> defaultHeaders = new HashMap<>();

    public DavClientConfiguration withPreemptiveAuth(boolean preemptiveAuth) {
        this.preemptiveAuth = preemptiveAuth;
        return this;
    }

    public DavClientConfiguration withFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public DavClientConfiguration withDefaultHeader(String name, String value) {
        this.defaultHeaders.put(name, value);
        return this;
    }

    public boolean isPreemptiveAuth() {
        return preemptiveAuth;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public Map<String, String> getDefaultHeaders() {
        return new HashMap<>(defaultHeaders);
    }
}

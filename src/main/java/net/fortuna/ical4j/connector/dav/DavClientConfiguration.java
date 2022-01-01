package net.fortuna.ical4j.connector.dav;

public class DavClientConfiguration {

    private boolean preemptiveAuth;

    private boolean followRedirects;

    public DavClientConfiguration withPreemptiveAuth(boolean preemptiveAuth) {
        this.preemptiveAuth = preemptiveAuth;
        return this;
    }

    public DavClientConfiguration withFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public boolean isPreemptiveAuth() {
        return preemptiveAuth;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }
}

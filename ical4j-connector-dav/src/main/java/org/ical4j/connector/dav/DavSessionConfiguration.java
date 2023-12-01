package org.ical4j.connector.dav;

import org.apache.http.client.CredentialsProvider;

public class DavSessionConfiguration {

    private String user;

    private char[] password;

    private String bearerAuth;

    private CredentialsProvider credentialsProvider;

    private String workspace;

    public DavSessionConfiguration withUser(String user) {
        this.user = user;
        return this;
    }

    public DavSessionConfiguration withPassword(char[] password) {
        this.password = password;
        return this;
    }

    public DavSessionConfiguration withBearerAuth(String bearerAuth) {
        this.bearerAuth = bearerAuth;
        return this;
    }

    public DavSessionConfiguration withCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public DavSessionConfiguration withWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public String getUser() {
        return user;
    }

    public char[] getPassword() {
        return password;
    }

    public String getBearerAuth() {
        return bearerAuth;
    }

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public String getWorkspace() {
        return workspace;
    }
}

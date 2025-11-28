package org.ical4j.connector.dav;

import org.apache.http.client.CredentialsProvider;

/**
 * DavSessionConfiguration is used to configure the session for a DAV client.
 * It allows setting user credentials, bearer authentication, and workspace information.
 *
 * This class is mutable and provides methods to chain configuration settings.
 *
 * Created on 24/02/2008
 *
 * @author Ben
 */
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

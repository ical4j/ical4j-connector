package org.ical4j.connector.dav

import org.apache.http.auth.AuthScope
import org.apache.http.auth.Credentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider

interface RadicaleTestSupport {

    default String getContainerImageName() { 'tomsquest/docker-radicale' }

    default int getContainerPort() { 5232 }

    default PathResolver getPathResolver() { PathResolver.Defaults.RADICALE }

    default CredentialsProvider getCredentialsProvider() {
        Credentials credentials = new UsernamePasswordCredentials('admin', 'admin');
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        credentialsProvider
    }
}

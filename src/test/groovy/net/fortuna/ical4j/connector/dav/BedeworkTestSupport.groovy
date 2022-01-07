package net.fortuna.ical4j.connector.dav

import org.apache.http.auth.AuthScope
import org.apache.http.auth.Credentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider

interface BedeworkTestSupport {

    default String getContainerImageName() { 'ioggstream/bedework' }

    default int getContainerPort() { 8080 }

    default PathResolver getPathResolver() { PathResolver.Defaults.BEDEWORK }

    default CredentialsProvider getCredentialsProvider() {
        Credentials credentials = new UsernamePasswordCredentials('admin', 'admin');
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        credentialsProvider
    }
}

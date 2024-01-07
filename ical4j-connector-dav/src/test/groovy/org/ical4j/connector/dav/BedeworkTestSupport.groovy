package org.ical4j.connector.dav

import org.apache.http.auth.AuthScope
import org.apache.http.auth.Credentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider

interface BedeworkTestSupport {

    default String getContainerImageName() { 'ioggstream/bedework' }

    default int getContainerPort() { 8080 }

    default List<Tuple<?>> getBindMounts() {
        []
    }

    default String getRepositoryPath() { '/ucaldav' }

    default PathResolver getPathResolver() { PathResolver.Defaults.BEDEWORK }

    default String getUser() { 'test' }

    default String getWorkspace() { 'test' }

    default Map<String, ?> getExpectedValues() {
        ['calendar-home-set': '/calendars/test']
    }

    default CredentialsProvider getCredentialsProvider() {
        Credentials credentials = new UsernamePasswordCredentials(getUser(), 'test');
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        credentialsProvider
    }
}

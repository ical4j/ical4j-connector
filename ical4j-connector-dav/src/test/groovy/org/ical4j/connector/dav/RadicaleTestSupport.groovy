package org.ical4j.connector.dav

import org.apache.http.auth.AuthScope
import org.apache.http.auth.Credentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.testcontainers.containers.BindMode

import static org.ical4j.connector.dav.SupportedFeature.*

interface RadicaleTestSupport {

    default String getContainerImageName() { 'tomsquest/docker-radicale' }

    default int getContainerPort() { 5232 }

    default List<Tuple<?>> getBindMounts() {
        [
                Tuple.tuple('src/test/resources/radicale', '/config', BindMode.READ_ONLY),
        ]
    }

    default String getRepositoryPath() { '/' }

    default String getPathPrefix() { '/' }

    default PathResolver getPathResolver() { PathResolver.Defaults.RADICALE }

    default String getUser() { 'test' }

    default String getWorkspace() { 'test' }

    default CredentialsProvider getCredentialsProvider() {
        Credentials credentials = new UsernamePasswordCredentials(getUser(), 'test');
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        credentialsProvider
    }

    default Map<String, ?> getExpectedValues() {
        [
                'calendar-home-set': '/test',
                'supported-features': [CALENDAR_ACCESS, ADDRESSBOOK, EXTENDED_MKCOL]
        ]
    }
}

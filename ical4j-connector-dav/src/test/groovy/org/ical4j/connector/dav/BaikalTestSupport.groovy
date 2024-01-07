package org.ical4j.connector.dav

import org.apache.http.auth.AuthScope
import org.apache.http.auth.Credentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.testcontainers.containers.BindMode

import static org.ical4j.connector.dav.SupportedFeature.*

interface BaikalTestSupport {

    default String getContainerImageName() { 'ckulka/baikal:nginx' }

    default int getContainerPort() { 80 }

    default List<Tuple3<String, String, BindMode>> getBindMounts() {
        [
            Tuple.tuple('src/test/resources/baikal/config', '/var/www/baikal/config', BindMode.READ_WRITE),
            Tuple.tuple('src/test/resources/baikal/Specific', '/var/www/baikal/Specific', BindMode.READ_WRITE)
        ]
    }

    default String getRepositoryPath() { '/dav.php' }

    default PathResolver getPathResolver() { PathResolver.Defaults.BAIKAL }

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
                'calendar-home-set': '/dav.php/calendars/test/',
                'supported-features': [EXTENDED_MKCOL, ACCESS_CONTROL, CALENDARSERVER_PRINCIPAL_PROPERTY_SEARCH,
                                       CALENDAR_ACCESS, CALENDAR_PROXY, CALENDAR_AUTO_SCHEDULE, CALENDAR_AVAILABILITY,
                                       CALENDARSERVER_SHARING, ADDRESSBOOK]
        ]
    }
}

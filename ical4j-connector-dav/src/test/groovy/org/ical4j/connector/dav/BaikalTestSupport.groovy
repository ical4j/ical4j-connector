package org.ical4j.connector.dav

import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.testcontainers.containers.BindMode

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import static org.ical4j.connector.dav.SupportedFeature.*

interface BaikalTestSupport {

    default String getContainerImageName() { 'ckulka/baikal:nginx' }

    default int getContainerPort() { 80 }

    default List<Tuple3<String, String, BindMode>> getBindMounts() {
        // mount a copy of the baikal config and db so test runs don't modify the checked-in resources..
        def source = Paths.get('src/test/resources/baikal')
        Files.createDirectories(Paths.get('build'))
        def target = Files.createTempDirectory(Paths.get('build'), 'baikal').toAbsolutePath()
        Files.walk(source).withCloseable { paths ->
            paths.filter { it != source }.forEach { path ->
                Files.copy(path, target.resolve(source.relativize(path).toString()), StandardCopyOption.COPY_ATTRIBUTES)
            }
        }
        [
            Tuple.tuple(target.resolve('config').toString(), '/var/www/baikal/config', BindMode.READ_WRITE),
            Tuple.tuple(target.resolve('Specific').toString(), '/var/www/baikal/Specific', BindMode.READ_WRITE)
        ]
    }

    default String getRepositoryPath() { '/dav.php' }

    default PathResolver getPathResolver() { PathResolver.Defaults.BAIKAL }

    default String getUser() { 'test' }

    default String getWorkspace() { 'test' }

    default CredentialsProvider getCredentialsProvider() {
        def credentials = new UsernamePasswordCredentials(getUser(), 'test');
        def credentialsProvider = new BasicCredentialsProvider();
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

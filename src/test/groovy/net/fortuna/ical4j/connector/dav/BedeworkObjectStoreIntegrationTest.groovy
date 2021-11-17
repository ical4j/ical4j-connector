package net.fortuna.ical4j.connector.dav

import spock.lang.Ignore

@Ignore
class BedeworkObjectStoreIntegrationTest extends AbstractDavObjectStoreIntegrationTest {

    @Override
    String getContainerImageName() {
        return 'ioggstream/bedework'
    }

    @Override
    int getContainerPort() {
        return 8080
    }

    @Override
    PathResolver getPathResolver() {
        return PathResolver.BEDEWORK
    }
}

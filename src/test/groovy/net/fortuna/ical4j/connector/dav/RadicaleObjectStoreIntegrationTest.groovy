package net.fortuna.ical4j.connector.dav

class RadicaleObjectStoreIntegrationTest extends AbstractDavObjectStoreIntegrationTest {

    @Override
    String getContainerImageName() {
        return 'tomsquest/docker-radicale'
    }

    @Override
    int getContainerPort() {
        return 5232
    }

    @Override
    PathResolver getPathResolver() {
        return PathResolver.RADICALE
    }
}

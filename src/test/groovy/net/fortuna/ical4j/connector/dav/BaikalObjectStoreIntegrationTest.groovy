package net.fortuna.ical4j.connector.dav

class BaikalObjectStoreIntegrationTest extends AbstractDavObjectStoreIntegrationTest {

    @Override
    String getContainerImageName() {
        return 'ckulka/baikal:nginx'
    }

    @Override
    int getContainerPort() {
        return 80
    }

    @Override
    PathResolver getPathResolver() {
        return PathResolver.BAIKAL
    }
}

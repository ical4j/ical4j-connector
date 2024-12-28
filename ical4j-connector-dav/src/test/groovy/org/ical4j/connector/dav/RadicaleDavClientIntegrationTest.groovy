package org.ical4j.connector.dav


import org.testcontainers.utility.MountableFile

class RadicaleDavClientIntegrationTest extends AbstractDavClientIntegrationTest implements RadicaleTestSupport {

    def setup() {
        container.withCopyToContainer(MountableFile.forHostPath('src/test/resources/radicale/htpasswd',),
                '/etc/radicale/users')
    }
}

package org.ical4j.connector.dav

import org.testcontainers.containers.BindMode

class RadicaleDavClientIntegrationTest extends AbstractDavClientIntegrationTest implements RadicaleTestSupport {

    def setup() {
        container.addFileSystemBind('src/test/resources/htpasswd', '/etc/radicale/users', BindMode.READ_ONLY)
    }
}

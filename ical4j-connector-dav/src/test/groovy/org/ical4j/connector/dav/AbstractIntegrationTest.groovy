package org.ical4j.connector.dav

import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
abstract class AbstractIntegrationTest extends Specification {

    @Shared
    GenericContainer container = new GenericContainer(getContainerImageName())
//            .withFileSystemBind(getConfigPath(), getContainerConfigPath(), BindMode.READ_ONLY)
            .withExposedPorts(getContainerPort())
    .withFileSystemBind('src/test/resources/baikal/db', '/var/www/baikal/Specific/db', BindMode.READ_WRITE)
    .withFileSystemBind('src/test/resources/baikal/config', '/var/www/baikal/config', BindMode.READ_WRITE)


    abstract String getContainerImageName();

    abstract int getContainerPort();

    abstract String getConfigPath();

    abstract String getContainerConfigPath();

    abstract String getRepositoryPath();

    String getContainerUrl() {
        "http://$container.containerIpAddress:${container.getMappedPort(getContainerPort())}${getRepositoryPath()}"
    }
}

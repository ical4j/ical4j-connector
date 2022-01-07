package net.fortuna.ical4j.connector

import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
abstract class AbstractIntegrationTest extends Specification {

    @Shared
    GenericContainer container = new GenericContainer(getContainerImageName())
            .withExposedPorts(getContainerPort())

    abstract String getContainerImageName();

    abstract int getContainerPort();

    String getContainerUrl() {
        "http://$container.containerIpAddress:${container.getMappedPort(getContainerPort())}"
    }
}

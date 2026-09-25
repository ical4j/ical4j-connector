package org.ical4j.connector.dav

import org.apache.http.client.CredentialsProvider
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
abstract class AbstractIntegrationTest extends Specification {

    @Shared
    def container = new GenericContainer(getContainerImageName())
            .withExposedPorts(getContainerPort())
            // wait until the DAV endpoint answers (e.g. 401), not just until the port is open,
            // as servers like Baikal accept connections before their backend is ready..
            .waitingFor(Wait.forHttp(getRepositoryPath()).forStatusCodeMatching { it < 500 })
            .with(container -> {
        getBindMounts().forEach { container.withFileSystemBind(it.v1, it.v2, it.v3)}
        container
    })

    def setupSpec() {
        container.start()
    }

    abstract String getContainerImageName();

    abstract int getContainerPort();

    abstract List<Tuple3<String, String, BindMode>> getBindMounts();

    abstract String getRepositoryPath();

    String getContainerUrl() {
        "http://$container.containerIpAddress:${container.getMappedPort(getContainerPort())}${getRepositoryPath()}"
    }

    abstract PathResolver getPathResolver();

    abstract CredentialsProvider getCredentialsProvider()

    abstract String getUser()

    abstract String getWorkspace()

    abstract Map<String, ?> getExpectedValues()
}

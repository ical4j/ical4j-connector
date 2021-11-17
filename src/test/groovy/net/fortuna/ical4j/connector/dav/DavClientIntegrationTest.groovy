package net.fortuna.ical4j.connector.dav

import net.fortuna.ical4j.connector.AbstractIntegrationTest
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.client.BasicCredentialsProvider
import org.testcontainers.spock.Testcontainers

@Testcontainers
class DavClientIntegrationTest extends AbstractIntegrationTest {

    @Override
    String getContainerImageName() {
        return 'nginx'
    }

    @Override
    int getContainerPort() {
        return 80
    }

    def 'assert preemptive auth configuration'() {
        given: 'a dav client factory configured for preemptive auth'
        DavClientFactory clientFactory = [true]

        when: 'a new client instance is created'
        DavClient client = clientFactory.newInstance(URI.create("http://$container.containerIpAddress:${container.getMappedPort(getContainerPort())}").toURL(), '', '');

        and: 'a session is initiated'
        BasicCredentialsProvider credentialsProvider = []
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials('', ''))
        client.begin(credentialsProvider)

        then: 'the client is configured for preemptive auth'
        client.httpClientContext.authCache instanceof BasicAuthCache
    }
}

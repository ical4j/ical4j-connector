package org.ical4j.connector.dav

class BaikalIntegrationTest extends AbstractIntegrationTest implements BaikalTestSupport {

    def 'test client authentication'() {
        given: 'a dav client instance'
        def href = URI.create(getContainerUrl()).toURL()
        def client = new DavClientFactory().withPreemptiveAuth(true)
                .withFollowRedirects(true).newInstance(href)

        when: 'a session is started'
        def supportedFeatures = client.begin(getCredentialsProvider())

        then: 'authentication is successful'
        client.getSupportedFeatures() == expectedValues['supported-features']
    }
}

package net.fortuna.ical4j.connector.dav

import spock.lang.Specification

class DavClientTest extends Specification {

    def 'assert preemptive auth configuration'() {
        given: 'a dav client factory configured for preemptive auth'
        DavClientFactory clientFactory = [true]

        when: 'a new client instance is created'
        DavClient client = clientFactory.newInstance(URI.create('http://dav.example.com').toURL(), '', '');

        and: 'a session is initated'
        client.begin()

        then: 'the client is configured for preemptive auth'
        client.httpClient.params.authenticationPreemptive == true
    }
}

package org.ical4j.connector.dav

import com.sun.net.httpserver.HttpServer
import spock.lang.Specification

/**
 * Verifies that user/password supplied via {@link DavSessionConfiguration} are used to
 * authenticate with the DAV server (see issue #59), and that bearer tokens are likewise applied.
 */
class DavSessionAuthenticationTest extends Specification {

    HttpServer server

    List<String> authorizationHeaders = []

    def setup() {
        def expected = 'Basic ' + 'alice:secret'.bytes.encodeBase64().toString()
        server = HttpServer.create(new InetSocketAddress('localhost', 0), 0)
        server.createContext('/') { exchange ->
            def authorization = exchange.requestHeaders.getFirst('Authorization')
            authorizationHeaders << authorization
            if (authorization == expected || authorization == 'Bearer token123') {
                exchange.responseHeaders.add('DAV', '1, 2, calendar-access')
                exchange.sendResponseHeaders(207, -1)
            } else {
                exchange.responseHeaders.add('WWW-Authenticate', 'Basic realm="test"')
                exchange.sendResponseHeaders(401, -1)
            }
            exchange.close()
        }
        server.start()
    }

    def cleanup() {
        server.stop(0)
    }

    def newStore() {
        new CalDavCalendarStore('-//Test//iCal4j Connector//EN',
                URI.create("http://localhost:${server.address.port}/").toURL(), PathResolver.Defaults.GENERIC)
    }

    def 'test connect authenticates with session user and password'() {
        given: 'a store'
        def store = newStore()

        when: 'connecting with user and password'
        def connected = store.connect(new DavSessionConfiguration().withUser('alice')
                .withPassword('secret'.toCharArray()))

        then: 'the connection succeeds using the supplied credentials'
        connected
        store.supportedFeatures().contains(SupportedFeature.CALENDAR_ACCESS)
        authorizationHeaders.last() == 'Basic ' + 'alice:secret'.bytes.encodeBase64().toString()
    }

    def 'test deprecated connect with username and password authenticates'() {
        given: 'a store'
        def store = newStore()

        expect: 'the deprecated connect method also authenticates'
        store.connect('alice', 'secret'.toCharArray())
    }

    def 'test connect with incorrect password fails'() {
        given: 'a store'
        def store = newStore()

        when: 'connecting with the wrong password'
        store.connect(new DavSessionConfiguration().withUser('alice')
                .withPassword('wrong'.toCharArray()))

        then: 'the connection is rejected'
        thrown(RuntimeException)
    }

    def 'test connect authenticates with session bearer token'() {
        given: 'a store'
        def store = newStore()

        when: 'connecting with a bearer token'
        def connected = store.connect(new DavSessionConfiguration().withUser('alice').withBearerAuth('token123'))

        then: 'the connection succeeds using the bearer token'
        connected
        authorizationHeaders == ['Bearer token123']
    }

    def 'test deprecated connect with bearer token authenticates'() {
        given: 'a store'
        def store = newStore()

        expect: 'the deprecated connect method also authenticates'
        store.connect('token123')
    }

    def 'test bearer token is not reused by a later connection'() {
        given: 'a store previously connected with a bearer token'
        def store = newStore()
        store.connect(new DavSessionConfiguration().withUser('alice').withBearerAuth('token123'))
        authorizationHeaders.clear()

        when: 'reconnecting without credentials'
        store.connect(new DavSessionConfiguration().withUser('alice'))

        then: 'the bearer token is no longer sent'
        thrown(RuntimeException)
        authorizationHeaders == [null]
    }
}

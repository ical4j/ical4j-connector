package org.ical4j.connector.dav

import com.sun.net.httpserver.HttpServer
import org.ical4j.connector.ObjectNotFoundException
import org.ical4j.connector.ObjectStoreException
import spock.lang.Specification

/**
 * Verifies home-set discovery falls back to {@code current-user-principal} where the configured
 * principal path doesn't provide a home-set (see issue #57).
 */
class HomeSetDiscoveryTest extends Specification {

    HttpServer server

    /**
     * PROPFIND responses by request path, as [status, body]. Unmapped paths return 404.
     */
    Map<String, List> routes = [:]

    List<String> requests = []

    def setup() {
        server = HttpServer.create(new InetSocketAddress('localhost', 0), 0)
        server.createContext('/') { exchange ->
            def path = exchange.requestURI.rawPath
            requests << "${exchange.requestMethod} ${path}".toString()
            def route = routes[path]
            exchange.responseHeaders.add('DAV', '1, 2, calendar-access, addressbook')
            if (route) {
                def body = route[1].toString().getBytes('UTF-8')
                exchange.responseHeaders.add('Content-Type', 'application/xml; charset=utf-8')
                exchange.sendResponseHeaders(route[0] as int, body.length)
                exchange.responseBody.write(body)
            } else {
                exchange.sendResponseHeaders(404, -1)
            }
            exchange.close()
        }
        server.start()
        // the server root, used on connect and for current-user-principal discovery
        routes['/'] = [207, multistatus(response('/', ''))]
    }

    def cleanup() {
        server.stop(0)
    }

    String baseUrl() {
        "http://localhost:${server.address.port}"
    }

    static String multistatus(String... responses) {
        """<?xml version="1.0" encoding="utf-8"?>
<d:multistatus xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav" xmlns:card="urn:ietf:params:xml:ns:carddav">${responses.join()}</d:multistatus>"""
    }

    static String response(String href, String okProps, String notFoundProps = null) {
        def propstats = "<d:propstat><d:prop>${okProps}</d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat>"
        if (notFoundProps) {
            propstats += "<d:propstat><d:prop>${notFoundProps}</d:prop><d:status>HTTP/1.1 404 Not Found</d:status></d:propstat>"
        }
        "<d:response><d:href>${href}</d:href>${propstats}</d:response>"
    }

    static String calendarHomeSet(String href) {
        "<c:calendar-home-set><d:href>${href}</d:href></c:calendar-home-set>"
    }

    static String currentUserPrincipal(String href) {
        "<d:current-user-principal><d:href>${href}</d:href></d:current-user-principal>"
    }

    def withCalendarHome(String homePath) {
        routes[homePath] = [207, multistatus(
                response(homePath, '<d:resourcetype><d:collection/></d:resourcetype>'),
                response("${homePath}work/", '<d:resourcetype><d:collection/><c:calendar/></d:resourcetype>'))]
    }

    def connectedCalendarStore(DavSessionConfiguration session = new DavSessionConfiguration().withUser('alice')) {
        def store = new CalDavCalendarStore('-//Test//iCal4j Connector//EN',
                URI.create("${baseUrl()}/").toURL(), PathResolver.Defaults.GENERIC)
        store.connect(session)
        store
    }

    def 'test calendar-home-set from the configured principal path is used'() {
        given: 'a principal path that provides the calendar-home-set'
        routes['/alice'] = [207, multistatus(response('/alice', calendarHomeSet('/calendars/alice/')))]
        withCalendarHome('/calendars/alice/')

        when: 'collections are listed'
        def collections = connectedCalendarStore().getCollections()

        then: 'they are listed from the home-set'
        collections*.id == ['work']

        and: 'current-user-principal discovery is not needed'
        requests.count { it == 'PROPFIND /' } == 1
    }

    def 'test fallback to current-user-principal when principal has no calendar-home-set'() {
        given: 'a principal path without a calendar-home-set'
        routes['/alice'] = [207, multistatus(response('/alice', '', '<c:calendar-home-set/>'))]

        and: 'a server root that reports the current user principal'
        routes['/'] = [207, multistatus(response('/', currentUserPrincipal('/principals/users/alice@example.com/')))]
        routes['/principals/users/alice@example.com/'] = [207, multistatus(
                response('/principals/users/alice@example.com/', calendarHomeSet('/calendars/alice@example.com/')))]
        withCalendarHome('/calendars/alice@example.com/')

        expect: 'collections are listed from the discovered home-set'
        connectedCalendarStore().getCollections()*.id == ['work']
    }

    def 'test fallback to current-user-principal when principal path is not found'() {
        given: 'a principal path that does not exist, and a server root that reports the principal'
        routes['/'] = [207, multistatus(response('/', currentUserPrincipal('/principals/alice/')))]
        routes['/principals/alice/'] = [207, multistatus(
                response('/principals/alice/', calendarHomeSet('/calendars/alice/')))]
        withCalendarHome('/calendars/alice/')

        expect: 'collections are listed from the discovered home-set'
        connectedCalendarStore().getCollections()*.id == ['work']
    }

    def 'test absolute URLs for current-user-principal and calendar-home-set are resolved as paths'() {
        given: 'a server returning absolute URLs'
        routes['/'] = [207, multistatus(response('/', currentUserPrincipal("${baseUrl()}/principals/alice/")))]
        routes['/principals/alice/'] = [207, multistatus(
                response('/principals/alice/', calendarHomeSet("${baseUrl()}/calendars/alice/")))]
        withCalendarHome('/calendars/alice/')

        when: 'the calendar-home-set is found'
        def store = connectedCalendarStore()

        then: 'it is resolved to a path'
        store.findCalendarHomeSet() == '/calendars/alice/'
        store.getCollections()*.id == ['work']
    }

    def 'test calendar-home-set is discovered without a configured user'() {
        given: 'a server root that reports the current user principal'
        routes['/'] = [207, multistatus(response('/', currentUserPrincipal('/principals/alice/')))]
        routes['/principals/alice/'] = [207, multistatus(
                response('/principals/alice/', calendarHomeSet('/calendars/alice/')))]

        expect: 'the calendar-home-set is found'
        connectedCalendarStore(new DavSessionConfiguration()).findCalendarHomeSet() == '/calendars/alice/'
    }

    def 'test missing calendar-home-set without current-user-principal is not found'() {
        given: 'a principal without a calendar-home-set and no current-user-principal'
        routes['/alice'] = [207, multistatus(response('/alice', '', '<c:calendar-home-set/>'))]

        when: 'collections are listed'
        connectedCalendarStore().getCollections()

        then: 'a not found exception is thrown'
        thrown(ObjectNotFoundException)
    }

    def 'test principal path failure is reported when discovery also fails'() {
        given: 'a principal path that does not exist and no current-user-principal'

        when: 'collections are listed'
        connectedCalendarStore().getCollections()

        then: 'the original failure is reported'
        def e = thrown(ObjectStoreException)
        e.cause.message == 'Unexpected status code: 404'
    }

    def 'test address book home-set falls back to current-user-principal'() {
        given: 'a principal path without an addressbook-home-set'
        routes['/alice'] = [207, multistatus(response('/alice', '', '<card:addressbook-home-set/>'))]

        and: 'a server root that reports the current user principal'
        routes['/'] = [207, multistatus(response('/', currentUserPrincipal('/principals/alice/')))]
        routes['/principals/alice/'] = [207, multistatus(response('/principals/alice/',
                '<card:addressbook-home-set><d:href>/addressbooks/alice/</d:href></card:addressbook-home-set>'))]
        routes['/addressbooks/alice/'] = [207, multistatus(
                response('/addressbooks/alice/', '<d:resourcetype><d:collection/></d:resourcetype>'),
                response('/addressbooks/alice/contacts/',
                        '<d:resourcetype><d:collection/><card:addressbook/></d:resourcetype>'))]

        and: 'a connected card store'
        def store = new CardDavStore('-//Test//iCal4j Connector//EN',
                URI.create("${baseUrl()}/").toURL(), PathResolver.Defaults.GENERIC)
        store.connect(new DavSessionConfiguration().withUser('alice'))

        expect: 'address books are listed from the discovered home-set'
        store.getCollections()*.id == ['contacts']
    }
}

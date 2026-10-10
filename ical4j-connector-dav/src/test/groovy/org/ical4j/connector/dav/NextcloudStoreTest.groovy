package org.ical4j.connector.dav

import com.sun.net.httpserver.HttpServer
import net.fortuna.ical4j.data.CalendarBuilder
import spock.lang.Specification

/**
 * Verifies the store works against a server using the Nextcloud DAV layout under {@code /remote.php/dav}
 * (see issue #53).
 */
class NextcloudStoreTest extends Specification {

    static final String DAV = '/remote.php/dav'

    static final String USER = 'alice@example.com'

    HttpServer server

    /**
     * Responses by "METHOD path", as [status, body]. Unmapped requests return 404.
     */
    Map<CharSequence, List> routes = [:]

    List<String> requests = []

    def setup() {
        server = HttpServer.create(new InetSocketAddress('localhost', 0), 0)
        server.createContext('/') { exchange ->
            def request = "${exchange.requestMethod} ${exchange.requestURI.rawPath}".toString()
            requests << request
            exchange.requestBody.bytes
            def route = routes.find { it.key.toString() == request }?.value
            exchange.responseHeaders.add('DAV', '1, 3, access-control, calendar-access, addressbook')
            if (route && route[1]) {
                def body = route[1].toString().getBytes('UTF-8')
                exchange.responseHeaders.add('Content-Type', 'application/xml; charset=utf-8')
                exchange.sendResponseHeaders(route[0] as int, body.length)
                exchange.responseBody.write(body)
            } else {
                exchange.sendResponseHeaders(route ? route[0] as int : 404, -1)
            }
            exchange.close()
        }
        server.start()

        routes["PROPFIND ${DAV}"] = [207, multistatus(response("${DAV}/", ''))]
        routes["PROPFIND ${DAV}/principals/users/${USER}/"] = [207, multistatus(
                response("${DAV}/principals/users/${USER}/",
                        "<c:calendar-home-set><d:href>${DAV}/calendars/${USER}/</d:href></c:calendar-home-set>" +
                        "<card:addressbook-home-set><d:href>${DAV}/addressbooks/users/${USER}/</d:href></card:addressbook-home-set>"))]

        def calendar = '<d:resourcetype><d:collection/><c:calendar/></d:resourcetype><d:displayname>Personal</d:displayname>'
        routes["PROPFIND ${DAV}/calendars/${USER}/"] = [207, multistatus(
                response("${DAV}/calendars/${USER}/", '<d:resourcetype><d:collection/></d:resourcetype>'),
                response("${DAV}/calendars/${USER}/personal/", calendar))]
        routes["PROPFIND ${DAV}/calendars/${USER}/personal/"] = [207, multistatus(
                response("${DAV}/calendars/${USER}/personal/", calendar))]

        def addressBook = '<d:resourcetype><d:collection/><card:addressbook/></d:resourcetype>'
        routes["PROPFIND ${DAV}/addressbooks/users/${USER}/"] = [207, multistatus(
                response("${DAV}/addressbooks/users/${USER}/", '<d:resourcetype><d:collection/></d:resourcetype>'),
                response("${DAV}/addressbooks/users/${USER}/contacts/", addressBook))]
        routes["PROPFIND ${DAV}/addressbooks/users/${USER}/contacts/"] = [207, multistatus(
                response("${DAV}/addressbooks/users/${USER}/contacts/", addressBook))]
    }

    def cleanup() {
        server.stop(0)
    }

    static String multistatus(String... responses) {
        """<?xml version="1.0" encoding="utf-8"?>
<d:multistatus xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav" xmlns:card="urn:ietf:params:xml:ns:carddav">${responses.join()}</d:multistatus>"""
    }

    static String response(String href, String okProps) {
        "<d:response><d:href>${href}</d:href><d:propstat><d:prop>${okProps}</d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>"
    }

    URL serverUrl() {
        URI.create("http://localhost:${server.address.port}${DAV}").toURL()
    }

    def 'test calendar collections are listed and resolved at nextcloud paths'() {
        given: 'a store connected with only a user, as in the issue'
        def store = new CalDavCalendarStore('-//Test//iCal4j Connector//EN', serverUrl(),
                PathResolver.Defaults.NEXTCLOUD)
        store.connect(new DavSessionConfiguration().withUser(USER))

        when: 'collections are listed'
        def collections = store.getCollections()

        then: 'the calendar is found in the home-set'
        collections*.id == ['personal']

        and: 'listed collections resolve to their nextcloud path'
        collections[0].path == "/calendars/${USER}/personal/"

        when: 'the collection is retrieved by id'
        def collection = store.getCollection('personal')

        then: 'it is found at the nextcloud calendar path'
        collection.id == 'personal'
        requests.contains("PROPFIND ${DAV}/calendars/${USER}/personal/".toString())
    }

    def 'test calendar objects are written to the nextcloud calendar path'() {
        given: 'a calendar collection'
        def store = new CalDavCalendarStore('-//Test//iCal4j Connector//EN', serverUrl(),
                PathResolver.Defaults.NEXTCLOUD)
        store.connect(new DavSessionConfiguration().withUser(USER))
        def collection = store.getCollections()[0]

        and: 'a server accepting the new object'
        routes["PUT ${DAV}/calendars/${USER}/personal/event-1.ics"] = [201, null]

        and: 'a calendar object'
        def calendar = new CalendarBuilder().build(new StringReader('''BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//Test//iCal4j Connector//EN
BEGIN:VEVENT
UID:event-1
DTSTAMP:20261010T000000Z
DTSTART:20261011T090000Z
SUMMARY:Test
END:VEVENT
END:VCALENDAR
'''.replace('\n', '\r\n')))

        when: 'the calendar is added'
        collection.add(calendar)

        then: 'it is written to the collection on the server'
        requests.contains("PUT ${DAV}/calendars/${USER}/personal/event-1.ics".toString())
    }

    def 'test address book collections are listed and resolved at nextcloud paths'() {
        given: 'a card store connected with only a user'
        def store = new CardDavStore('-//Test//iCal4j Connector//EN', serverUrl(), PathResolver.Defaults.NEXTCLOUD)
        store.connect(new DavSessionConfiguration().withUser(USER))

        expect: 'the address book is found in the home-set'
        store.getCollections()*.id == ['contacts']

        and: 'it is resolved at the nextcloud address book path'
        store.getCollection('contacts').id == 'contacts'
        requests.contains("PROPFIND ${DAV}/addressbooks/users/${USER}/contacts/".toString())
    }
}

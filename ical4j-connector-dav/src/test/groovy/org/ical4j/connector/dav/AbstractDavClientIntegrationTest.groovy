package org.ical4j.connector.dav


import net.fortuna.ical4j.model.ContentBuilder
import net.fortuna.ical4j.model.property.DtStart
import org.apache.http.impl.client.BasicAuthCache
import org.apache.jackrabbit.webdav.DavException
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet
import org.apache.jackrabbit.webdav.property.DavPropertySet
import org.ical4j.connector.dav.property.DavPropertyBuilder
import org.ical4j.connector.dav.request.CalendarQuery
import spock.lang.Ignore
import spock.lang.Shared

import java.time.LocalDateTime

import static org.apache.jackrabbit.webdav.property.DavPropertyName.*
import static org.apache.jackrabbit.webdav.security.SecurityConstants.*
import static org.ical4j.connector.dav.property.BaseDavPropertyName.CURRENT_USER_PRINCIPAL
import static org.ical4j.connector.dav.property.BaseDavPropertyName.SUPPORTED_REPORT_SET
import static org.ical4j.connector.dav.property.CalDavPropertyName.*
import static org.ical4j.connector.dav.property.CardDavPropertyName.ADDRESSBOOK_HOME_SET

abstract class AbstractDavClientIntegrationTest extends AbstractIntegrationTest {

    @Shared
    CalDavSupport client

    def setupSpec() {
        DavClientFactory clientFactory = new DavClientFactory().withPreemptiveAuth(true)
        URL href = URI.create(getContainerUrl()).toURL()
        DefaultDavClient client = clientFactory.newInstance(href);
        client.begin(getCredentialsProvider())
        this.client = client;
    }

    def 'assert preemptive auth configuration'() {
        given: 'a dav client factory configured for preemptive auth'
        DavClientFactory clientFactory = new DavClientFactory().withPreemptiveAuth(true)

        when: 'a new client instance is created'
        URL href = URI.create(getContainerUrl()).toURL()
        DefaultDavClient client = clientFactory.newInstance(href);

        and: 'a session is initiated'
        client.begin(getCredentialsProvider())

        then: 'the client is configured for preemptive auth'
        client.httpClientContext.authCache instanceof BasicAuthCache
    }

    def 'test client authentication'() {
        given: 'a dav client instance'
        URL href = URI.create(getContainerUrl()).toURL()
        def client = new DavClientFactory().withPreemptiveAuth(true)
                .withFollowRedirects(true).withCredentialsProvider(getCredentialsProvider())
                .newInstance(href)

        when: 'a session is started'
//        client.begin(getCredentialsProvider())
        def supportedFeatures= client.getSupportedFeatures()

        then: 'authentication is successful'
        supportedFeatures == expectedValues['supported-features']
    }

    def 'test create collection'() {
        given: 'a dav client instance'
        URL href = URI.create(getContainerUrl()).toURL()
        def client = new DavClientFactory().withPreemptiveAuth(true)
                .newInstance(href)

        and: 'a resource path'
        def path = getPathResolver().getCalendarPath('newcol', getWorkspace())

        when: 'a session is started'
        client.begin(getCredentialsProvider())

        and: 'a new collection is created'
        DavPropertySet props = []
        props.add(new DavPropertyBuilder<>().name(DISPLAYNAME).value('New Collection').build())
        props.add(new DavPropertyBuilder<>().name(CALENDAR_DESCRIPTION).value('A simple mkcalendar test').build())
        client.mkCalendar(path, props)

        then: 'the collection exists'
        client.propFind(path, props.propertyNames).get(0).getProperties().asList() == props.asList()

        cleanup: 'remove collection'
        client.delete(path)
    }

    def 'test create invalid collection'() {
        given: 'a dav client instance'
        URL href = URI.create(getContainerUrl()).toURL()
        def client = new DavClientFactory().withPreemptiveAuth(true)
                .newInstance(href)

        and: 'a non-existent resource path'
        def path = getPathResolver().getCalendarPath('test', 'notexist')

        when: 'a session is started'
        client.begin(getCredentialsProvider())

        and: 'a new collection is created'
        DavPropertySet props = []
        props.add(new DavPropertyBuilder<>().name(DISPLAYNAME).value('Test Collection').build())
        props.add(new DavPropertyBuilder<>().name(CALENDAR_DESCRIPTION).value('A simple mkcalendar test').build())
        client.mkCalendar(path, props)

        then: 'exception is thrown'
        thrown(DavException)
    }

    @Ignore('not working for radicale')
    def 'test get collection'() {
        given: 'a dav client instance'
        URL href = URI.create(getContainerUrl()).toURL()
        def client = new DavClientFactory().withPreemptiveAuth(true)
                .newInstance(href)

        and: 'a resource path'
        def path = getPathResolver().getCalendarPath('newcol', getWorkspace())

        when: 'a session is started'
        client.begin(getCredentialsProvider())

        and: 'a new collection is created'
        DavPropertySet props = []
        props.add(new DavPropertyBuilder<>().name(DISPLAYNAME).value('Test Collection').build())
        props.add(new DavPropertyBuilder<>().name(CALENDAR_DESCRIPTION).value('A simple mkcalendar test').build())
        client.mkCalendar(path, props)

        then: 'the retrieved calendar props are as expected'
        def result = client.propFind(path, DISPLAYNAME)
        result.get(0).getProperties().get(DISPLAYNAME).value == 'Test Collection'

        and: 'when calendar is added'
        client.put(path + '/test.ics', new ContentBuilder().calendar {
            vevent {
                summary 'test'
                dtstart new DtStart<>(LocalDateTime.now())
            }
        }.withDefaults(), null);

        then: 'calendar details are retrievable'
        CalendarQuery query = []
        def result2 = client.report(path, query, CALENDAR_DATA, GETETAG)
        !result2.isEmpty()

        cleanup: 'remove collection'
        client.delete(path)
    }

    @Ignore('ignore for now until logic can be improved')
    def 'test propfind all'() {
        given: 'a dav client instance'
        URL href = URI.create(getContainerUrl()).toURL()
        def client = new DavClientFactory().withPreemptiveAuth(true)
                .newInstance(href)

        and: 'a resource path'
        def path = getPathResolver().getCalendarPath('newcol', getWorkspace())

        when: 'a session is started'
        client.begin(getCredentialsProvider())

        and: 'a new collection is created'
        DavPropertySet props = []
        props.add(new DavPropertyBuilder<>().name(DISPLAYNAME).value('New Collection').build())
        props.add(new DavPropertyBuilder<>().name(CALENDAR_DESCRIPTION).value('A simple mkcalendar test').build())
        client.mkCalendar(path, props)

        and: 'expected props is defined'
        DavPropertyNameSet propNames = []
        propNames.add(PRINCIPAL_COLLECTION_SET)
        propNames.add(CURRENT_USER_PRIVILEGE_SET)
        propNames.add(CALENDAR_HOME_SET)
        propNames.add(CURRENT_USER_PRINCIPAL)
        propNames.add(PRINCIPAL_URL)
        propNames.add(OWNER)
        propNames.add(ADDRESSBOOK_HOME_SET)
        propNames.add(SUPPORTED_REPORT_SET)
        propNames.add(USER_ADDRESS_SET)
        propNames.add(RESOURCETYPE)

        then: 'propfind type = all is as expected'
        propNames.containsAll client.propFindAll(path).propertyNames

        cleanup: 'remove collection'
        client.delete(path)
    }

    @Ignore
    def 'test propfind principal collection set'() {
        given: 'a dav client instance'
        URL href = URI.create(getContainerUrl()).toURL()
        def client = new DavClientFactory().newInstance(href)

        and: 'a resource path'
        def path = getPathResolver().getCalendarPath('default', getWorkspace())

        when: 'a session is started'
        client.begin(getCredentialsProvider())

        and: 'a new collection is created'
        DavPropertySet props = []
//        props.add(new DefaultDavProperty<>(DavPropertyName.DISPLAYNAME, 'Test Collection'))
//        props.add(new DefaultDavProperty<>(CalDavPropertyName.CALENDAR_DESCRIPTION, 'A simple mkcalendar test'))
//        client.mkCalendar('admin/test', props)

        and: 'expected props is defined'
        props = []
        props.add(new DavPropertyBuilder<String[]>().name(PRINCIPAL_COLLECTION_SET).value(new String[0]).build())

        then: 'propfind type = all is as expected'
        props.containsAll client.propFind(path, PRINCIPAL_COLLECTION_SET)
    }
}

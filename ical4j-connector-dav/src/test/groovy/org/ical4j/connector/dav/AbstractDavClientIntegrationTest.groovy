package org.ical4j.connector.dav

import net.fortuna.ical4j.model.Calendar
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicAuthCache
import org.apache.jackrabbit.webdav.DavException
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet
import org.apache.jackrabbit.webdav.property.DavPropertySet
import org.ical4j.connector.dav.property.DavPropertyBuilder
import spock.lang.Ignore

import static org.apache.jackrabbit.webdav.property.DavPropertyName.DISPLAYNAME
import static org.apache.jackrabbit.webdav.property.DavPropertyName.RESOURCETYPE
import static org.apache.jackrabbit.webdav.security.SecurityConstants.*
import static org.ical4j.connector.dav.SupportedFeature.*
import static org.ical4j.connector.dav.property.BaseDavPropertyName.CURRENT_USER_PRINCIPAL
import static org.ical4j.connector.dav.property.BaseDavPropertyName.SUPPORTED_REPORT_SET
import static org.ical4j.connector.dav.property.CalDavPropertyName.*
import static org.ical4j.connector.dav.property.CardDavPropertyName.ADDRESSBOOK_HOME_SET

abstract class AbstractDavClientIntegrationTest extends AbstractIntegrationTest {

    abstract PathResolver getPathResolver();

    abstract CredentialsProvider getCredentialsProvider()

    String getContainerUrl() {
        "http://$container.containerIpAddress:${container.getMappedPort(getContainerPort())}$pathResolver.rootPath"
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
                .withFollowRedirects(true)
                .newInstance(href)

        when: 'a session is started'
        client.begin(getCredentialsProvider())
        def supportedFeatures= client.getSupportedFeatures()

        then: 'authentication is successful'
        supportedFeatures == Arrays.asList(CALENDAR_ACCESS, ADDRESSBOOK, EXTENDED_MKCOL)
    }

    def 'test create collection'() {
        given: 'a dav client instance'
        URL href = URI.create(getContainerUrl()).toURL()
        def client = new DavClientFactory().withPreemptiveAuth(true)
                .newInstance(href)

        and: 'a resource path'
        def path = getPathResolver().getRepositoryRoot('test', 'admin')

        when: 'a session is started'
        client.begin(getCredentialsProvider())

        and: 'a new collection is created'
        DavPropertySet props = []
        props.add(new DavPropertyBuilder<>().name(DISPLAYNAME).value('Test Collection').build())
        props.add(new DavPropertyBuilder<>().name(CALENDAR_DESCRIPTION).value('A simple mkcalendar test').build())
        client.mkCalendar(path, props)

        then: 'the collection exists'
        client.propFind(path, props.propertyNames).asList() == props.asList()

        cleanup: 'remove collection'
        client.delete(path)
    }

    def 'test create invalid collection'() {
        given: 'a dav client instance'
        URL href = URI.create(getContainerUrl()).toURL()
        def client = new DavClientFactory().withPreemptiveAuth(true)
                .newInstance(href)

        and: 'a non-existent resource path'
        def path = getPathResolver().getRepositoryRoot('test', 'notexist')

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

    def 'test get collection'() {
        given: 'a dav client instance'
        URL href = URI.create(getContainerUrl()).toURL()
        def client = new DavClientFactory().withPreemptiveAuth(true)
                .newInstance(href)

        and: 'a resource path'
        def path = getPathResolver().getRepositoryRoot('test', 'admin')

        when: 'a session is started'
        client.begin(getCredentialsProvider())

        and: 'a new collection is created'
        DavPropertySet props = []
        props.add(new DavPropertyBuilder<>().name(DISPLAYNAME).value('Test Collection').build())
        props.add(new DavPropertyBuilder<>().name(CALENDAR_DESCRIPTION).value('A simple mkcalendar test').build())
        client.mkCalendar(path, props)

        and: 'the collection is retrieved via get'
        Calendar calendar = client.getCalendar(path)

        then: 'the retrieved calendar is as expected'
        calendar != null

        cleanup: 'remove collection'
        client.delete(path)
    }

    def 'test propfind all'() {
        given: 'a dav client instance'
        URL href = URI.create(getContainerUrl()).toURL()
        def client = new DavClientFactory().withPreemptiveAuth(true)
                .newInstance(href)

        and: 'a resource path'
        def path = getPathResolver().getRepositoryRoot('admin', null)

        when: 'a session is started'
        client.begin(getCredentialsProvider())

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
    }

    @Ignore
    def 'test propfind principal collection set'() {
        given: 'a dav client instance'
        URL href = URI.create(getContainerUrl()).toURL()
        def client = new DavClientFactory().newInstance(href)

        and: 'a resource path'
        def path = getPathResolver().getRepositoryRoot('admin', null)

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

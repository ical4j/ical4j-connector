package org.ical4j.connector.dav

import org.apache.http.client.CredentialsProvider
import org.ical4j.connector.CalendarStore

abstract class AbstractCalendarStoreIntegrationTest extends AbstractIntegrationTest {

    abstract PathResolver getPathResolver();

    abstract CredentialsProvider getCredentialsProvider()

    abstract String getUser()

    abstract String getWorkspace()

    def 'test find calendar home set'() {
        given: 'an object store'
        CalendarStore store = new CalDavCalendarStore('ical4j-connector', URI.create(getContainerUrl()).toURL(),
            getPathResolver())

        and: 'a connection is established'
        store.connect(new DavSessionConfiguration().withCredentialsProvider(getCredentialsProvider())
                .withUser(getUser()).withWorkspace(getWorkspace()))

        and: 'a collection is created'
        store.addCollection('testCollection')

        when: 'calendar home set it requested'
        String calendarHomeSet = store.findCalendarHomeSet()
        
        then: 'the calendar home set is retrieved'
        calendarHomeSet != null

        cleanup:
        store.getCollection('testCollection').delete()
    }

    def 'test collection creation'() {
        given: 'an object store'
        CalendarStore store = new CalDavCalendarStore('ical4j-connector', URI.create(getContainerUrl()).toURL(),
            getPathResolver())

        and: 'a connection is established'
        store.connect(new DavSessionConfiguration().withCredentialsProvider(getCredentialsProvider())
                .withUser(getUser()).withWorkspace(getWorkspace()))

        when: 'a new collection is added'
        CalDavCalendarCollection collection = store.addCollection('test')

        then: 'the collection is created'
        collection != null

        cleanup:
        collection.delete()
    }
}

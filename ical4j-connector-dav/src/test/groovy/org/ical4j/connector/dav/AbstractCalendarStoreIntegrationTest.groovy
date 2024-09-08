package org.ical4j.connector.dav


import org.ical4j.connector.CalendarCollection
import org.ical4j.connector.CalendarStore
import spock.lang.Ignore

abstract class AbstractCalendarStoreIntegrationTest extends AbstractIntegrationTest {

    def 'test find calendar home set'() {
        given: 'an object store'
        CalendarStore store = new CalDavCalendarStore('ical4j-connector', URI.create(getContainerUrl()).toURL(),
            getPathResolver())

        and: 'a connection is established'
        store.connect(new DavSessionConfiguration().withCredentialsProvider(getCredentialsProvider())
                .withUser(getUser()).withWorkspace(getWorkspace()))

        and: 'a collection is created'
        CalendarCollection collection = store.addCollection('testCollection5')

        when: 'calendar home set it requested'
        String calendarHomeSet = store.findCalendarHomeSet()
        
        then: 'the calendar home set is retrieved'
        calendarHomeSet == expectedValues['calendar-home-set']

        cleanup:
        collection.delete()
    }

    @Ignore('not working for radicale and baikal')
    def 'test collection creation'() {
        given: 'an object store'
        CalendarStore store = new CalDavCalendarStore('ical4j-connector', URI.create(getContainerUrl()).toURL(),
            getPathResolver())

        and: 'a connection is established'
        store.connect(new DavSessionConfiguration().withCredentialsProvider(getCredentialsProvider())
                .withUser(getUser()).withWorkspace(getWorkspace()))

        when: 'a new collection is added'
        CalDavCalendarCollection collection = store.addCollection('test1')
        CalDavCalendarCollection collection2 = store.addCollection('test2')

        then: 'the collection is created'
        collection != null

        and: 'collections size matches expected'
        store.getCollections().size() == 1

        cleanup:
        collection.delete()
    }
}

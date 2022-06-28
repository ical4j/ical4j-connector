package org.ical4j.connector.dav

import org.ical4j.connector.CalendarStore

abstract class AbstractCalendarStoreIntegrationTest extends AbstractIntegrationTest {

    abstract PathResolver getPathResolver();

    def 'test find calendar home set'() {
        given: 'an object store'
        CalendarStore store = new CalDavCalendarStore('ical4j-connector', URI.create(getContainerUrl()).toURL(),
            getPathResolver())

        and: 'a connection is established'
        store.connect('admin', 'admin'.toCharArray())

        and: 'a collection is created'
        store.addCollection('testCollection')

        when: 'calendar home set it requested'
        String calendarHomeSet = store.findCalendarHomeSet()
        
        then: 'the calendar home set is retrieved'
        calendarHomeSet != null
    }

    def 'test collection creation'() {
        given: 'an object store'
        CalendarStore store = new CalDavCalendarStore('ical4j-connector', URI.create(getContainerUrl()).toURL(),
            getPathResolver())

        and: 'a connection is established'
        store.connect('admin', 'admin'.toCharArray())

        when: 'a new collection is added'
        CalDavCalendarCollection collection = store.addCollection('/test')

        then: 'the collection is created'
        collection != null
    }
}

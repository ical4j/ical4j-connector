package org.ical4j.connector.dav


import org.ical4j.connector.ObjectStore

abstract class AbstractDavObjectStoreIntegrationTest extends AbstractIntegrationTest {

    abstract PathResolver getPathResolver();

    def 'test find calendar home set'() {
        given: 'an object store'
        ObjectStore store = new CalDavCalendarStore('ical4j-connector', URI.create(getContainerUrl()).toURL(),
            getPathResolver())

        and: 'a connection is established'
        store.connect('admin', 'admin'.toCharArray())

        when: 'calendar home set it requested'
        String calendarHomeSet = store.findCalendarHomeSet()
        
        then: 'the calendar home set is retrieved'
        calendarHomeSet != null
    }

    def 'test calendar creation'() {
        given: 'an object store'
        ObjectStore store = new CalDavCalendarStore('ical4j-connector', URI.create(getContainerUrl()).toURL(),
            getPathResolver())

        and: 'a connection is established'
        store.connect('admin', 'admin'.toCharArray())

        when: 'a new collection is added'
        CalDavCalendarCollection collection = store.addCollection('/test/2')

        then: 'the collection is created'
        collection != null
    }
}

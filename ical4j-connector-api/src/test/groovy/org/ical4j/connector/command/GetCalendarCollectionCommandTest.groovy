package org.ical4j.connector.command

import org.ical4j.connector.CalendarStore
import spock.lang.Specification

class GetCalendarCollectionCommandTest extends Specification {

    def 'test get collection'() {
        given: 'a mock calendar store'
        CalendarStore store = Mock()

        when: 'a get calendar collection command is run'
        new GetCalendarCollectionCommand(store).withCollectionName('testCollection').run()

        then: 'store get collection is invoked'
        1 * store.getCollection('testCollection')
    }
}

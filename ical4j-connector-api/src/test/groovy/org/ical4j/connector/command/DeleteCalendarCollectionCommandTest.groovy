package org.ical4j.connector.command

import org.ical4j.connector.CalendarStore
import spock.lang.Specification

class DeleteCalendarCollectionCommandTest extends Specification {

    def 'test delete collection'() {
        given: 'a mock calendar store'
        CalendarStore store = Mock()

        when: 'a delete calendar collection command is run'
        new DeleteCalendarCollectionCommand(store).withCollectionName('testCollection').run()

        then: 'store remove collection is invoked'
        1 * store.removeCollection('testCollection')
    }
}

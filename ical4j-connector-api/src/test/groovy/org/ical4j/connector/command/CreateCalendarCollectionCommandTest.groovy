package org.ical4j.connector.command

import org.ical4j.connector.CalendarStore
import spock.lang.Specification

class CreateCalendarCollectionCommandTest extends Specification {

    def 'test create collection'() {
        given: 'a mock calendar store'
        CalendarStore store = Mock()

        when: 'a create calendar collection command is run'
        new CreateCalendarCollectionCommand(store).withCollectionName('testCollection').run()

        then: 'store add collection is invoked'
        1 * store.addCollection('testCollection')
    }
}

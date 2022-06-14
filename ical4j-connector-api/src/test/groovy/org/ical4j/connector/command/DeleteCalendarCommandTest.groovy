package org.ical4j.connector.command


import org.ical4j.connector.CalendarCollection
import spock.lang.Specification

class DeleteCalendarCommandTest extends Specification {

    def 'test delete calendar'() {
        given: 'a mock calendar collection'
        CalendarCollection collection = Mock()

        when: 'a delete calendar command is run'
        new DeleteCalendarCommand(collection).withCalendarUid('1234').run()

        then: 'collection remove calendar is invoked'
        1 * collection.removeCalendar('1234')
    }
}

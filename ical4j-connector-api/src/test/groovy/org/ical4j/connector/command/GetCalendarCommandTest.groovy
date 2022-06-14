package org.ical4j.connector.command


import org.ical4j.connector.CalendarCollection
import spock.lang.Specification

class GetCalendarCommandTest extends Specification {

    def 'test get calendar'() {
        given: 'a mock calendar collection'
        CalendarCollection collection = Mock()

        when: 'a get calendar command is run'
        new GetCalendarCommand(collection).withCalendarUid('1234').run()

        then: 'collection get calendar is invoked'
        1 * collection.getCalendar('1234')
    }
}

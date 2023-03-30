package org.ical4j.connector.command

import org.ical4j.connector.CalendarCollection
import org.ical4j.connector.CalendarStore
import spock.lang.Specification

class GetCalendarTest extends Specification {

    def 'test get calendar'() {
        given: 'a mock calendar collection'
        CalendarStore store = Mock()
        CalendarCollection collection = Mock()

        when: 'a get calendar command is run'
        new CalendarCommand.GetCalendar('default', (calendar) -> {}, store).withCalendarUid('1234').run()

        then: 'collection get calendar is invoked'
        1 * store.getCollection('default') >> collection
        1 * collection.getCalendar('1234')
    }
}

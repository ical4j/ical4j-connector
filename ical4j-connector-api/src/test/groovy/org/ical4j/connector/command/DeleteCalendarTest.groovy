package org.ical4j.connector.command


import org.ical4j.connector.CalendarCollection
import org.ical4j.connector.CalendarStore
import spock.lang.Specification

class DeleteCalendarTest extends Specification {

    def 'test delete calendar'() {
        given: 'a mock calendar collection'
        CalendarStore store = Mock()
        CalendarCollection collection = Mock()
        store.getCollection(_) >> collection

        when: 'a delete calendar command is run'
        new CalendarCommand.DeleteCalendar('testCollection', store).withCalendarUid('1234').run()

        then: 'collection remove calendar is invoked'
        1 * collection.removeCalendar('1234')
    }
}

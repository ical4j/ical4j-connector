package org.ical4j.connector.command

import net.fortuna.ical4j.model.Calendar
import org.ical4j.connector.CalendarCollection
import org.ical4j.connector.CalendarStore
import spock.lang.Specification

class CreateCalendarTest extends Specification {

    def 'test create calendar'() {
        given: 'a mock calendar collection'
        CalendarStore store = Mock()
        CalendarCollection collection = Mock()
        store.getCollection(_) >> collection

        and: 'a calendar instance'
        Calendar calendar = []

        when: 'a create calendar command is run'
        new CreateCalendar('testCollection', store).withCalendar(calendar).run()

        then: 'collection add calendar is invoked'
        1 * collection.addCalendar(calendar)
    }
}

package org.ical4j.connector.command

import net.fortuna.ical4j.model.Calendar
import org.ical4j.connector.CalendarCollection
import org.ical4j.connector.CalendarStore
import spock.lang.Specification

class UpdateCalendarTest extends Specification {

    def 'test update calendar'() {
        given: 'a mock calendar collection'
        CalendarStore store = Mock()
        CalendarCollection collection = Mock()
        store.getCollection(_) >> collection

        and: 'a calendar instance'
        Calendar calendar = []

        when: 'a update calendar command is run'
        new CalendarCommand.UpdateCalendar('testCollection', store).withCalendar(calendar).run()

        then: 'collection merge is invoked'
        1 * collection.merge(calendar)
    }
}

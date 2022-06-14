package org.ical4j.connector.command

import net.fortuna.ical4j.model.Calendar
import org.ical4j.connector.CalendarCollection
import spock.lang.Specification

class CreateCalendarCommandTest extends Specification {

    def 'test create calendar'() {
        given: 'a mock calendar collection'
        CalendarCollection collection = Mock()

        and: 'a calendar instance'
        Calendar calendar = []

        when: 'a create calendar command is run'
        new CreateCalendarCommand(collection).withCalendar(calendar).run()

        then: 'collection add calendar is invoked'
        1 * collection.addCalendar(calendar)
    }
}

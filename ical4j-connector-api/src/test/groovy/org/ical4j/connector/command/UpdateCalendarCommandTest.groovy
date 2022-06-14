package org.ical4j.connector.command

import net.fortuna.ical4j.model.Calendar
import org.ical4j.connector.CalendarCollection
import spock.lang.Specification

class UpdateCalendarCommandTest extends Specification {

    def 'test update calendar'() {
        given: 'a mock calendar collection'
        CalendarCollection collection = Mock()

        and: 'a calendar instance'
        Calendar calendar = []

        when: 'a update calendar command is run'
        new UpdateCalendarCommand(collection).withCalendar(calendar).run()

        then: 'collection merge is invoked'
        1 * collection.merge(calendar)
    }
}

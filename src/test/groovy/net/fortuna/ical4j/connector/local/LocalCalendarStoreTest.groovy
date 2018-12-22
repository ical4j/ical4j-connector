package net.fortuna.ical4j.connector.local

import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.DefaultTimeZoneRegistryFactory
import net.fortuna.ical4j.util.Calendars
import spock.lang.Specification

class LocalCalendarStoreTest extends Specification {

    def 'test create collection'() {
        given: 'a new local calendar store'
        LocalCalendarStore calendarStore = [new File('build', 'local')]

        when: 'a new collection is added'
        LocalCalendarCollection collection = calendarStore.addCollection('public_holidays')

        then: 'a local collection directory is added'
        new File('build/local', 'public_holidays').exists()
    }

    def 'test create and initialise collection'() {
        given: 'a new local calendar store'
        LocalCalendarStore calendarStore = [new File('build', 'local')]

        and: 'a timezone calendar'
        Calendar timezone = Calendars.wrap(new DefaultTimeZoneRegistryFactory().createRegistry()
                .getTimeZone('Australia/Melbourne').getVTimeZone())

        when: 'a new collection is added'
        LocalCalendarCollection collection = calendarStore.addCollection('public_holidays', 'Public Holidays',
                'Victorian public holidays', [Component.VEVENT] as String[], timezone)

        then: 'a local collection directory is added'
        new File('build/local', 'public_holidays').exists()

        and: 'the collection properties are saved'
        collection.displayName == 'Public Holidays'
        collection.description == 'Victorian public holidays'
        collection.supportedComponentTypes == [Component.VEVENT] as String[]
        collection.timeZone == timezone

    }
}

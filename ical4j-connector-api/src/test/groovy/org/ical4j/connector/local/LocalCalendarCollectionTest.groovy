package org.ical4j.connector.local

import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.ContentBuilder
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.util.Calendars
import net.fortuna.ical4j.util.RandomUidGenerator
import org.ical4j.connector.event.ObjectCollectionEvent
import org.ical4j.connector.event.ObjectCollectionListener
import spock.lang.Shared

class LocalCalendarCollectionTest extends AbstractLocalTest {

    @Shared
    Calendar calendar

    def setupSpec() {
        calendar = new ContentBuilder().with {
            calendar {
                prodid '-//Ben Fortuna//iCal4j 1.0//EN'
                version '2.0'
                vevent {
                    uid new RandomUidGenerator().generateUid()
                    dtstamp()
                    dtstart('20090810', parameters: parameters { value 'DATE' })
                    action 'DISPLAY'
                    attach('http://example.com/attachment', parameters: parameters() { value 'URI' })
                }
            }
        }
    }

    def 'test add calendar to collection'() {
        given: 'a local calendar collection'
        LocalCalendarStore calendarStore = [storeLocation]
        LocalCalendarCollection collection = calendarStore.addCollection('public_holidays')

        and: 'a collection listener'
        ObjectCollectionEvent<Calendar> event
        ObjectCollectionListener<Calendar> listener = { event = it } as ObjectCollectionListener
        collection.addObjectCollectionListener(listener)

        when: 'the new calendar is added to the collection'
        collection.addCalendar(calendar)

        then: 'a new calendar file is created'
        new File(storeLocation,
                "public_holidays/${calendar.getComponent(Component.VEVENT).get().getRequiredProperty(Property.UID).getValue()}.ics").exists()

        and: 'the listener is notified'
        event != null && event.object == calendar
    }

    def 'test remove calendar from collection'() {
        given: 'a local calendar collection'
        LocalCalendarStore calendarStore = [storeLocation]
        LocalCalendarCollection collection = calendarStore.addCollection('public_holidays')

        and: 'a calendar object that is added to the collection'
        collection.addCalendar(calendar)

        when: 'the calendar is removed from the collection'
        def removed = collection.removeCalendar(Calendars.getUid(calendar).value)

        then: 'the exsiting calendar file is deleted'
        !new File(storeLocation,
                "public_holidays/${calendar.getComponent(Component.VEVENT).get().getRequiredProperty(Property.UID).getValue()}.ics").exists()

        and: 'removed calendar is identical to added'
        removed == calendar
    }

    def 'test get calendar from collection'() {
        given: 'a local calendar collection'
        LocalCalendarStore calendarStore = [storeLocation]
        LocalCalendarCollection collection = calendarStore.addCollection('public_holidays')

        and: 'a calendar object that is added to the collection'
        collection.addCalendar(calendar)

        when: 'the calendar is retrieved from the collection'
        def retrieved = collection.getCalendar(Calendars.getUid(calendar).value)

        then: 'retrieved calendar is identical to added'
        retrieved == calendar
    }

    def 'test list object uids in collection'() {
        given: 'a local calendar collection'
        LocalCalendarStore calendarStore = [storeLocation]
        LocalCalendarCollection collection = calendarStore.addCollection('public_holidays')

        and: 'a calendar object that is added to the collection'
        collection.addCalendar(calendar)

        when: 'the collection object uids are listed'
        def uids = collection.listObjectUids()

        then: 'the added calendar uid is in the list'
        uids.contains(Calendars.getUid(calendar).value)
    }

    def 'test export collection'() {
        given: 'a local calendar collection'
        LocalCalendarStore calendarStore = [storeLocation]
        LocalCalendarCollection collection = calendarStore.addCollection('public_holidays')

        and: 'a calendar object that is added to the collection'
        collection.addCalendar(calendar)

        when: 'the collection is exported'
        def export = collection.export()

        then: 'the exported collection is identical to added'
        export == calendar
    }
}

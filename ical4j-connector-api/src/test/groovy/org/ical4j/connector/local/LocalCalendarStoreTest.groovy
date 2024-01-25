package org.ical4j.connector.local

import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.DefaultTimeZoneRegistryFactory
import net.fortuna.ical4j.util.Calendars
import org.ical4j.connector.event.ObjectStoreEvent
import org.ical4j.connector.event.ObjectStoreListener

class LocalCalendarStoreTest extends AbstractLocalTest {

    def 'test create collection'() {
        given: 'a new local calendar store'
        LocalCalendarStore calendarStore = [storeLocation]

        and: 'a store listener'
        ObjectStoreEvent<Calendar> event
        ObjectStoreListener<Calendar> listener = { event = it } as ObjectStoreListener
        calendarStore.addObjectStoreListener(listener)

        when: 'a new collection is added'
        LocalCalendarCollection collection = calendarStore.addCollection('public_holidays')

        then: 'a local collection directory is added'
        new File(workspaceLocation, 'public_holidays').exists()

        and: 'the listener is notified'
        event != null && event.collection == collection
    }

    def 'test create and initialise collection'() {
        given: 'a new local calendar store'
        LocalCalendarStore calendarStore = [storeLocation]

        and: 'a timezone calendar'
        Calendar timezone = Calendars.wrap(new DefaultTimeZoneRegistryFactory().createRegistry()
                .getTimeZone('Australia/Melbourne').getVTimeZone())

        when: 'a new collection is added'
        LocalCalendarCollection collection = calendarStore.addCollection('public_holidays', 'Public Holidays',
                'Victorian public holidays', [Component.VEVENT] as String[], timezone)

        then: 'a local collection directory is added'
        new File(workspaceLocation, 'public_holidays').exists()

        and: 'the collection properties are saved'
        collection.displayName == 'Public Holidays'
        collection.description == 'Victorian public holidays'
        collection.supportedComponentTypes == [Component.VEVENT] as String[]
        collection.timeZone == timezone

    }

    def 'test get collection'() {
        given: 'a new local calendar store'
        LocalCalendarStore calendarStore = [storeLocation]

        and: 'a timezone calendar'
        Calendar timezone = Calendars.wrap(new DefaultTimeZoneRegistryFactory().createRegistry()
                .getTimeZone('Australia/Melbourne').getVTimeZone())

        and: 'a new collection is added'
        calendarStore.addCollection('public_holidays', 'Public Holidays',
                'Victorian public holidays', [Component.VEVENT] as String[], timezone)

        when: 'collection is retrieved'
        LocalCalendarCollection collection = calendarStore.getCollection('public_holidays')

        then: 'the collection properties are saved'
        collection.displayName == 'Public Holidays'
        collection.description == 'Victorian public holidays'
        collection.supportedComponentTypes == [Component.VEVENT] as String[]
        collection.timeZone == timezone

    }

    def 'test remove collection'() {
        given: 'a new local calendar store'
        LocalCalendarStore calendarStore = [storeLocation]

        when: 'a new collection is added'
        LocalCalendarCollection c = calendarStore.addCollection('public_holidays')

        and: 'the collection is removed'
        c.delete()

        then: 'a local collection directory is deleted'
        !new File(storeLocation, 'public_holidays').exists()
    }
}

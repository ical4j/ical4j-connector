package org.ical4j.connector.google

import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.CalendarListEntry
import org.ical4j.connector.ObjectStore
import org.ical4j.connector.ObjectStoreException
import spock.lang.Specification
import spock.mock.MockMakers

class GoogleCalendarStoreTest extends Specification {

    Calendar client = Mock(mockMaker: MockMakers.mockito)
    Calendar.CalendarList calendarListApi = Mock(mockMaker: MockMakers.mockito)
    Calendar.CalendarList.List calendarListReq = Mock(mockMaker: MockMakers.mockito)
    Calendar.Calendars calendarsApi = Mock(mockMaker: MockMakers.mockito)

    def 'listWorkspaceIds returns DEFAULT_WORKSPACE only'() {
        given:
        GoogleCalendarStore store = new GoogleCalendarStore(client)

        expect:
        store.listWorkspaceIds() == [ObjectStore.DEFAULT_WORKSPACE]
    }

    def 'non-default workspace raises ObjectStoreException'() {
        given:
        GoogleCalendarStore store = new GoogleCalendarStore(client)

        when:
        store.getCollections('not-default')

        then:
        thrown(ObjectStoreException)

        when:
        store.getCollection('id', 'not-default')

        then:
        thrown(ObjectStoreException)

        when:
        store.addCollection('name', 'not-default')

        then:
        thrown(ObjectStoreException)
    }

    def 'default workspace argument is accepted'() {
        given:
        GoogleCalendarStore store = new GoogleCalendarStore(client)
        CalendarListEntry entry = new CalendarListEntry().setId('cal-1')
        CalendarList page = new CalendarList().setItems([entry])

        when:
        def collections = store.getCollections(ObjectStore.DEFAULT_WORKSPACE)

        then:
        1 * client.calendarList() >> calendarListApi
        1 * calendarListApi.list() >> calendarListReq
        1 * calendarListReq.execute() >> page
        collections.size() == 1
    }

    def 'getCollections maps calendarList entries to GoogleCalendarCollection'() {
        given:
        GoogleCalendarStore store = new GoogleCalendarStore(client)
        CalendarList page = new CalendarList().setItems([
                new CalendarListEntry().setId('a'),
                new CalendarListEntry().setId('b'),
        ])

        when:
        def collections = store.getCollections()

        then:
        1 * client.calendarList() >> calendarListApi
        1 * calendarListApi.list() >> calendarListReq
        1 * calendarListReq.execute() >> page
        collections*.class == [GoogleCalendarCollection, GoogleCalendarCollection]
    }

    def 'addCollection(name) issues calendars().insert and wraps the result'() {
        given:
        GoogleCalendarStore store = new GoogleCalendarStore(client)
        Calendar.Calendars.Insert insertReq = Mock(mockMaker: MockMakers.mockito)
        com.google.api.services.calendar.model.Calendar inserted =
                new com.google.api.services.calendar.model.Calendar().setId('new-cal-id')

        when:
        def collection = store.addCollection('Team Events')

        then:
        1 * client.calendars() >> calendarsApi
        1 * calendarsApi.insert({ it.summary == 'Team Events' }) >> insertReq
        1 * insertReq.execute() >> inserted
        collection instanceof GoogleCalendarCollection
    }

    def 'removeCollection issues calendars().delete'() {
        given:
        GoogleCalendarStore store = new GoogleCalendarStore(client)
        Calendar.Calendars.Delete deleteReq = Mock(mockMaker: MockMakers.mockito)

        when:
        def result = store.removeCollection('cal-id')

        then:
        1 * client.calendars() >> calendarsApi
        1 * calendarsApi.delete('cal-id') >> deleteReq
        1 * deleteReq.execute() >> null
        result == null
    }
}

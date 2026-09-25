package org.ical4j.connector.google

import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.Events
import net.fortuna.ical4j.model.PropertyList
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.Summary
import net.fortuna.ical4j.model.property.Uid
import org.ical4j.connector.ObjectStoreException
import spock.lang.Specification
import spock.mock.MockMakers

class GoogleCalendarCollectionTest extends Specification {

    Calendar client = Mock(mockMaker: MockMakers.mockito)
    Calendar.Events eventsApi = Mock(mockMaker: MockMakers.mockito)
    Calendar.Calendars calendarsApi = Mock(mockMaker: MockMakers.mockito)
    GoogleCalendarStore store = new GoogleCalendarStore(client)

    def 'listObjectUIDs paginates across 3 pages and aggregates results'() {
        given:
        GoogleCalendarCollection collection = new GoogleCalendarCollection(store, 'cal-1')
        Calendar.Events.List listReq = Mock(mockMaker: MockMakers.mockito)
        Events page1 = new Events().setItems([new Event().setICalUID('a'), new Event().setICalUID('b')]).setNextPageToken('p2')
        Events page2 = new Events().setItems([new Event().setICalUID('c')]).setNextPageToken('p3')
        Events page3 = new Events().setItems([new Event().setICalUID('d')])

        when:
        def uids = collection.listObjectUIDs()

        then:
        3 * client.events() >> eventsApi
        3 * eventsApi.list('cal-1') >> listReq
        1 * listReq.setPageToken(null) >> listReq
        1 * listReq.setPageToken('p2') >> listReq
        1 * listReq.setPageToken('p3') >> listReq
        3 * listReq.execute() >>> [page1, page2, page3]
        uids == ['a', 'b', 'c', 'd']
    }

    def 'get(uid) returns the matching event'() {
        given:
        GoogleCalendarCollection collection = new GoogleCalendarCollection(store, 'cal-1')
        Calendar.Events.List listReq = Mock(mockMaker: MockMakers.mockito)
        Events page = new Events().setItems([
                new Event().setICalUID('a').setSummary('First'),
                new Event().setICalUID('b').setSummary('Second'),
        ])

        when:
        def result = collection.get('b')

        then:
        1 * client.events() >> eventsApi
        1 * eventsApi.list('cal-1') >> listReq
        1 * listReq.setPageToken(null) >> listReq
        1 * listReq.execute() >> page
        result.isPresent()
        ((VEvent) result.get().getComponent('VEVENT').get()).getProperty('SUMMARY').get().getValue() == 'Second'
    }

    def 'get(uid) returns empty for unknown uid'() {
        given:
        GoogleCalendarCollection collection = new GoogleCalendarCollection(store, 'cal-1')
        Calendar.Events.List listReq = Mock(mockMaker: MockMakers.mockito)
        Events page = new Events().setItems([new Event().setICalUID('a')])

        when:
        def result = collection.get('missing')

        then:
        1 * client.events() >> eventsApi
        1 * eventsApi.list('cal-1') >> listReq
        1 * listReq.setPageToken(null) >> listReq
        1 * listReq.execute() >> page
        !result.isPresent()
    }

    def 'add() with no VEVENT raises ObjectStoreException'() {
        given:
        GoogleCalendarCollection collection = new GoogleCalendarCollection(store, 'cal-1')
        net.fortuna.ical4j.model.Calendar empty = new net.fortuna.ical4j.model.Calendar()

        when:
        collection.add(empty)

        then:
        thrown(ObjectStoreException)
    }

    def 'add() inserts the converted event and returns iCalUID'() {
        given:
        GoogleCalendarCollection collection = new GoogleCalendarCollection(store, 'cal-1')
        VEvent vevent = new VEvent(new PropertyList())
        vevent.add(new Uid('event-uid@example.com'))
        vevent.add(new Summary('hello'))
        net.fortuna.ical4j.model.Calendar cal = new net.fortuna.ical4j.model.Calendar()
        cal.add(vevent)

        Calendar.Events.Insert insertReq = Mock(mockMaker: MockMakers.mockito)
        Event inserted = new Event().setICalUID('event-uid@example.com')

        when:
        String uid = collection.add(cal)

        then:
        1 * client.events() >> eventsApi
        1 * eventsApi.insert('cal-1', { it.iCalUID == 'event-uid@example.com' }) >> insertReq
        1 * insertReq.execute() >> inserted
        uid == 'event-uid@example.com'
    }

    def 'delete() issues calendars().delete'() {
        given:
        GoogleCalendarCollection collection = new GoogleCalendarCollection(store, 'cal-1')
        Calendar.Calendars.Delete deleteReq = Mock(mockMaker: MockMakers.mockito)

        when:
        collection.delete()

        then:
        1 * client.calendars() >> calendarsApi
        1 * calendarsApi.delete('cal-1') >> deleteReq
        1 * deleteReq.execute() >> null
    }
}

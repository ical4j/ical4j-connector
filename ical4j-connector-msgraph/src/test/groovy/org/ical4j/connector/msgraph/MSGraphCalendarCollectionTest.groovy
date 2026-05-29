package org.ical4j.connector.msgraph

import com.microsoft.graph.models.Event
import com.microsoft.graph.models.EventCollectionResponse
import com.microsoft.graph.serviceclient.GraphServiceClient
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import spock.lang.Specification

import java.util.function.Consumer

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.RETURNS_DEEP_STUBS
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

/**
 * Tests for {@link MSGraphCalendarCollection} against a mocked Microsoft Graph client.
 */
class MSGraphCalendarCollectionTest extends Specification {

    GraphServiceClient client = mock(GraphServiceClient, RETURNS_DEEP_STUBS)
    MSGraphCalendarStore store = new MSGraphCalendarStore(client)
    MSGraphCalendarCollection collection = new MSGraphCalendarCollection(store, 'cal')

    private def events() {
        client.me().calendars().byCalendarId('cal').events()
    }

    private static Event event(String uid, String id = null) {
        new Event(ICalUId: uid, id: id)
    }

    private static EventCollectionResponse page(List<Event> events, String next) {
        def response = new EventCollectionResponse()
        response.value = events
        response.odataNextLink = next
        response
    }

    private static Calendar calendar(String... uids) {
        def lines = ['BEGIN:VCALENDAR', 'VERSION:2.0', 'PRODID:-//test//EN']
        uids.each { lines += ['BEGIN:VEVENT', "UID:${it}", 'DTSTART:20260601T090000Z', 'END:VEVENT'] }
        lines += 'END:VCALENDAR'
        new CalendarBuilder().build(new StringReader(lines.join('\r\n') + '\r\n'))
    }

    def 'listObjectUIDs aggregates across paginated responses'() {
        given:
        when(events().get()).thenReturn(page([event('u1')], 'NEXT1'))
        when(events().withUrl('NEXT1').get()).thenReturn(page([event('u2')], 'NEXT2'))
        when(events().withUrl('NEXT2').get()).thenReturn(page([event('u3')], null))

        expect:
        collection.listObjectUIDs() == ['u1', 'u2', 'u3']
    }

    def 'get(uid) returns the server-filtered match'() {
        given:
        when(events().get(any(Consumer))).thenReturn(page([event('found@x')], null))

        when:
        def result = collection.get('found@x')

        then:
        result.isPresent()
        result.get().getComponent('VEVENT').get().getProperty('UID').get().value == 'found@x'
    }

    def 'get(uid) returns empty when no event matches'() {
        given:
        when(events().get(any(Consumer))).thenReturn(page([], null))

        expect:
        collection.get('missing').isEmpty()
    }

    def 'add returns the server-assigned iCalUId'() {
        given:
        when(events().post(any())).thenReturn(event('server-assigned@x'))

        expect:
        collection.add(calendar('submitted@x')) == 'server-assigned@x'
    }

    def 'removeAll deletes matching events, returns them, and skips unknown UIDs'() {
        given: 'the server-side filter is unsupported, exercising the paged-scan fallback'
        when(events().get(any(Consumer))).thenThrow(new RuntimeException('filter not supported'))
        when(events().get()).thenReturn(page([event('u1', 'eid1'), event('u2', 'eid2')], null))

        when:
        def removed = collection.removeAll('u1', 'missing')

        then:
        removed.size() == 1
        removed[0].getComponent('VEVENT').get().getProperty('UID').get().value == 'u1'
        verify(events().byEventId('eid1')).delete()
    }

    def 'export aggregates all events into a single calendar'() {
        given:
        when(events().get()).thenReturn(page([event('u1'), event('u2')], null))

        expect:
        collection.export().getComponents().size() == 2
    }

    def 'merge adds one object per UID'() {
        given:
        when(events().post(any())).thenReturn(event('assigned@x'))

        when:
        def uids = collection.merge(calendar('u1', 'u2'))
        verify(events(), times(2)).post(any())

        then:
        uids.collect { it.value } as Set == ['u1', 'u2'] as Set
    }

    def 'listObjectUIDs works through the calendar-group path'() {
        given:
        def grouped = new MSGraphCalendarCollection(store, 'cal', 'grp')
        def groupEvents = client.me().calendarGroups().byCalendarGroupId('grp').calendars().byCalendarId('cal').events()
        when(groupEvents.get()).thenReturn(page([event('g1')], null))

        expect:
        grouped.listObjectUIDs() == ['g1']
    }
}

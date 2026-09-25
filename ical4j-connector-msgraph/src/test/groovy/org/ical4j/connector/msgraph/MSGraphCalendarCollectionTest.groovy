package org.ical4j.connector.msgraph

import com.microsoft.graph.models.Event
import com.microsoft.graph.models.EventCollectionResponse
import com.microsoft.graph.serviceclient.GraphServiceClient
import com.microsoft.graph.users.item.calendars.item.events.EventsRequestBuilder
import com.microsoft.kiota.ApiException
import com.microsoft.kiota.RequestAdapter
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import spock.lang.Specification

import java.util.function.Consumer

import org.ical4j.connector.ObjectStoreException
import org.mockito.ArgumentCaptor

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

    private static ApiException apiError(int status) {
        def error = new ApiException("HTTP ${status}")
        error.setResponseStatusCode(status)
        error
    }

    /**
     * Applies the request configuration passed to the filtered events query and returns the resulting filter.
     */
    private String capturedFilter() {
        def captor = ArgumentCaptor.forClass(Consumer)
        verify(events()).get(captor.capture())
        def config = new EventsRequestBuilder.GetRequestConfiguration(
                new EventsRequestBuilder('https://graph.example', mock(RequestAdapter)))
        captor.value.accept(config)
        config.queryParameters.filter
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
        when(events().get(any(Consumer))).thenThrow(apiError(400))
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

    def 'merge adds one object per UID and returns the server-assigned UIDs'() {
        given:
        when(events().post(any())).thenReturn(event('assigned1@x'), event('assigned2@x'))

        when:
        def uids = collection.merge(calendar('u1', 'u2'))
        verify(events(), times(2)).post(any())

        then:
        uids.collect { it.value } as Set == ['assigned1@x', 'assigned2@x'] as Set
    }

    def 'get(uid) escapes single quotes in the iCalUId filter'() {
        given:
        when(events().get(any(Consumer))).thenReturn(page([], null))

        when:
        collection.get("x' or subject eq 'Board meeting")

        then:
        capturedFilter() == "iCalUId eq 'x'' or subject eq ''Board meeting'"
    }

    def 'get(uid) ignores filtered results that are not an exact UID match'() {
        given:
        when(events().get(any(Consumer))).thenReturn(page([event('other@x'), event('found@x')], null))

        when:
        def result = collection.get('found@x')

        then:
        result.get().getComponent('VEVENT').get().getProperty('UID').get().value == 'found@x'
    }

    def 'removeAll propagates service errors other than a rejected filter'() {
        given:
        when(events().get(any(Consumer))).thenThrow(apiError(401))

        when:
        collection.removeAll('u1')

        then:
        def e = thrown(ApiException)
        e.responseStatusCode == 401
    }

    def 'listObjectUIDs works through the calendar-group path'() {
        given:
        def grouped = new MSGraphCalendarCollection(store, 'cal', 'grp')
        def groupEvents = client.me().calendarGroups().byCalendarGroupId('grp').calendars().byCalendarId('cal').events()
        when(groupEvents.get()).thenReturn(page([event('g1')], null))

        expect:
        grouped.listObjectUIDs() == ['g1']
    }

    private static Calendar parse(String... lines) {
        def all = ['BEGIN:VCALENDAR', 'VERSION:2.0', 'PRODID:-//test//EN'] + (lines as List) + ['END:VCALENDAR']
        new CalendarBuilder().build(new StringReader(all.join('\r\n') + '\r\n'))
    }

    def 'add posts the series VEVENT when a recurrence override comes first'() {
        given:
        def captor = ArgumentCaptor.forClass(Event)
        when(events().post(any())).thenReturn(event('assigned@x'))
        def calendar = parse(
                'BEGIN:VEVENT', 'UID:s@x', 'SUMMARY:Override', 'RECURRENCE-ID:20260608T090000Z',
                'DTSTART:20260608T100000Z', 'END:VEVENT',
                'BEGIN:VEVENT', 'UID:s@x', 'SUMMARY:Master', 'DTSTART:20260601T090000Z',
                'RRULE:FREQ=WEEKLY', 'END:VEVENT')

        when:
        collection.add(calendar)
        verify(events()).post(captor.capture())

        then:
        captor.value.subject == 'Master'
    }

    def 'add rejects a calendar containing only recurrence overrides'() {
        given:
        def calendar = parse('BEGIN:VEVENT', 'UID:s@x', 'RECURRENCE-ID:20260608T090000Z',
                'DTSTART:20260608T100000Z', 'END:VEVENT')

        when:
        collection.add(calendar)

        then:
        thrown(ObjectStoreException)
    }

    def 'merge skips objects without a VEVENT instead of failing part-way'() {
        given:
        when(events().post(any())).thenReturn(event('assigned@x'))
        def calendar = parse(
                'BEGIN:VTODO', 'UID:t@x', 'SUMMARY:Task', 'END:VTODO',
                'BEGIN:VEVENT', 'UID:e@x', 'DTSTART:20260601T090000Z', 'END:VEVENT')

        when:
        def uids = collection.merge(calendar)
        verify(events(), times(1)).post(any())

        then:
        uids.collect { it.value } == ['assigned@x']
    }
}

package org.ical4j.connector.msgraph

import com.microsoft.graph.models.Calendar
import com.microsoft.graph.models.CalendarCollectionResponse
import com.microsoft.graph.models.CalendarGroup
import com.microsoft.graph.models.CalendarGroupCollectionResponse
import com.microsoft.graph.serviceclient.GraphServiceClient
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.RETURNS_DEEP_STUBS
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

/**
 * Tests for {@link MSGraphCalendarStore} against a mocked Microsoft Graph client.
 */
class MSGraphCalendarStoreTest extends Specification {

    GraphServiceClient client = mock(GraphServiceClient, RETURNS_DEEP_STUBS)
    MSGraphCalendarStore store = new MSGraphCalendarStore(client)

    def 'addCollection with description and timezone posts a calendar with name only'() {
        given:
        def captor = org.mockito.ArgumentCaptor.forClass(Calendar)
        when(client.me().calendars().post(any())).thenReturn(new Calendar(id: 'new-id'))

        when:
        def collection = store.addCollection('ignored-id', 'My Calendar', 'a description',
                ['VEVENT'] as String[], null)
        verify(client.me().calendars()).post(captor.capture())

        then:
        collection instanceof MSGraphCalendarCollection
        captor.value.name == 'My Calendar'
    }

    def 'addCollection with workspace targets the calendar group'() {
        given:
        def captor = org.mockito.ArgumentCaptor.forClass(Calendar)
        when(client.me().calendarGroups().byCalendarGroupId('grp').calendars().post(any()))
                .thenReturn(new Calendar(id: 'new-id'))

        when:
        store.addCollection('id', 'My Calendar', 'desc', null, null, 'grp')
        verify(client.me().calendarGroups().byCalendarGroupId('grp').calendars()).post(captor.capture())

        then:
        captor.value.name == 'My Calendar'
    }

    def 'getCollections maps each calendar to a collection'() {
        given:
        def response = new CalendarCollectionResponse(value: [new Calendar(id: 'c1'), new Calendar(id: 'c2')])
        when(client.me().calendars().get()).thenReturn(response)

        expect:
        store.getCollections().size() == 2
    }

    def 'listWorkspaceIds returns calendar group ids'() {
        given:
        def response = new CalendarGroupCollectionResponse(
                value: [new CalendarGroup(id: 'g1'), new CalendarGroup(id: 'g2')])
        when(client.me().calendarGroups().get()).thenReturn(response)

        expect:
        store.listWorkspaceIds() == ['g1', 'g2']
    }
}

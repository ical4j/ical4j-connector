package org.ical4j.connector.msgraph

import com.microsoft.graph.models.Attendee
import com.microsoft.graph.models.BodyType
import com.microsoft.graph.models.DateTimeTimeZone
import com.microsoft.graph.models.EmailAddress
import com.microsoft.graph.models.Event
import com.microsoft.graph.models.ItemBody
import com.microsoft.graph.models.Recipient
import com.microsoft.graph.models.ResponseStatus
import com.microsoft.graph.models.ResponseType
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import spock.lang.Specification

import java.time.OffsetDateTime

/**
 * Unit tests for the read-path mapping (Microsoft Graph {@link Event} -> iCal4j {@link Calendar}).
 */
class ICalCalendarBuilderTest extends Specification {

    private static VEvent convert(Event event) {
        Calendar calendar = new ICalCalendarBuilder().build(event)
        calendar.getComponent('VEVENT').get() as VEvent
    }

    private static DateTimeTimeZone dtz(String dateTime, String timeZone) {
        def d = new DateTimeTimeZone()
        d.dateTime = dateTime
        d.timeZone = timeZone
        d
    }

    def 'emits UID from iCalUId'() {
        given:
        def event = new Event(ICalUId: 'abc@outlook.com', subject: 'Standup')

        expect:
        convert(event).getProperty('UID').get().value == 'abc@outlook.com'
    }

    def 'generates a UID when iCalUId is absent'() {
        given:
        def event = new Event(subject: 'Standup')

        when:
        def uid = convert(event).getProperty('UID').get().value

        then:
        uid != null && !uid.isEmpty()
    }

    def 'maps subject, plain-text body and location'() {
        given:
        def event = new Event(ICalUId: 'a@x', subject: 'Standup',
                body: new ItemBody(contentType: BodyType.Text, content: 'Bring your laptop'),
                location: new com.microsoft.graph.models.Location(displayName: 'Room 1'))

        when:
        def ve = convert(event)

        then:
        ve.getProperty('SUMMARY').get().value == 'Standup'
        ve.getProperty('DESCRIPTION').get().value == 'Bring your laptop'
        ve.getProperty('LOCATION').get().value == 'Room 1'
    }

    def 'converts an HTML body to plain text'() {
        given:
        def event = new Event(ICalUId: 'a@x',
                body: new ItemBody(contentType: BodyType.Html, content: '<p>Bring your <b>laptop</b></p>'))

        expect:
        convert(event).getProperty('DESCRIPTION').get().value == 'Bring your laptop'
    }

    def 'maps a Windows time-zone name to an IANA TZID'() {
        given:
        def event = new Event(ICalUId: 'a@x',
                start: dtz('2026-06-01T09:30:00.0000000', 'AUS Eastern Standard Time'))

        when:
        def dtStart = convert(event).getProperty('DTSTART').get()

        then:
        dtStart.getParameter('TZID').get().value == 'Australia/Sydney'
        dtStart.value == '20260601T093000'
    }

    def 'maps an all-day event to a DATE value'() {
        given:
        def event = new Event(ICalUId: 'a@x', isAllDay: true,
                start: dtz('2026-06-01T00:00:00.0000000', 'UTC'))

        when:
        def dtStart = convert(event).getProperty('DTSTART').get()

        then:
        dtStart.getParameter('VALUE').get().value == 'DATE'
        dtStart.value == '20260601'
    }

    def 'maps a UTC time to a Z-suffixed value with no TZID'() {
        given:
        def event = new Event(ICalUId: 'a@x', start: dtz('2026-06-01T09:30:00.0000000', 'UTC'))

        when:
        def dtStart = convert(event).getProperty('DTSTART').get()

        then:
        dtStart.value == '20260601T093000Z'
        dtStart.getParameter('TZID').isEmpty()
    }

    def 'maps organizer and attendee with PARTSTAT'() {
        given:
        def organizer = new Recipient(emailAddress: new EmailAddress(address: 'alice@example.com', name: 'Alice'))
        def attendee = new Attendee(emailAddress: new EmailAddress(address: 'bob@example.com', name: 'Bob'),
                status: new ResponseStatus(response: ResponseType.Accepted))
        def event = new Event(ICalUId: 'a@x', organizer: organizer, attendees: [attendee])

        when:
        def ve = convert(event)

        then:
        ve.getProperty('ORGANIZER').get().value == 'mailto:alice@example.com'
        ve.getProperty('ORGANIZER').get().getParameter('CN').get().value == 'Alice'
        def att = ve.getProperty('ATTENDEE').get()
        att.value == 'mailto:bob@example.com'
        att.getParameter('PARTSTAT').get().value == 'ACCEPTED'
    }

    def 'maps all four responseType values to PARTSTAT'() {
        given:
        def event = new Event(ICalUId: 'a@x', attendees: [new Attendee(
                emailAddress: new EmailAddress(address: 'bob@example.com'),
                status: new ResponseStatus(response: response))])

        expect:
        convert(event).getProperty('ATTENDEE').get().getParameter('PARTSTAT').get().value == expected

        where:
        response                        || expected
        ResponseType.Accepted           || 'ACCEPTED'
        ResponseType.Declined           || 'DECLINED'
        ResponseType.TentativelyAccepted || 'TENTATIVE'
        ResponseType.NotResponded       || 'NEEDS-ACTION'
    }

    def 'maps created and last-modified timestamps'() {
        given:
        def event = new Event(ICalUId: 'a@x',
                createdDateTime: OffsetDateTime.parse('2026-01-02T03:04:05Z'),
                lastModifiedDateTime: OffsetDateTime.parse('2026-01-03T04:05:06Z'))

        when:
        def ve = convert(event)

        then:
        ve.getProperty('CREATED').get().value == '20260102T030405Z'
        ve.getProperty('LAST-MODIFIED').get().value == '20260103T040506Z'
    }
}

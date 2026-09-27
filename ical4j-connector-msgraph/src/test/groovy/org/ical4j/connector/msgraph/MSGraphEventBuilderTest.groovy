package org.ical4j.connector.msgraph

import com.microsoft.graph.models.BodyType
import com.microsoft.graph.models.DayOfWeek
import com.microsoft.graph.models.Event
import com.microsoft.graph.models.RecurrencePatternType
import com.microsoft.graph.models.RecurrenceRangeType
import com.microsoft.graph.models.ResponseType
import com.microsoft.graph.models.WeekIndex
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.component.VEvent
import spock.lang.Specification

import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * Unit tests for the write-path mapping (iCal4j {@link VEvent} -> Microsoft Graph {@link Event}).
 */
class MSGraphEventBuilderTest extends Specification {

    private static VEvent vevent(String... lines) {
        def all = ['BEGIN:VCALENDAR', 'VERSION:2.0', 'PRODID:-//test//EN', 'BEGIN:VEVENT'] +
                (lines as List) + ['END:VEVENT', 'END:VCALENDAR']
        def ical = all.join('\r\n') + '\r\n'
        new CalendarBuilder().build(new StringReader(ical)).getComponent('VEVENT').get() as VEvent
    }

    def 'maps summary, plain-text body and location; leaves iCalUId unset'() {
        when:
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'SUMMARY:Standup', 'DESCRIPTION:Bring your laptop',
                'LOCATION:Room 1', 'DTSTART:20260601T093000Z', 'DTEND:20260601T100000Z')).build()

        then:
        event.subject == 'Standup'
        event.body.contentType == BodyType.Text
        event.body.content == 'Bring your laptop'
        event.location.displayName == 'Room 1'
        event.getICalUId() == null
    }

    def 'maps all-day event to isAllDay with date-only start/end'() {
        when:
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'SUMMARY:Holiday',
                'DTSTART;VALUE=DATE:20260601', 'DTEND;VALUE=DATE:20260602')).build()

        then:
        event.isAllDay
        event.start.dateTime == '2026-06-01T00:00:00'
        event.start.timeZone == 'UTC'
        event.end.dateTime == '2026-06-02T00:00:00'
    }

    def 'maps timed event with TZID, preserving the zone'() {
        when:
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'SUMMARY:Meeting',
                'DTSTART;TZID=Australia/Melbourne:20260601T093000',
                'DTEND;TZID=Australia/Melbourne:20260601T100000')).build()

        then:
        !event.isAllDay
        event.start.dateTime == '2026-06-01T09:30:00'
        event.start.timeZone == 'Australia/Melbourne'
    }

    def 'maps timed event without TZID to UTC'() {
        when:
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'SUMMARY:Meeting',
                'DTSTART:20260601T093000Z', 'DTEND:20260601T100000Z')).build()

        then:
        event.start.timeZone == 'UTC'
        event.start.dateTime == '2026-06-01T09:30:00'
    }

    def 'maps organizer and attendees'() {
        when:
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'SUMMARY:Meeting', 'DTSTART:20260601T093000Z',
                'ORGANIZER;CN=Alice:mailto:alice@example.com',
                'ATTENDEE;CN=Bob;PARTSTAT=ACCEPTED:mailto:bob@example.com')).build()

        then:
        event.organizer.emailAddress.address == 'alice@example.com'
        event.organizer.emailAddress.name == 'Alice'
        event.attendees.size() == 1
        event.attendees[0].emailAddress.address == 'bob@example.com'
        event.attendees[0].emailAddress.name == 'Bob'
        event.attendees[0].status.response == ResponseType.Accepted
    }

    def 'maps all four PARTSTAT values to responseType'() {
        expect:
        new MSGraphEventBuilder().vevent(vevent('UID:a@example.com', 'DTSTART:20260601T093000Z',
                "ATTENDEE;PARTSTAT=${partStat}:mailto:bob@example.com")).build()
                .attendees[0].status.response == expected

        where:
        partStat       || expected
        'ACCEPTED'     || ResponseType.Accepted
        'DECLINED'     || ResponseType.Declined
        'TENTATIVE'    || ResponseType.TentativelyAccepted
        'NEEDS-ACTION' || ResponseType.NotResponded
    }

    def 'maps a simple weekly RRULE to a PatternedRecurrence'() {
        when:
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'SUMMARY:Weekly', 'DTSTART:20260601T093000Z',
                'RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO')).build()

        then:
        event.recurrence.pattern.type == RecurrencePatternType.Weekly
        event.recurrence.pattern.interval == 1
        event.recurrence.pattern.daysOfWeek == [DayOfWeek.Monday]
        event.recurrence.range.startDate.toString() == '2026-06-01'
    }

    def 'maps RRULE COUNT to a numbered range'() {
        when:
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'DTSTART:20260601T093000Z', 'RRULE:FREQ=DAILY;COUNT=10')).build()

        then:
        event.recurrence.pattern.type == RecurrencePatternType.Daily
        event.recurrence.range.type == RecurrenceRangeType.Numbered
        event.recurrence.range.numberOfOccurrences == 10
    }

    def 'maps RRULE UNTIL to an end-date range'() {
        when:
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'DTSTART:20260601T093000Z',
                'RRULE:FREQ=MONTHLY;UNTIL=20261201T000000Z')).build()

        then:
        event.recurrence.range.type == RecurrenceRangeType.EndDate
        event.recurrence.range.endDate.toString() == '2026-12-01'
    }

    def 'maps a weekly RRULE without BYDAY to the DTSTART weekday'() {
        when:
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'DTSTART:20260603T093000Z', 'RRULE:FREQ=WEEKLY')).build()

        then:
        event.recurrence.pattern.type == RecurrencePatternType.Weekly
        event.recurrence.pattern.daysOfWeek == [DayOfWeek.Wednesday]
    }

    def 'maps a yearly RRULE BYMONTH rather than the DTSTART month'() {
        when:
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'DTSTART:20260115T090000Z', 'RRULE:FREQ=YEARLY;BYMONTH=3;BYMONTHDAY=10')).build()

        then:
        event.recurrence.pattern.type == RecurrencePatternType.AbsoluteYearly
        event.recurrence.pattern.month == 3
        event.recurrence.pattern.dayOfMonth == 10
    }

    def 'maps BYSETPOS with BYDAY to a relative monthly pattern'() {
        when:
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'DTSTART:20260601T090000Z',
                'RRULE:FREQ=MONTHLY;BYDAY=MO,TU,WE,TH,FR;BYSETPOS=1')).build()

        then:
        event.recurrence.pattern.type == RecurrencePatternType.RelativeMonthly
        event.recurrence.pattern.index == WeekIndex.First
        event.recurrence.pattern.daysOfWeek.size() == 5
    }

    def 'omits recurrence for RRULEs Graph cannot express'() {
        when:
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'DTSTART:20260601T090000Z', "RRULE:${rrule}")).build()

        then:
        event.recurrence == null

        where:
        rrule << ['FREQ=MONTHLY;BYDAY=-2FR', 'FREQ=MONTHLY;BYDAY=MO', 'FREQ=MONTHLY;BYMONTHDAY=1,15',
                  'FREQ=MONTHLY;BYMONTHDAY=-1', 'FREQ=HOURLY']
    }

    def 'maps RRULE UNTIL to an end date in the series time zone'() {
        when: 'a Sydney 09:00 daily series ends at 2026-06-01T23:00Z, i.e. 09:00 on 2 June in Sydney'
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'DTSTART;TZID=Australia/Sydney:20260601T090000',
                'RRULE:FREQ=DAILY;UNTIL=20260601T230000Z')).build()

        then: 'the 2 June occurrence is kept'
        event.recurrence.range.endDate.toString() == '2026-06-02'
    }

    def 'converts an offset date-time to UTC rather than relabelling it'() {
        given:
        def vevent = new VEvent(OffsetDateTime.parse('2026-06-01T09:30:00+10:00'),
                OffsetDateTime.parse('2026-06-01T10:00:00+10:00'), 'Meeting')

        when:
        Event event = new MSGraphEventBuilder().vevent(vevent).build()

        then:
        event.start.dateTime == '2026-05-31T23:30:00'
        event.start.timeZone == 'UTC'
    }

    def 'maps a floating time in the configured floating time zone'() {
        when:
        Event event = new MSGraphEventBuilder().floatingTimeZone(ZoneId.of('Europe/London')).vevent(vevent(
                'UID:a@example.com', 'DTSTART:20260601T093000', 'DTEND:20260601T100000')).build()

        then:
        event.start.dateTime == '2026-06-01T09:30:00'
        event.start.timeZone == 'Europe/London'
    }

    def 'converts a time in a TZID Graph cannot resolve to UTC'() {
        given: 'a custom VTIMEZONE at +05:30 that is neither an IANA nor a Windows zone name'
        def ical = ['BEGIN:VCALENDAR', 'VERSION:2.0', 'PRODID:-//test//EN',
                    'BEGIN:VTIMEZONE', 'TZID:Custom Zone',
                    'BEGIN:STANDARD', 'DTSTART:19700101T000000', 'TZOFFSETFROM:+0530', 'TZOFFSETTO:+0530',
                    'END:STANDARD', 'END:VTIMEZONE',
                    'BEGIN:VEVENT', 'UID:a@example.com', 'DTSTAMP:20260101T000000Z',
                    'DTSTART;TZID=Custom Zone:20260601T093000', 'END:VEVENT', 'END:VCALENDAR'].join('\r\n') + '\r\n'
        def vevent = new CalendarBuilder().build(new StringReader(ical)).getComponent('VEVENT').get() as VEvent

        when:
        Event event = new MSGraphEventBuilder().vevent(vevent).build()

        then:
        event.start.dateTime == '2026-06-01T04:00:00'
        event.start.timeZone == 'UTC'
    }

    def 'drops unmapped properties silently'() {
        when:
        Event event = new MSGraphEventBuilder().vevent(vevent(
                'UID:a@example.com', 'SUMMARY:Meeting', 'DTSTART:20260601T093000Z',
                'URL:https://example.com', 'CLASS:PRIVATE', 'CATEGORIES:WORK,PERSONAL')).build()

        then:
        noExceptionThrown()
        event.subject == 'Meeting'
    }
}

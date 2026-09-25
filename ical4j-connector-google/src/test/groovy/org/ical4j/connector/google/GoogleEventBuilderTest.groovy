package org.ical4j.connector.google

import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventAttendee
import net.fortuna.ical4j.model.ParameterList
import net.fortuna.ical4j.model.PropertyList
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.parameter.Cn
import net.fortuna.ical4j.model.parameter.PartStat
import net.fortuna.ical4j.model.property.Attendee
import net.fortuna.ical4j.model.property.Categories
import net.fortuna.ical4j.model.property.Clazz
import net.fortuna.ical4j.model.property.Created
import net.fortuna.ical4j.model.property.Description
import net.fortuna.ical4j.model.property.DtEnd
import net.fortuna.ical4j.model.property.DtStart
import net.fortuna.ical4j.model.property.LastModified
import net.fortuna.ical4j.model.property.Location
import net.fortuna.ical4j.model.property.Organizer
import net.fortuna.ical4j.model.property.RRule
import net.fortuna.ical4j.model.property.Summary
import net.fortuna.ical4j.model.property.Uid
import net.fortuna.ical4j.model.property.Url
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class GoogleEventBuilderTest extends Specification {

    def 'all-day event round-trip'() {
        given: 'an all-day VEVENT'
        VEvent source = new VEvent(new PropertyList())
        source.add(new Uid('all-day@example.com'))
        source.add(new Summary('All-day'))
        source.add(new DtStart<>(LocalDate.of(2026, 6, 1)))
        source.add(new DtEnd<>(LocalDate.of(2026, 6, 2)))

        when: 'converted to Google Event and back'
        Event ge = new GoogleEventBuilder().vevent(source).build()
        net.fortuna.ical4j.model.Calendar roundTrip = new ICalCalendarBuilder().build(ge)
        VEvent vevent = (VEvent) roundTrip.getComponent('VEVENT').get()

        then: 'all-day fields are preserved'
        ge.getStart().getDate() != null
        ge.getStart().getDateTime() == null
        ge.getEnd().getDate() != null

        and: 'round-tripped VEVENT has LocalDate dtstart/dtend'
        ((DtStart) vevent.getProperty('DTSTART').get()).getDate() == LocalDate.of(2026, 6, 1)
        ((DtEnd) vevent.getProperty('DTEND').get()).getDate() == LocalDate.of(2026, 6, 2)
    }

    def 'timed event with TZID round-trip'() {
        given: 'a timed VEVENT in Australia/Melbourne'
        ZoneId melbourne = ZoneId.of('Australia/Melbourne')
        ZonedDateTime start = ZonedDateTime.of(2026, 6, 1, 9, 30, 0, 0, melbourne)
        VEvent source = new VEvent(new PropertyList())
        source.add(new Uid('timed@example.com'))
        source.add(new Summary('Timed'))
        source.add(new DtStart<>(start))

        when: 'converted to Google Event'
        Event ge = new GoogleEventBuilder().vevent(source).build()

        then: 'Google Event has dateTime and timeZone set'
        ge.getStart().getDateTime() != null
        ge.getStart().getDate() == null
        ge.getStart().getTimeZone() == 'Australia/Melbourne'

        when: 'converted back to VEVENT'
        net.fortuna.ical4j.model.Calendar roundTrip = new ICalCalendarBuilder().build(ge)
        VEvent vevent = (VEvent) roundTrip.getComponent('VEVENT').get()
        ZonedDateTime roundTripStart = (ZonedDateTime) ((DtStart) vevent.getProperty('DTSTART').get()).getDate()

        then: 'instant and zone match'
        roundTripStart.toInstant() == start.toInstant()
        roundTripStart.getZone() == melbourne
    }

    def 'timed event without TZID defaults to UTC'() {
        given: 'a VEVENT with UTC Instant'
        Instant start = Instant.parse('2026-06-01T09:30:00Z')
        VEvent source = new VEvent(new PropertyList())
        source.add(new Uid('utc@example.com'))
        source.add(new DtStart<>(start))

        when: 'converted to Google Event'
        Event ge = new GoogleEventBuilder().vevent(source).build()

        then: 'Google Event timeZone is UTC'
        ge.getStart().getTimeZone() == 'UTC'
        ge.getStart().getDateTime() != null
    }

    @Unroll
    def 'PARTSTAT #partStat maps to responseStatus #responseStatus'() {
        given: 'a VEVENT with one ATTENDEE having the PARTSTAT'
        VEvent source = new VEvent(new PropertyList())
        source.add(new Uid('attendees@example.com'))
        Attendee attendee = new Attendee(new ParameterList([new PartStat(partStat)] as List), URI.create('mailto:a@example.com'))
        source.add(attendee)

        when: 'converted to Google Event'
        Event ge = new GoogleEventBuilder().vevent(source).build()

        then: 'attendee responseStatus matches the mapping'
        ge.getAttendees().size() == 1
        ge.getAttendees().get(0).getResponseStatus() == responseStatus

        when: 'converted back to VEVENT'
        net.fortuna.ical4j.model.Calendar roundTrip = new ICalCalendarBuilder().build(ge)
        VEvent vevent = (VEvent) roundTrip.getComponent('VEVENT').get()
        Attendee roundTripped = (Attendee) vevent.getProperty('ATTENDEE').get()

        then: 'reverse mapping yields the original PARTSTAT'
        ((PartStat) roundTripped.getParameter('PARTSTAT').get()).getValue() == partStat

        where:
        partStat       | responseStatus
        'NEEDS-ACTION' | 'needsAction'
        'ACCEPTED'     | 'accepted'
        'DECLINED'     | 'declined'
        'TENTATIVE'    | 'tentative'
    }

    def 'RRULE round-trip'() {
        given: 'a VEVENT with an RRULE'
        VEvent source = new VEvent(new PropertyList())
        source.add(new Uid('recurring@example.com'))
        source.add(new RRule<>('FREQ=WEEKLY;BYDAY=MO'))

        when: 'converted to Google Event'
        Event ge = new GoogleEventBuilder().vevent(source).build()

        then: 'recurrence contains the raw RRULE line'
        ge.getRecurrence() == ['RRULE:FREQ=WEEKLY;BYDAY=MO']

        when: 'converted back to VEVENT'
        net.fortuna.ical4j.model.Calendar roundTrip = new ICalCalendarBuilder().build(ge)
        VEvent vevent = (VEvent) roundTrip.getComponent('VEVENT').get()

        then: 'RRULE is reconstructed'
        ((RRule) vevent.getProperty('RRULE').get()).getValue() == 'FREQ=WEEKLY;BYDAY=MO'
    }

    def 'iCalUID preserved'() {
        given: 'a VEVENT with UID'
        VEvent source = new VEvent(new PropertyList())
        source.add(new Uid('some-uid@example.com'))

        when: 'converted to Google Event'
        Event ge = new GoogleEventBuilder().vevent(source).build()

        then: 'iCalUID matches'
        ge.getICalUID() == 'some-uid@example.com'

        when: 'converted back to VEVENT'
        net.fortuna.ical4j.model.Calendar roundTrip = new ICalCalendarBuilder().build(ge)
        VEvent vevent = (VEvent) roundTrip.getComponent('VEVENT').get()

        then: 'UID is preserved'
        ((Uid) vevent.getProperty('UID').get()).getValue() == 'some-uid@example.com'
    }

    def 'unmapped properties are dropped silently'() {
        given: 'a VEVENT with URL, CLASS, and CATEGORIES'
        VEvent source = new VEvent(new PropertyList())
        source.add(new Uid('extras@example.com'))
        source.add(new Url(URI.create('https://example.com/event')))
        source.add(new Clazz('PRIVATE'))
        source.add(new Categories('MEETING'))

        when: 'converted to Google Event'
        Event ge = new GoogleEventBuilder().vevent(source).build()

        then: 'unmapped fields do not appear on the Event'
        noExceptionThrown()
        ge.getICalUID() == 'extras@example.com'
        ge.get('url') == null
        ge.get('class') == null
        ge.get('categories') == null
    }

    def 'organizer and attendee email + displayName'() {
        given: 'a VEVENT with an organizer and attendee carrying CN'
        VEvent source = new VEvent(new PropertyList())
        source.add(new Uid('org@example.com'))
        ParameterList orgParams = new ParameterList([new Cn('Alice')] as List)
        source.add(new Organizer(orgParams, URI.create('mailto:alice@example.com')))
        ParameterList attParams = new ParameterList([new Cn('Bob'), new PartStat('ACCEPTED')] as List)
        source.add(new Attendee(attParams, URI.create('mailto:bob@example.com')))

        when: 'converted to Google Event'
        Event ge = new GoogleEventBuilder().vevent(source).build()

        then: 'organizer email and displayName mapped'
        ge.getOrganizer().getEmail() == 'alice@example.com'
        ge.getOrganizer().getDisplayName() == 'Alice'

        and: 'attendee email and displayName mapped'
        ge.getAttendees().size() == 1
        EventAttendee bob = ge.getAttendees().get(0)
        bob.getEmail() == 'bob@example.com'
        bob.getDisplayName() == 'Bob'
        bob.getResponseStatus() == 'accepted'
    }

    def 'CREATED and LAST-MODIFIED map to created/updated'() {
        given: 'a VEVENT with timestamps'
        Instant created = Instant.parse('2026-01-15T10:00:00Z')
        Instant updated = Instant.parse('2026-02-20T12:30:00Z')
        VEvent source = new VEvent(new PropertyList())
        source.add(new Uid('ts@example.com'))
        source.add(new Created(created))
        source.add(new LastModified(updated))

        when: 'converted to Google Event'
        Event ge = new GoogleEventBuilder().vevent(source).build()

        then: 'Event created and updated are set'
        ge.getCreated().getValue() == created.toEpochMilli()
        ge.getUpdated().getValue() == updated.toEpochMilli()
    }
}

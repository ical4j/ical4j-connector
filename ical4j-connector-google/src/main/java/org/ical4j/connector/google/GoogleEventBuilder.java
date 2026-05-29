package org.ical4j.connector.google;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.Organizer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * Copyright (c) 2026, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class GoogleEventBuilder {

    private VEvent icalEvent;

    public GoogleEventBuilder vevent(VEvent icalEvent) {
        this.icalEvent = icalEvent;
        return this;
    }

    public Event build() {
        Event event = new Event();
        icalEvent.<net.fortuna.ical4j.model.property.Uid>getProperty(Property.UID)
                .ifPresent(p -> event.setICalUID(p.getValue()));
        icalEvent.<net.fortuna.ical4j.model.property.Summary>getProperty(Property.SUMMARY)
                .ifPresent(p -> event.setSummary(p.getValue()));
        icalEvent.<net.fortuna.ical4j.model.property.Description>getProperty(Property.DESCRIPTION)
                .ifPresent(p -> event.setDescription(p.getValue()));
        icalEvent.<net.fortuna.ical4j.model.property.Location>getProperty(Property.LOCATION)
                .ifPresent(p -> event.setLocation(p.getValue()));

        icalEvent.<DateProperty<Temporal>>getProperty(Property.DTSTART)
                .ifPresent(p -> event.setStart(toEventDateTime(p)));
        icalEvent.<DateProperty<Temporal>>getProperty(Property.DTEND)
                .ifPresent(p -> event.setEnd(toEventDateTime(p)));

        icalEvent.<Organizer>getProperty(Property.ORGANIZER)
                .ifPresent(p -> event.setOrganizer(toOrganizer(p)));

        List<Attendee> attendees = icalEvent.getProperties(Property.ATTENDEE);
        if (!attendees.isEmpty()) {
            List<EventAttendee> mapped = new ArrayList<>();
            for (Attendee a : attendees) {
                mapped.add(toAttendee(a));
            }
            event.setAttendees(mapped);
        }

        List<String> recurrence = new ArrayList<>();
        for (Property p : icalEvent.<Property>getProperties(Property.RRULE)) {
            recurrence.add("RRULE:" + p.getValue());
        }
        for (Property p : icalEvent.<Property>getProperties(Property.RDATE)) {
            recurrence.add("RDATE:" + p.getValue());
        }
        for (Property p : icalEvent.<Property>getProperties(Property.EXDATE)) {
            recurrence.add("EXDATE:" + p.getValue());
        }
        if (!recurrence.isEmpty()) {
            event.setRecurrence(recurrence);
        }

        icalEvent.<net.fortuna.ical4j.model.property.Created>getProperty(Property.CREATED)
                .ifPresent(p -> event.setCreated(new DateTime(p.getDate().toEpochMilli(), 0)));
        icalEvent.<net.fortuna.ical4j.model.property.LastModified>getProperty(Property.LAST_MODIFIED)
                .ifPresent(p -> event.setUpdated(new DateTime(p.getDate().toEpochMilli(), 0)));

        return event;
    }

    private static EventDateTime toEventDateTime(DateProperty<Temporal> property) {
        Temporal temporal = property.getDate();
        EventDateTime edt = new EventDateTime();
        if (temporal instanceof LocalDate) {
            LocalDate ld = (LocalDate) temporal;
            edt.setDate(new DateTime(true, ld.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), 0));
        } else if (temporal instanceof ZonedDateTime) {
            ZonedDateTime zdt = (ZonedDateTime) temporal;
            int tzShift = zdt.getOffset().getTotalSeconds() / 60;
            edt.setDateTime(new DateTime(zdt.toInstant().toEpochMilli(), tzShift));
            edt.setTimeZone(zdt.getZone().getId());
        } else if (temporal instanceof Instant) {
            Instant ins = (Instant) temporal;
            edt.setDateTime(new DateTime(ins.toEpochMilli(), 0));
            edt.setTimeZone("UTC");
        }
        return edt;
    }

    private static com.google.api.services.calendar.model.Event.Organizer toOrganizer(Organizer property) {
        com.google.api.services.calendar.model.Event.Organizer organizer =
                new com.google.api.services.calendar.model.Event.Organizer();
        organizer.setEmail(stripMailto(property.getValue()));
        property.<Cn>getParameter(net.fortuna.ical4j.model.Parameter.CN)
                .ifPresent(cn -> organizer.setDisplayName(cn.getValue()));
        return organizer;
    }

    private static EventAttendee toAttendee(Attendee property) {
        EventAttendee attendee = new EventAttendee();
        attendee.setEmail(stripMailto(property.getValue()));
        property.<Cn>getParameter(net.fortuna.ical4j.model.Parameter.CN)
                .ifPresent(cn -> attendee.setDisplayName(cn.getValue()));
        Optional<PartStat> partStat = property.getParameter(net.fortuna.ical4j.model.Parameter.PARTSTAT);
        partStat.map(ps -> mapPartStat(ps.getValue())).ifPresent(attendee::setResponseStatus);
        return attendee;
    }

    static String mapPartStat(String partStatValue) {
        if (partStatValue == null) return null;
        switch (partStatValue) {
            case "ACCEPTED":     return "accepted";
            case "DECLINED":     return "declined";
            case "TENTATIVE":    return "tentative";
            case "NEEDS-ACTION": return "needsAction";
            default:             return "needsAction";
        }
    }

    private static String stripMailto(String value) {
        if (value == null) return null;
        if (value.regionMatches(true, 0, "mailto:", 0, 7)) {
            return value.substring(7);
        }
        return value;
    }
}

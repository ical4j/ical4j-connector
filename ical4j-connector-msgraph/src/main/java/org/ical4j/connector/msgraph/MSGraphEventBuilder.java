package org.ical4j.connector.msgraph;

import com.microsoft.graph.models.AttendeeType;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.DateTimeTimeZone;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Location;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.ResponseStatus;
import com.microsoft.graph.models.ResponseType;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RRule;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

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

/**
 * Builds a Microsoft Graph {@link Event} from an iCal4j {@link VEvent}, mapping the common calendar
 * fields. {@code iCalUId} is intentionally left unset because Graph assigns it server-side and
 * ignores a client-supplied value on create (see the {@code complete-msgraph-connector} change).
 */
public class MSGraphEventBuilder {

    private static final DateTimeFormatter GRAPH_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private VEvent icalEvent;

    public MSGraphEventBuilder vevent(VEvent icalEvent) {
        this.icalEvent = icalEvent;
        return this;
    }

    public Event build() {
        Event event = new Event();

        icalEvent.<net.fortuna.ical4j.model.property.Summary>getProperty(Property.SUMMARY)
                .ifPresent(p -> event.setSubject(p.getValue()));
        icalEvent.<net.fortuna.ical4j.model.property.Description>getProperty(Property.DESCRIPTION)
                .ifPresent(p -> event.setBody(textBody(p.getValue())));
        icalEvent.<net.fortuna.ical4j.model.property.Location>getProperty(Property.LOCATION)
                .ifPresent(p -> {
                    Location location = new Location();
                    location.setDisplayName(p.getValue());
                    event.setLocation(location);
                });

        boolean allDay = isAllDay(icalEvent.getProperty(Property.DTSTART).orElse(null));
        if (allDay) {
            event.setIsAllDay(true);
        }
        icalEvent.<DateProperty<Temporal>>getProperty(Property.DTSTART)
                .ifPresent(p -> event.setStart(toDateTimeTimeZone(p)));
        icalEvent.<DateProperty<Temporal>>getProperty(Property.DTEND)
                .ifPresent(p -> event.setEnd(toDateTimeTimeZone(p)));

        icalEvent.<Organizer>getProperty(Property.ORGANIZER)
                .ifPresent(p -> event.setOrganizer(toRecipient(p)));

        List<Attendee> attendees = icalEvent.getProperties(Property.ATTENDEE);
        if (!attendees.isEmpty()) {
            List<com.microsoft.graph.models.Attendee> mapped = new ArrayList<>();
            for (Attendee a : attendees) {
                mapped.add(toAttendee(a));
            }
            event.setAttendees(mapped);
        }

        icalEvent.<RRule<Temporal>>getProperty(Property.RRULE).ifPresent(rrule -> {
            LocalDate start = icalEvent.<DateProperty<Temporal>>getProperty(Property.DTSTART)
                    .map(p -> toLocalDate(p.getDate())).orElse(null);
            if (start != null) {
                var recurrence = RecurrenceMapping.toPatternedRecurrence(rrule.getRecur(), start);
                if (recurrence != null) {
                    event.setRecurrence(recurrence);
                }
            }
        });

        return event;
    }

    private static ItemBody textBody(String content) {
        ItemBody body = new ItemBody();
        body.setContentType(BodyType.Text);
        body.setContent(content);
        return body;
    }

    private static boolean isAllDay(Property dtStart) {
        if (dtStart instanceof DateProperty) {
            return ((DateProperty<?>) dtStart).getDate() instanceof LocalDate;
        }
        return false;
    }

    private static DateTimeTimeZone toDateTimeTimeZone(DateProperty<Temporal> property) {
        Temporal temporal = property.getDate();
        DateTimeTimeZone result = new DateTimeTimeZone();
        if (temporal instanceof LocalDate) {
            result.setDateTime(((LocalDate) temporal).atStartOfDay().format(GRAPH_DATE_TIME));
            result.setTimeZone("UTC");
            return result;
        }

        LocalDateTime local;
        if (temporal instanceof ZonedDateTime) {
            local = ((ZonedDateTime) temporal).toLocalDateTime();
        } else if (temporal instanceof OffsetDateTime) {
            local = ((OffsetDateTime) temporal).toLocalDateTime();
        } else if (temporal instanceof Instant) {
            local = ((Instant) temporal).atZone(ZoneOffset.UTC).toLocalDateTime();
        } else {
            local = (LocalDateTime) temporal;
        }
        result.setDateTime(local.format(GRAPH_DATE_TIME));

        // Prefer the explicit TZID parameter: a TZID without an in-calendar VTIMEZONE leaves the
        // parsed ZonedDateTime carrying a synthetic ical4j zone id, so the parameter is authoritative.
        String tzid = property.getParameter(Parameter.TZID).map(net.fortuna.ical4j.model.Parameter::getValue).orElse(null);
        result.setTimeZone(tzid != null ? tzid : "UTC");
        return result;
    }

    private static LocalDate toLocalDate(Temporal temporal) {
        if (temporal instanceof LocalDate) {
            return (LocalDate) temporal;
        }
        if (temporal instanceof Instant) {
            return ((Instant) temporal).atZone(ZoneOffset.UTC).toLocalDate();
        }
        return LocalDate.from(temporal);
    }

    private static Recipient toRecipient(Organizer property) {
        Recipient recipient = new Recipient();
        recipient.setEmailAddress(emailAddress(property.getValue(),
                property.<Cn>getParameter(Parameter.CN).map(Cn::getValue).orElse(null)));
        return recipient;
    }

    private static com.microsoft.graph.models.Attendee toAttendee(Attendee property) {
        com.microsoft.graph.models.Attendee attendee = new com.microsoft.graph.models.Attendee();
        attendee.setEmailAddress(emailAddress(property.getValue(),
                property.<Cn>getParameter(Parameter.CN).map(Cn::getValue).orElse(null)));
        attendee.setType(AttendeeType.Required);
        property.<PartStat>getParameter(Parameter.PARTSTAT).ifPresent(ps -> {
            ResponseStatus status = new ResponseStatus();
            status.setResponse(toResponseType(ps.getValue()));
            attendee.setStatus(status);
        });
        return attendee;
    }

    private static EmailAddress emailAddress(String value, String name) {
        EmailAddress email = new EmailAddress();
        email.setAddress(stripMailto(value));
        if (name != null) {
            email.setName(name);
        }
        return email;
    }

    static ResponseType toResponseType(String partStat) {
        if (partStat == null) {
            return ResponseType.NotResponded;
        }
        switch (partStat) {
            case "ACCEPTED": return ResponseType.Accepted;
            case "DECLINED": return ResponseType.Declined;
            case "TENTATIVE": return ResponseType.TentativelyAccepted;
            default: return ResponseType.NotResponded;
        }
    }

    private static String stripMailto(String value) {
        if (value == null) {
            return null;
        }
        if (value.regionMatches(true, 0, "mailto:", 0, 7)) {
            return value.substring(7);
        }
        return value;
    }
}

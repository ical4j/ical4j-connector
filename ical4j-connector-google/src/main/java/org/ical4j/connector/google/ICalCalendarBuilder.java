package org.ical4j.connector.google;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
public class ICalCalendarBuilder {

    public Calendar build(Event event) throws IOException {
        VEvent vevent = new VEvent(new PropertyList());

        vevent.add(new Uid(event.getICalUID() != null ? event.getICalUID() : UUID.randomUUID().toString()));

        if (event.getSummary() != null) {
            vevent.add(new Summary(event.getSummary()));
        }
        if (event.getDescription() != null) {
            vevent.add(new Description(event.getDescription()));
        }
        if (event.getLocation() != null) {
            vevent.add(new Location(event.getLocation()));
        }

        if (event.getStart() != null) {
            vevent.add(new DtStart<>(toTemporal(event.getStart())));
        }
        if (event.getEnd() != null) {
            vevent.add(new DtEnd<>(toTemporal(event.getEnd())));
        }

        if (event.getOrganizer() != null && event.getOrganizer().getEmail() != null) {
            vevent.add(toOrganizer(event.getOrganizer()));
        }

        if (event.getAttendees() != null) {
            for (EventAttendee attendee : event.getAttendees()) {
                vevent.add(toAttendee(attendee));
            }
        }

        if (event.getRecurrence() != null) {
            for (String line : event.getRecurrence()) {
                if (line == null) continue;
                int sep = line.indexOf(':');
                if (sep < 0) continue;
                String name = line.substring(0, sep);
                String value = line.substring(sep + 1);
                switch (name) {
                    case "RRULE": vevent.add(new RRule<>(value)); break;
                    case "RDATE": vevent.add(new RDate<>(new ParameterList(), value)); break;
                    case "EXDATE": vevent.add(new ExDate<>(value)); break;
                }
            }
        }

        if (event.getCreated() != null) {
            vevent.add(new Created(Instant.ofEpochMilli(event.getCreated().getValue())));
        }
        if (event.getUpdated() != null) {
            vevent.add(new LastModified(Instant.ofEpochMilli(event.getUpdated().getValue())));
        }

        Calendar calendar = new Calendar();
        calendar.add(vevent);
        return calendar;
    }

    private static java.time.temporal.Temporal toTemporal(EventDateTime edt) {
        if (edt.getDate() != null) {
            return LocalDate.parse(edt.getDate().toStringRfc3339());
        }
        if (edt.getDateTime() != null) {
            Instant instant = Instant.ofEpochMilli(edt.getDateTime().getValue());
            String tz = edt.getTimeZone();
            if (tz != null) {
                return ZonedDateTime.ofInstant(instant, ZoneId.of(tz));
            }
            return instant;
        }
        return null;
    }

    private static Organizer toOrganizer(Event.Organizer source) {
        ParameterList params = new ParameterList(source.getDisplayName() != null
                ? List.of(new Cn(source.getDisplayName()))
                : List.of());
        return new Organizer(params, URI.create("mailto:" + source.getEmail()));
    }

    private static Attendee toAttendee(EventAttendee source) {
        List<net.fortuna.ical4j.model.Parameter> params = new ArrayList<>();
        if (source.getDisplayName() != null) {
            params.add(new Cn(source.getDisplayName()));
        }
        if (source.getResponseStatus() != null) {
            params.add(new PartStat(mapResponseStatus(source.getResponseStatus())));
        }
        return new Attendee(new ParameterList(params), URI.create("mailto:" + source.getEmail()));
    }

    static String mapResponseStatus(String responseStatus) {
        if (responseStatus == null) return PartStat.NEEDS_ACTION.getValue();
        switch (responseStatus) {
            case "accepted":    return PartStat.ACCEPTED.getValue();
            case "declined":    return PartStat.DECLINED.getValue();
            case "tentative":   return PartStat.TENTATIVE.getValue();
            case "needsAction": return PartStat.NEEDS_ACTION.getValue();
            default:            return PartStat.NEEDS_ACTION.getValue();
        }
    }
}

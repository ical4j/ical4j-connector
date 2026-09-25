package org.ical4j.connector.msgraph;

import com.microsoft.graph.models.Attendee;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.DateTimeTimeZone;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.ResponseType;
import net.fortuna.ical4j.data.ContentHandler;
import net.fortuna.ical4j.data.DefaultContentHandler;
import net.fortuna.ical4j.model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

/**
 * Builds an iCal4j {@link Calendar} from a Microsoft Graph {@link Event}, mapping the common
 * calendar fields (see the {@code complete-msgraph-connector} change spec). Construction is driven
 * through a {@link ContentHandler} so a caller may inject a custom handler / {@link TimeZoneRegistry}.
 */
public class ICalCalendarBuilder {

    private static final DateTimeFormatter LOCAL_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private static final DateTimeFormatter UTC_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private final ContentHandler contentHandler;

    private final TimeZoneRegistry registry;

    private Calendar calendar;

    public ICalCalendarBuilder() {
        this.registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        this.contentHandler = new DefaultContentHandler(c -> this.calendar = c, registry);
    }

    public ICalCalendarBuilder(ContentHandler contentHandler, TimeZoneRegistry registry) {
        this.contentHandler = contentHandler;
        this.registry = registry;
    }

    public Calendar build(Event event) throws IOException {
        contentHandler.startCalendar();
        contentHandler.startComponent(Component.VEVENT);

        // UID — always emit; generate one only when Graph returns none.
        property(Property.UID, event.getICalUId() != null ? event.getICalUId() : UUID.randomUUID().toString());

        property(Property.SUMMARY, event.getSubject());
        property(Property.DESCRIPTION, bodyText(event.getBody()));
        if (event.getLocation() != null) {
            property(Property.LOCATION, event.getLocation().getDisplayName());
        }

        boolean allDay = Boolean.TRUE.equals(event.getIsAllDay());
        dateProperty(Property.DTSTART, event.getStart(), allDay);
        dateProperty(Property.DTEND, event.getEnd(), allDay);

        organizer(event.getOrganizer());
        if (event.getAttendees() != null) {
            for (Attendee attendee : event.getAttendees()) {
                attendee(attendee);
            }
        }

        if (event.getRecurrence() != null) {
            property(Property.RRULE, RecurrenceMapping.toRruleValue(event.getRecurrence()));
        }

        utcProperty(Property.CREATED, event.getCreatedDateTime());
        utcProperty(Property.LAST_MODIFIED, event.getLastModifiedDateTime());

        contentHandler.endComponent(Component.VEVENT);
        contentHandler.endCalendar();
        return calendar;
    }

    public TimeZoneRegistry getRegistry() {
        return registry;
    }

    private void property(String name, String value) {
        if (value == null) {
            return;
        }
        contentHandler.startProperty(name);
        contentHandler.propertyValue(value);
        contentHandler.endProperty(name);
    }

    private void dateProperty(String name, DateTimeTimeZone source, boolean allDay) {
        if (source == null || source.getDateTime() == null) {
            return;
        }
        LocalDateTime local = LocalDateTime.parse(source.getDateTime());
        contentHandler.startProperty(name);
        if (allDay) {
            contentHandler.parameter(Parameter.VALUE, "DATE");
            contentHandler.propertyValue(local.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE));
        } else {
            ZoneId zone = TimeZoneMapping.toZoneId(source.getTimeZone());
            if (isUtc(zone)) {
                contentHandler.propertyValue(local.format(UTC_DATE_TIME));
            } else {
                contentHandler.parameter(Parameter.TZID, zone.getId());
                contentHandler.propertyValue(local.format(LOCAL_DATE_TIME));
            }
        }
        contentHandler.endProperty(name);
    }

    private void utcProperty(String name, OffsetDateTime value) {
        if (value == null) {
            return;
        }
        contentHandler.startProperty(name);
        contentHandler.propertyValue(value.atZoneSameInstant(ZoneOffset.UTC).format(UTC_DATE_TIME));
        contentHandler.endProperty(name);
    }

    private void organizer(Recipient organizer) {
        if (organizer == null || organizer.getEmailAddress() == null
                || organizer.getEmailAddress().getAddress() == null) {
            return;
        }
        EmailAddress email = organizer.getEmailAddress();
        contentHandler.startProperty(Property.ORGANIZER);
        if (email.getName() != null) {
            contentHandler.parameter(Parameter.CN, email.getName());
        }
        contentHandler.propertyValue("mailto:" + email.getAddress());
        contentHandler.endProperty(Property.ORGANIZER);
    }

    private void attendee(Attendee attendee) {
        if (attendee == null || attendee.getEmailAddress() == null
                || attendee.getEmailAddress().getAddress() == null) {
            return;
        }
        EmailAddress email = attendee.getEmailAddress();
        contentHandler.startProperty(Property.ATTENDEE);
        if (email.getName() != null) {
            contentHandler.parameter(Parameter.CN, email.getName());
        }
        if (attendee.getStatus() != null && attendee.getStatus().getResponse() != null) {
            contentHandler.parameter(Parameter.PARTSTAT, toPartStat(attendee.getStatus().getResponse()));
        }
        contentHandler.propertyValue("mailto:" + email.getAddress());
        contentHandler.endProperty(Property.ATTENDEE);
    }

    private static String bodyText(ItemBody body) {
        if (body == null || body.getContent() == null) {
            return null;
        }
        if (body.getContentType() == BodyType.Html) {
            return stripHtml(body.getContent());
        }
        return body.getContent();
    }

    static String stripHtml(String html) {
        String text = html.replaceAll("(?s)<[^>]*>", " ");
        text = text.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
        return text.replaceAll("\\s+", " ").trim();
    }

    private static String toPartStat(ResponseType response) {
        switch (response) {
            case Accepted: return "ACCEPTED";
            case Declined: return "DECLINED";
            case TentativelyAccepted: return "TENTATIVE";
            default: return "NEEDS-ACTION";
        }
    }

    private static boolean isUtc(ZoneId zone) {
        if (zone.equals(ZoneOffset.UTC)) {
            return true;
        }
        String id = zone.getId();
        return "UTC".equals(id) || "Z".equals(id) || "GMT".equals(id)
                || "Etc/UTC".equals(id) || "Etc/GMT".equals(id);
    }
}

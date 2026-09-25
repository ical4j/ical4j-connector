package org.ical4j.connector.dav.response;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 * Handles a CalDAV free-busy-query REPORT (RFC 4791 §7.10) response. The body is a single
 * iCalendar object containing one VFREEBUSY component, parsed into a {@link Calendar}.
 */
public class GetFreeBusyCalendar extends AbstractResponseHandler<Calendar> {

    @Override
    public Calendar handleResponse(HttpResponse response) throws IOException {
        try {
            return new CalendarBuilder().build(response.getEntity().getContent());
        } catch (ParserException e) {
            throw new IOException("Failed to parse free-busy-query response as iCalendar", e);
        }
    }
}

package org.ical4j.connector.dav.response;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import org.apache.http.HttpResponse;
import org.ical4j.connector.MediaType;

import java.io.IOException;

/**
 * Handles the response for retrieving a calendar resource from a CalDAV server.
 * This class processes the HTTP response to extract the calendar data and returns it as a Calendar object.
 * It uses the iCalendar 2.0 media type for parsing the content.
 */
public class GetCalendarResource extends AbstractResponseHandler<Calendar> {

    @Override
    public Calendar handleResponse(HttpResponse response) throws IOException {
        var content = getContent(response, MediaType.ICALENDAR_2_0);
        if (content != null) {
            try {
                var builder = new CalendarBuilder();
                return builder.build(content);
            } catch (ParserException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}

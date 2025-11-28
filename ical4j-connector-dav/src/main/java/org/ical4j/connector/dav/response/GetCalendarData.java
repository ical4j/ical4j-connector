package org.ical4j.connector.dav.response;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.jackrabbit.webdav.DavException;
import org.ical4j.connector.dav.property.CalDavPropertyName;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the response for retrieving calendar data from a CalDAV server.
 * This class processes the HTTP response to extract calendar data and returns it as a list of Calendar objects.
 * It filters the responses to include only those with the calendar data property.
 */
public class GetCalendarData extends AbstractResponseHandler<List<Calendar>> {

    @Override
    public List<Calendar> handleResponse(HttpResponse response) {
        try {
            var multiStatus = getMultiStatus(response);
            return Arrays.stream(multiStatus.getResponses())
                    .filter(msr -> msr.getProperties(HttpStatus.SC_OK).get(CalDavPropertyName.CALENDAR_DATA) != null)
                    .map(msr -> {
                        try {
                            return new CalendarBuilder().build(
                                    new StringReader((String) msr.getProperties(HttpStatus.SC_OK).get(CalDavPropertyName.CALENDAR_DATA).getValue()));
                        } catch (IOException  | ParserException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());
        } catch (DavException e) {
            throw new RuntimeException(e);
        }
    }
}

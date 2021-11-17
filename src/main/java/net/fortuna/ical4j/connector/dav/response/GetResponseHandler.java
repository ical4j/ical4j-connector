package net.fortuna.ical4j.connector.dav.response;

import net.fortuna.ical4j.connector.dav.ResponseHandler;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

public class GetResponseHandler implements ResponseHandler {

    private final HttpGet httpGet;

    private HttpResponse httpResponse;

    public GetResponseHandler(HttpGet httpGet) {
        this.httpGet = httpGet;
    }

    /**
     * @return a calendar object instance
     * @throws IOException where a communication error occurs
     * @throws ParserException where calendar parsing fails
     */
    public Calendar getCalendar() throws IOException, ParserException {
        String contentType = httpGet.getFirstHeader("Content-Type").getValue();
        if (contentType.startsWith("text/calendar")) {
            CalendarBuilder builder = new CalendarBuilder();
            return builder.build(httpResponse.getEntity().getContent());
        }
        return null;
    }

    @Override
    public void accept(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }
}

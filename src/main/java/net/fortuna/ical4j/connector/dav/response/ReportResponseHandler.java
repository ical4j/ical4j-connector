package net.fortuna.ical4j.connector.dav.response;

import net.fortuna.ical4j.connector.dav.ResponseHandler;
import net.fortuna.ical4j.connector.dav.property.CalDavPropertyName;
import net.fortuna.ical4j.connector.dav.property.CardDavPropertyName;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import org.apache.http.HttpResponse;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.HttpReport;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.w3c.dom.DOMException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ReportResponseHandler implements ResponseHandler {

    private final HttpReport httpReport;

    private HttpResponse httpResponse;

    public ReportResponseHandler(HttpReport httpReport) {
        this.httpReport = httpReport;
    }

    /**
     * @return an array of calendar objects
     * @throws IOException where communication fails
     * @throws DavException where the DAV method fails
     * @throws DOMException where XML parsing fails
     * @throws ParserException where calendar parsing fails
     */
    public Calendar[] getCalendars() throws IOException, DavException, DOMException, ParserException {
        List<Calendar> calendars = new ArrayList<Calendar>();
        MultiStatus multi = httpReport.getResponseBodyAsMultiStatus(httpResponse);
        for (MultiStatusResponse response : multi.getResponses()) {
            DavPropertySet props = response.getProperties(200);
            if (props.get(CalDavPropertyName.CALENDAR_DATA) != null) {
                String value = (String) props.get(CalDavPropertyName.CALENDAR_DATA).getValue();
                CalendarBuilder builder = new CalendarBuilder();
                calendars.add(builder.build(new StringReader(value)));
            }
        }
        return calendars.toArray(new Calendar[calendars.size()]);
    }

    public VCard[] getVCards() throws IOException, DavException, DOMException {
        List<VCard> cards = new ArrayList<VCard>();
        MultiStatus multi = httpReport.getResponseBodyAsMultiStatus(httpResponse);
        for (MultiStatusResponse response : multi.getResponses()) {
            DavPropertySet props = response.getProperties(200);
            if (props.get(CardDavPropertyName.ADDRESS_DATA) != null) {
                String value = (String) props.get(CardDavPropertyName.ADDRESS_DATA).getValue();
                VCardBuilder builder = new VCardBuilder(new StringReader(value));
                try {
                    cards.add(builder.build());
                } catch (ParserException e) {
                    System.out.println(e.getMessage());
                    System.out.println(value);
                }
            }
        }
        return cards.toArray(new VCard[cards.size()]);
    }

    @Override
    public void accept(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }
}

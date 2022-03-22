package net.fortuna.ical4j.connector.dav;

import net.fortuna.ical4j.connector.FailedOperationException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.request.CalendarQuery;
import net.fortuna.ical4j.connector.dav.response.GetCalendarResource;
import net.fortuna.ical4j.model.Calendar;
import org.apache.http.client.ResponseHandler;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public interface CalDavSupport extends WebDavSupport {

    /**
     * <pre>
     *    An HTTP request using the MKCALENDAR method creates a new calendar
     *    collection resource.  A server MAY restrict calendar collection
     *    creation to particular collections.
     *
     *    Support for MKCALENDAR on the server is only RECOMMENDED and not
     *    REQUIRED because some calendar stores only support one calendar per
     *    user (or principal), and those are typically pre-created for each
     *    account.  However, servers and clients are strongly encouraged to
     *    support MKCALENDAR whenever possible to allow users to create
     *    multiple calendar collections to help organize their data better.
     *
     *    Clients SHOULD use the DAV:displayname property for a human-readable
     *    name of the calendar.  Clients can either specify the value of the
     *    DAV:displayname property in the request body of the MKCALENDAR
     *    request, or alternatively issue a PROPPATCH request to change the
     *    DAV:displayname property to the appropriate value immediately after
     *    issuing the MKCALENDAR request.  Clients SHOULD NOT set the DAV:
     *    displayname property to be the same as any other calendar collection
     *    at the same URI "level".  When displaying calendar collections to
     *    users, clients SHOULD check the DAV:displayname property and use that
     *    value as the name of the calendar.  In the event that the DAV:
     *    displayname property is empty, the client MAY use the last part of
     *    the calendar collection URI as the name; however, that path segment
     *    may be "opaque" and not represent any meaningful human-readable text.
     *
     *    If a MKCALENDAR request fails, the server state preceding the request
     *    MUST be restored.
     *
     *    Marshalling:
     *       If a request body is included, it MUST be a CALDAV:mkcalendar XML
     *       element.  Instruction processing MUST occur in the order
     *       instructions are received (i.e., from top to bottom).
     *       Instructions MUST either all be executed or none executed.  Thus,
     *       if any error occurs during processing, all executed instructions
     *       MUST be undone and a proper error result returned.  Instruction
     *       processing details can be found in the definition of the DAV:set
     *       instruction in Section 12.13.2 of [RFC2518].
     *
     *          <!ELEMENT mkcalendar (DAV:set)>
     *
     *       If a response body for a successful request is included, it MUST
     *       be a CALDAV:mkcalendar-response XML element.
     *
     *          <!ELEMENT mkcalendar-response ANY>
     *
     *       The response MUST include a Cache-Control:no-cache header.
     *
     *    Preconditions:
     *
     *       (DAV:resource-must-be-null): A resource MUST NOT exist at the
     *       Request-URI;
     *
     *       (CALDAV:calendar-collection-location-ok): The Request-URI MUST
     *       identify a location where a calendar collection can be created;
     *
     *       (CALDAV:valid-calendar-data): The time zone specified in the
     *       CALDAV:calendar-timezone property MUST be a valid iCalendar object
     *       containing a single valid VTIMEZONE component;
     *
     *       (DAV:needs-privilege): The DAV:bind privilege MUST be granted to
     *       the current user on the parent collection of the Request-URI.
     *
     *    Postconditions:
     *
     *       (CALDAV:initialize-calendar-collection): A new calendar collection
     *       exists at the Request-URI.  The DAV:resourcetype of the calendar
     *       collection MUST contain both DAV:collection and CALDAV:calendar
     *       XML elements.
     *       </pre>
     *
     * @param uri the (partial) URI of the collection to create
     * @param properties a set of DAV properties to initialise the collection
     * @throws IOException for failures such as network connectivity
     * @throws ObjectStoreException when creation fails (i.e. non-2xx HTTP status)
     */
    void mkCalendar(String uri, DavPropertySet properties) throws IOException, ObjectStoreException, DavException;

    default Map<String, DavPropertySet> report(String path, CalendarQuery query, ReportType reportType,
                                              DavPropertyName...propertyNames) throws IOException, ParserConfigurationException {
        DavPropertyNameSet nameSet = new DavPropertyNameSet();
        Arrays.stream(propertyNames).forEach(nameSet::add);
        return report(path, query, reportType, nameSet);
    }

    /**
     * <pre>
     *    The REPORT method (defined in Section 3.6 of [RFC3253]) provides an
     *    extensible mechanism for obtaining information about one or more
     *    resources.  Unlike the PROPFIND method, which returns the value of
     *    one or more named properties, the REPORT method can involve more
     *    complex processing.  REPORT is valuable in cases where the server has
     *    access to all of the information needed to perform the complex
     *    request (such as a query), and where it would require multiple
     *    requests for the client to retrieve the information needed to perform
     *    the same request.
     *
     *    CalDAV servers MUST support the DAV:expand-property REPORT defined in
     *    Section 3.8 of [RFC3253].
     *    </pre>
     *
     * @param uri
     * @param query
     * @param reportType
     * @param propertyNames
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     */
    Map<String, DavPropertySet> report(String uri, CalendarQuery query, ReportType reportType,
                                       DavPropertyNameSet propertyNames) throws IOException, ParserConfigurationException;

    <T> T report(String path, ReportInfo info, ResponseHandler<T> handler) throws IOException,
            ParserConfigurationException;

    /**
     * Save calendar data.
     * @param uri
     * @param calendar
     * @throws IOException
     */
    void put(String uri, Calendar calendar, String etag) throws IOException, FailedOperationException;

    default Calendar getCalendar(String path) throws IOException {
        return get(path, new GetCalendarResource());
    }
}

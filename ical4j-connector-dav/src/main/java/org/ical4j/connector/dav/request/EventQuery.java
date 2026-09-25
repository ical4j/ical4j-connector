package org.ical4j.connector.dav.request;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.ical4j.connector.dav.property.BaseDavPropertyName;
import org.ical4j.connector.dav.property.CalDavPropertyName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a CalDAV event query that can be serialized to XML.
 * This class is used to build a query request for events within a specified time range.
 * It extends the ReportInfo class to provide additional functionality for CalDAV reports.
 *
 * @see <a href="https://tools.ietf.org/html/rfc4791">RFC 4791 - Calendaring Extensions to WebDAV (CalDAV)</a>
 */
public class EventQuery extends ReportInfo implements XmlSupport {

    private DateTime startTime;

    private DateTime endTime;

    public EventQuery(int depth) {
        super(CalDavPropertyName.CALENDAR_QUERY, depth);
    }

    public EventQuery withStartTime(DateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public EventQuery withEndTime(DateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    @Override
    public Element toXml(Document document) {
        var prop = newElement(document, BaseDavPropertyName.PROP,
                newElement(document, DavConstants.PROPERTY_GETETAG, DavConstants.NAMESPACE),
                newElement(document, CalDavPropertyName.CALENDAR_DATA));
        setContentElement(prop);

        var filter = newCalDavElement(document, CalDavPropertyName.PROPERTY_FILTER,
                newComponentFilter(document, Calendar.VCALENDAR,
                        newComponentFilter(document, Component.VEVENT,
                                newTimeRange(document, startTime.toString(), endTime.toString()))));
        setContentElement(filter);

        return super.toXml(document);
    }
}

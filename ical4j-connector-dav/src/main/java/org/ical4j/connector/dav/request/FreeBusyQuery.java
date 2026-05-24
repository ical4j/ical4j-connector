package org.ical4j.connector.dav.request;

import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.ical4j.connector.dav.property.CalDavPropertyName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Represents a CalDAV free-busy-query REPORT request body (RFC 4791 §7.10). Used to compute
 * free/busy intervals for the resources in a calendar collection over a bounded time range.
 */
public class FreeBusyQuery extends ReportInfo implements XmlSupport {

    private static final DateTimeFormatter UTC_DATETIME =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final Instant start;
    private final Instant end;

    public FreeBusyQuery(Instant start, Instant end) {
        super(CalDavPropertyName.FREEBUSY_QUERY, 0);
        this.start = start;
        this.end = end;
    }

    @Override
    public Element toXml(Document document) {
        var timeRange = newTimeRange(document, UTC_DATETIME.format(start), UTC_DATETIME.format(end));
        setContentElement(timeRange);
        return super.toXml(document);
    }
}

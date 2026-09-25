package org.ical4j.connector.dav.request;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.ical4j.connector.dav.property.BaseDavPropertyName;
import org.ical4j.connector.dav.property.CalDavPropertyName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Represents a CalDAV calendar-multiget REPORT request body (RFC 4791 §7.9). Used to fetch a
 * specific set of calendar object resources identified by their href, returning GETETAG and
 * calendar-data for each in a single request.
 */
public class CalendarMultiget extends ReportInfo implements XmlSupport {

    private final List<String> hrefs;

    public CalendarMultiget(List<String> hrefs) {
        super(CalDavPropertyName.CALENDAR_MULTIGET, 0);
        this.hrefs = hrefs;
    }

    @Override
    public Element toXml(Document document) {
        var prop = newElement(document, BaseDavPropertyName.PROP,
                newElement(document, DavConstants.PROPERTY_GETETAG, DavConstants.NAMESPACE),
                newElement(document, CalDavPropertyName.CALENDAR_DATA));
        setContentElement(prop);

        for (String href : hrefs) {
            var hrefElement = newElement(document, DavConstants.XML_HREF, DavConstants.NAMESPACE);
            hrefElement.setTextContent(href);
            setContentElement(hrefElement);
        }

        return super.toXml(document);
    }
}

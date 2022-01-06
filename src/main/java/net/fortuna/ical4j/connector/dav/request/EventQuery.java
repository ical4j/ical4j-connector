package net.fortuna.ical4j.connector.dav.request;

import net.fortuna.ical4j.connector.dav.property.BaseDavPropertyName;
import net.fortuna.ical4j.connector.dav.property.CalDavPropertyName;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

        Element calData = newElement(document, CalDavPropertyName.CALENDAR_DATA);

        Element calFilter = newComponentFilter(document, Calendar.VCALENDAR,
                newComponentFilter(document, Component.VEVENT,
                        newTimeRange(document, startTime.toString(), endTime.toString())));

        Element property = newElement(document, BaseDavPropertyName.PROP,
                newCalDavElement(document, DavConstants.PROPERTY_GETETAG),
                document.importNode(calData, true));

        document.appendChild(property);
        setContentElement(property);

        Element parentFilter = newCalDavElement(document, CalDavPropertyName.PROPERTY_FILTER);
        setContentElement(parentFilter);

        Node importedFilter = document.importNode(calFilter, true);
        parentFilter.appendChild(importedFilter);
        DavPropertyNameSet propertyNames = new DavPropertyNameSet();
        propertyNames.add(DavPropertyName.create(DavPropertyName.XML_PROP, CalDavPropertyName.NAMESPACE));
        propertyNames.add(DavPropertyName.GETETAG);

        return super.toXml(document);
    }
}

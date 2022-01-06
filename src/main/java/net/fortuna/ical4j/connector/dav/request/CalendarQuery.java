package net.fortuna.ical4j.connector.dav.request;

import net.fortuna.ical4j.connector.dav.property.CalDavPropertyName;
import net.fortuna.ical4j.model.Calendar;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;

public class CalendarQuery implements XmlSupport, XmlSerializable {

    private final String componentType;

    public CalendarQuery(String componentType) {
        this.componentType = componentType;
    }

    public Element build() throws ParserConfigurationException {
        Document document = newXmlDocument();
        return toXml(document);
    }

    @Override
    public Element toXml(Document document) {
        return newCalDavElement(document, CalDavPropertyName.PROPERTY_FILTER,
                newComponentFilter(document, Calendar.VCALENDAR,
                        newComponentFilter(document, componentType)));
    }
}

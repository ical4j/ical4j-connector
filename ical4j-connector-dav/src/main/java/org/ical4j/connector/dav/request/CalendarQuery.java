package org.ical4j.connector.dav.request;

import net.fortuna.ical4j.model.Calendar;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.ical4j.connector.dav.property.CalDavPropertyName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Arrays;

public class CalendarQuery implements XmlSupport, XmlSerializable {

    private final String[] componentType;

    public CalendarQuery(String... componentType) {
        this.componentType = componentType;
    }

    public Element build() throws ParserConfigurationException {
        var document = newXmlDocument();
//        DomUtil.setNamespaceAttribute(document.getDocumentElement(), "xmlns:d", "DAV:");
//        DomUtil.setNamespaceAttribute(document.getDocumentElement(), "xmlns:c",
//                "urn:ietf:params:xml:ns:caldav");
        return toXml(document);
    }

    @Override
    public Element toXml(Document document) {
        return newCalDavElement(document, CalDavPropertyName.PROPERTY_FILTER,
                newComponentFilter(document, Calendar.VCALENDAR,
                        Arrays.stream(componentType).map(t ->
                                newComponentFilter(document, t)).toArray(Element[]::new)));
    }
}

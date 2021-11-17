package net.fortuna.ical4j.connector.dav.request;

import net.fortuna.ical4j.connector.dav.CalDavConstants;
import net.fortuna.ical4j.model.Calendar;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;

public class CalendarQuery implements XmlSupport {

    private final String componentType;

    public CalendarQuery(String componentType) {
        this.componentType = componentType;
    }

    public Element build() throws ParserConfigurationException {
        Document document = newXmlDocument();
        return newCalDavElement(document, CalDavConstants.PROPERTY_FILTER,
                newComponentFilter(document, Calendar.VCALENDAR,
                        newComponentFilter(document, componentType)));
    }


}

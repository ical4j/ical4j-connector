package org.ical4j.connector.dav.request;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.ical4j.connector.dav.property.CSDavPropertyName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a CalDAV expand property query that can be serialized to XML.
 * This class is used to build a query request for expanding properties related to proxy read/write.
 * It allows specifying additional property names to include in the query.
 *
 * @see <a href="https://tools.ietf.org/html/rfc4791">RFC 4791 - Calendaring Extensions to WebDAV (CalDAV)</a>
 */
public class ExpandPropertyQuery implements XmlSupport, XmlSerializable {

    public enum Type {
        PROXY_READ_FOR(CSDavPropertyName.PROXY_READ_FOR),
        PROXY_WRITE_FOR(CSDavPropertyName.PROXY_WRITE_FOR);

        private final DavPropertyName propertyName;

        Type(DavPropertyName propertyName) {
            this.propertyName = propertyName;
        }

        public DavPropertyName getPropertyName() {
            return propertyName;
        }
    }

    private final Type type;

    private final List<DavPropertyName> propertyNames;

    public ExpandPropertyQuery(Type type) {
        this.type = type;
        propertyNames = new ArrayList<>();
    }

    public ExpandPropertyQuery withPropertyName(DavPropertyName propertyName) {
        propertyNames.add(propertyName);
        return this;
    }

    public Element build() throws ParserConfigurationException {
        var document = newXmlDocument();
        return toXml(document);
    }

    @Override
    public Element toXml(Document document) {
        var propertyElement = type.getPropertyName().toXml(document);
        propertyNames.stream().map(p -> p.toXml(document)).forEach(propertyElement::appendChild);
        return propertyElement;
    }
}

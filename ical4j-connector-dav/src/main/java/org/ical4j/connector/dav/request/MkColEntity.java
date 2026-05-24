package org.ical4j.connector.dav.request;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a CalDAV MKCOL request entity that can be serialized to XML.
 * This class is used to create a new collection on a CalDAV server.
 * It contains properties that can be set for the collection, such as metadata.
 *
 * Created on 19/11/2008
 *
 * @author Ben
 */
public class MkColEntity implements XmlSerializable, XmlSupport {

    private DavPropertySet properties;

    public MkColEntity withProperties(DavPropertySet properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public Element toXml(Document document) {
        return newElement(document, "mkcol", DavPropertyName.NAMESPACE,
                newElement(document, DavPropertyName.XML_SET, DavPropertyName.NAMESPACE, properties.toXml(document)));
    }
}

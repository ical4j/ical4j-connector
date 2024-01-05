package org.ical4j.connector.dav.request;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MkColEntity implements XmlSerializable, XmlSupport {

    private DavPropertySet properties;

    public MkColEntity withProperties(DavPropertySet properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public Element toXml(Document document) {
        return newElement(document, "create", DavPropertyName.NAMESPACE,
                newElement(document, DavPropertyName.XML_SET, DavPropertyName.NAMESPACE, properties.toXml(document)));
    }
}

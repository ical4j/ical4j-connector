package org.ical4j.connector.dav.response;

import org.apache.jackrabbit.webdav.property.DavPropertySet;

public class ResourceProps {

    private final String href;

    private final DavPropertySet properties;

    public ResourceProps(String href, DavPropertySet properties) {
        this.href = href;
        this.properties = properties;
    }

    public String getHref() {
        return href;
    }

    public DavPropertySet getProperties() {
        return properties;
    }
}

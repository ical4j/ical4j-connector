package org.ical4j.connector.dav.response;

import org.apache.jackrabbit.webdav.property.DavPropertySet;

/**
 * Represents the properties of a resource in a WebDAV response.
 * This class encapsulates the resource's href and its associated properties.
 */
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

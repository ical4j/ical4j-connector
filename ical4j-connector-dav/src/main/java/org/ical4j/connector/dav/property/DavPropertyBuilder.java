package org.ical4j.connector.dav.property;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.HrefProperty;
import org.apache.jackrabbit.webdav.security.SecurityConstants;

import java.util.Collections;
import java.util.List;

/**
 * Builder for creating DavProperty instances with a specified name and value.
 * This builder supports special handling for properties that are expected to be
 * href properties, such as those defined in the SecurityConstants.
 *
 * @param <T> the type of the value of the property
 */
public class DavPropertyBuilder<T> {

    private static final List<DavPropertyName> hrefProps = Collections.singletonList(SecurityConstants.PRINCIPAL_COLLECTION_SET);

    private DavPropertyName name;

    private T value;

    public DavPropertyBuilder<T> name(DavPropertyName name) {
        this.name = name;
        return this;
    }

    public DavPropertyBuilder<T> value(T value) {
        this.value = value;
        return this;
    }

    @SuppressWarnings("unchecked")
    public DavProperty<T> build() {
        if (hrefProps.contains(name)) {
            return (DavProperty<T>) new HrefProperty(name, (String[]) value, false);
        }
        return new DefaultDavProperty<>(name, value);
    }
}

package org.ical4j.connector.dav.property;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.HrefProperty;
import org.apache.jackrabbit.webdav.security.SecurityConstants;

import java.util.Collections;
import java.util.List;

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

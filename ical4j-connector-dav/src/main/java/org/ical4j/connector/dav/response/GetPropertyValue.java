package org.ical4j.connector.dav.response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * Handles the response for retrieving a specific property value from a CalDAV/CardDAV server.
 *
 * <p>The handler looks up a named property in the SC_OK property set and extracts its
 * value. Servers represent property values in two shapes, and both are tolerated:
 * <ul>
 *   <li>An {@code Element} (Jackrabbit retained the XML node) — the text content of
 *       the element is returned, which transparently handles both
 *       {@code <prop>text</prop>} and {@code <prop><href>text</href></prop>}
 *       (the latter is the RFC shape for href-typed properties).</li>
 *   <li>A {@code String} (Jackrabbit pre-extracted the text) — returned as-is.</li>
 * </ul>
 * Unknown property value shapes return {@code null} rather than throwing.
 *
 * @param <T> the type of the property value to be returned
 */
public class GetPropertyValue<T> extends AbstractResponseHandler<T> {

    private final DavPropertyName propertyName;

    public GetPropertyValue(DavPropertyName propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T handleResponse(HttpResponse httpResponse) throws IOException {
        try {
            var multiStatus = getMultiStatus(httpResponse);
            for (var msr : multiStatus.getResponses()) {
                var prop = msr.getProperties(HttpStatus.SC_OK).get(propertyName);
                if (prop == null) {
                    continue;
                }
                var value = prop.getValue();
                if (value instanceof Element) {
                    return (T) ((Element) value).getTextContent();
                } else if (value instanceof String) {
                    return (T) value;
                } else {
                    return null;
                }
            }
        } catch (DavException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

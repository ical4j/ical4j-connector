package org.ical4j.connector.dav.response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.jackrabbit.webdav.DavException;
import org.w3c.dom.Element;

import java.io.IOException;

public class GetPropertyValue<T> extends AbstractResponseHandler<T> {

    @Override
    public T handleResponse(HttpResponse httpResponse) throws IOException {
        try {
            var multiStatus = getMultiStatus(httpResponse);
            for (var msr : multiStatus.getResponses()) {
                // only one response expected.. return found properties
                var propElement = (Element) msr.getProperties(HttpStatus.SC_OK).iterator().nextProperty().getValue();
                //noinspection unchecked
                return (T) propElement.getFirstChild().getNodeValue();
            }
        } catch (DavException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

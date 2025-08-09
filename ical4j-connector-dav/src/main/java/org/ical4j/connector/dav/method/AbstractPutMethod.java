package org.ical4j.connector.dav.method;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;

import java.net.URI;

/**
 * AbstractPutMethod is a base class for HTTP PUT methods that handle ETag headers.
 * It provides functionality to set ETag headers and check if the response indicates success.
 */
class AbstractPutMethod extends HttpPut {

    public AbstractPutMethod(URI uri) {
        super(uri);
        setEtag(null);
    }

    public AbstractPutMethod(String uri) {
        super(uri);
        setEtag(null);
    }

    public void setEtag(String etag) {
        removeHeaders("If-None-Match");
        removeHeaders("If-Match");

        if (etag != null) {
            addHeader("If-Match", etag);
        } else {
            addHeader("If-None-Match", "*");
        }
    }

    public boolean succeeded(HttpResponse response) {
        int status = response.getStatusLine().getStatusCode();
        return status == HttpStatus.SC_CREATED || status == HttpStatus.SC_NO_CONTENT;
    }
}

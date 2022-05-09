package org.ical4j.connector.dav.method;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;

import java.net.URI;

public class AbstractPutMethod extends HttpPut {

    public AbstractPutMethod(URI uri) {
        super(uri);
        setEtag(null);
    }

    public AbstractPutMethod(String uri) {
        super(uri);
        setEtag(null);
    }

    public void setEtag(String etag) {
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

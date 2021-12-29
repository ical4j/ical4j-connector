package net.fortuna.ical4j.connector.dav.method;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;

import java.net.URI;

public class AbstractPutMethod extends HttpPut {

    public AbstractPutMethod(URI uri, boolean update) {
        super(uri);
        if (update) {
            addHeader("If-Match", "*");
        } else {
            addHeader("If-None-Match", "*");
        }
    }

    public AbstractPutMethod(String uri, boolean update) {
        super(uri);
        if (update) {
            addHeader("If-Match", "*");
        } else {
            addHeader("If-None-Match", "*");
        }
    }

    public boolean succeeded(HttpResponse response) {
        int status = response.getStatusLine().getStatusCode();
        return status == HttpStatus.SC_CREATED || status == HttpStatus.SC_NO_CONTENT;
    }
}

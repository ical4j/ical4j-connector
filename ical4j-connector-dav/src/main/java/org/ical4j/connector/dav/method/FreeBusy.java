package org.ical4j.connector.dav.method;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.Organizer;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.jackrabbit.webdav.DavConstants;

import java.io.UnsupportedEncodingException;
import java.net.URI;

public class FreeBusy extends HttpPost {

    public FreeBusy(URI uri, Organizer organizer) {
        super(uri);
        addHeader(DavConstants.HEADER_CONTENT_TYPE, "text/calendar; charset=utf-8");
        if (organizer != null) {
            // Was removed from the draft, but some servers still need it
            addHeader("Originator", organizer.getValue());
        }
    }

    public FreeBusy(String uri, Organizer organizer) {
        super(uri);
        addHeader(DavConstants.HEADER_CONTENT_TYPE, "text/calendar; charset=utf-8");
        if (organizer != null) {
            // Was removed from the draft, but some servers still need it
            addHeader("Originator", organizer.getValue());
        }
    }

    public void setQuery(Calendar query) throws UnsupportedEncodingException {
        setEntity(new StringEntity(query.toString()));
    }
}

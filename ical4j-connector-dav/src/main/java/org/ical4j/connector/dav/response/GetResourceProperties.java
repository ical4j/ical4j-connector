package org.ical4j.connector.dav.response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

public class GetResourceProperties extends AbstractResponseHandler<DavPropertySet> {

    @Override
    public DavPropertySet handleResponse(HttpResponse response) {
        try {
            MultiStatus multiStatus = getMultiStatus(response);
            for (MultiStatusResponse msr : multiStatus.getResponses()) {
                // only one response expected.. return found properties
                return msr.getProperties(HttpStatus.SC_OK);
            }
        } catch (DavException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

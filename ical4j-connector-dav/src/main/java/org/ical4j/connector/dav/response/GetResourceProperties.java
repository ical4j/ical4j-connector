package org.ical4j.connector.dav.response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GetResourceProperties extends AbstractResponseHandler<List<ResourceProps>> {

    @Override
    public List<ResourceProps> handleResponse(HttpResponse response) {
        try {
            MultiStatus multiStatus = getMultiStatus(response);
            return Arrays.stream(multiStatus.getResponses()).map(msr ->
                    new ResourceProps(msr.getHref(), msr.getProperties(HttpStatus.SC_OK))).collect(Collectors.toList());
        } catch (DavException e) {
            throw new RuntimeException(e);
        }
    }
}

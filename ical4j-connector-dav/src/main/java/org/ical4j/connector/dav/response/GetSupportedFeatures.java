package org.ical4j.connector.dav.response;

import org.apache.http.HttpResponse;
import org.apache.jackrabbit.webdav.DavConstants;
import org.ical4j.connector.dav.SupportedFeature;

import java.util.List;

public class GetSupportedFeatures extends AbstractResponseHandler<List<SupportedFeature>> {

    @Override
    public List<SupportedFeature> handleResponse(HttpResponse response) {
        if (response.getStatusLine().getStatusCode() > 299) {
            throw new RuntimeException("Method failed: " + response.getStatusLine());
        }
        return getHeaderElements(response, DavConstants.HEADER_DAV,
                header -> SupportedFeature.findByDescription(header.getName()));
    }
}

package org.ical4j.connector.dav.response;

import org.apache.http.HttpResponse;
import org.apache.jackrabbit.webdav.DavConstants;
import org.ical4j.connector.dav.SupportedFeature;

import java.util.List;

/**
 * Handles the response for retrieving supported features from a CalDAV server.
 * This class processes the HTTP response to extract supported features
 * and returns them as a list of SupportedFeature objects.
 */
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

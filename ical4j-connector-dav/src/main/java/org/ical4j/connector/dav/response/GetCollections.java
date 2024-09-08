package org.ical4j.connector.dav.response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.ical4j.connector.dav.ResourceType;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handle a DAV response by extracting identified collections and their associated properties.
 */
public class GetCollections extends AbstractResponseHandler<Map<String, DavPropertySet>> {

    private final List<String> resourceTypes;

    public GetCollections(ResourceType...type) {
        this.resourceTypes = Arrays.stream(type).map(ResourceType::description).collect(Collectors.toList());
    }

    @Override
    public Map<String, DavPropertySet> handleResponse(HttpResponse response) {
        try {
            MultiStatus multiStatus = getMultiStatus(response);
            return Arrays.stream(multiStatus.getResponses())
                    .filter(msr -> resourceTypes.containsAll(
                            (List<Element>) msr.getProperties(HttpStatus.SC_OK).get(DavPropertyName.RESOURCETYPE).getValue()))
                    .collect(Collectors.toMap(MultiStatusResponse::getHref,
                            msr -> msr.getProperties(HttpStatus.SC_OK)));
        } catch (DavException e) {
            throw new RuntimeException(e);
        }
    }
}

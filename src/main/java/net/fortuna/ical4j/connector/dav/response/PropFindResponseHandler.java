package net.fortuna.ical4j.connector.dav.response;

import net.fortuna.ical4j.connector.FailedOperationException;
import net.fortuna.ical4j.connector.dav.DavConstants;
import net.fortuna.ical4j.connector.dav.ResponseHandler;
import net.fortuna.ical4j.connector.dav.enums.ResourceType;
import net.fortuna.ical4j.connector.dav.enums.SupportedFeature;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.jackrabbit.webdav.*;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropFindResponseHandler implements ResponseHandler {

    private final HttpPropfind propfind;

    private HttpResponse httpResponse;

    public PropFindResponseHandler(HttpPropfind propfind) {
        this.propfind = propfind;
    }

    public DavPropertySet getPropertySet() {
        if (httpResponse.getStatusLine().getStatusCode() == DavServletResponse.SC_MULTI_STATUS) {
            try {
                MultiStatus multiStatus = propfind.getResponseBodyAsMultiStatus(httpResponse);
                MultiStatusResponse[] responses = multiStatus.getResponses();

                for (MultiStatusResponse msResponse : responses) {
                    return msResponse.getProperties(200);
                }
            } catch (DavException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public List<SupportedFeature> getSupportedFeatures() throws FailedOperationException {
        List<SupportedFeature> supportedFeatures = new ArrayList<>();
        if (httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new FailedOperationException(String.format("Principals not found"));
        } else {
            Header[] davHeaders = httpResponse.getHeaders(net.fortuna.ical4j.connector.dav.DavConstants.HEADER_DAV);
            for (Header header : davHeaders) {
                HeaderElement[] elements = header.getElements();
                for (HeaderElement element : elements) {
                    String feature = element.getName();
                    if (feature != null) {
                        SupportedFeature supportedFeature = SupportedFeature.findByDescription(feature);
                        if (supportedFeature != null) {
                            supportedFeatures.add(supportedFeature);
                        }
                    }
                }
            }
        }
        return supportedFeatures;
    }

    /**
     * Get the list of collections from a MultiStatus (HTTP 207 status code) response and populate the list of
     * properties of each collection.
     */
    public Map<String, DavPropertySet> getCollections(List<ResourceType> types) throws DavException {

        /*
         * TODO: supported features can be different on collections than the store, we should
         * check the headers and store the supported features per collection when we fetch them
         */
        MultiStatus multiStatus = propfind.getResponseBodyAsMultiStatus(httpResponse);
        MultiStatusResponse[] responses = multiStatus.getResponses();

        Map<String, DavPropertySet> collections = new HashMap<>();

        for (MultiStatusResponse msResponse : responses) {
            DavPropertySet foundProperties = msResponse.getProperties(200);
            String collectionUri = msResponse.getHref();

            for (int j = 0; j < msResponse.getStatus().length; j++) {
                if (msResponse.getStatus()[j].getStatusCode() == 200) {
                    boolean isCollection = false;
                    DavPropertySet _properties = new DavPropertySet();
                    for (DavPropertyIterator iNames = foundProperties.iterator(); iNames.hasNext(); ) {
                        DavProperty<?> property = iNames.nextProperty();
                        if (property != null) {
                            _properties.add(property);
                            List<ResourceType> resourceTypes = getResourceTypes(property);
                            for (ResourceType resourceType : resourceTypes) {
                                if (types.contains(resourceType)) {
                                    isCollection = true;
                                }
                            }
                        }
                    }
                    if (isCollection) {
                        collections.put(collectionUri, _properties);
                    }
                }
            }
        }

        return collections;
    }

    private List<ResourceType> getResourceTypes(DavProperty property) {
        List<ResourceType> resourceTypes = new ArrayList<>();
        if ((DavConstants.PROPERTY_RESOURCETYPE.equals(property.getName().getName())) && (DavConstants.NAMESPACE.equals(property.getName().getNamespace()))) {
            Object value = property.getValue();
            if (value instanceof ArrayList) {
                for (Node child : (ArrayList<Node>) value) {
                    if (child instanceof Element) {
                        String nameNode = child.getLocalName();
                        if (nameNode != null) {
                            resourceTypes.add(ResourceType.findByDescription(nameNode));
                        }
                    }
                }
            }
        }
        return resourceTypes;
    }

    public String getDavPropertyUri(DavPropertyName type) throws DavException {
        MultiStatus multiStatus = propfind.getResponseBodyAsMultiStatus(httpResponse);
        MultiStatusResponse[] responses = multiStatus.getResponses();
        for (MultiStatusResponse respons : responses) {
            for (int j = 0; j < respons.getStatus().length; j++) {
                Status status = respons.getStatus()[j];
                for (DavPropertyIterator iNames = respons.getProperties(status.getStatusCode()).iterator(); iNames
                        .hasNext(); ) {
                    DavProperty<?> name = iNames.nextProperty();
                    if ((name.getName().getName().equals(type.getName()))
                            && (type.getNamespace().isSame(name.getName()
                            .getNamespace().getURI()))) {
                        if (name.getValue() instanceof ArrayList) {
                            for (Object child : (ArrayList<?>) name.getValue()) {
                                if (child instanceof Element) {
                                    String calendarHomeSetUri = ((Element) child).getTextContent();
                                    /*
                                     * If the trailing slash is not there, CalendarServer will return a 301 status code
                                     * and we will get a nice DavException with "Moved Permanently" as the error
                                     */
                                    if (!(calendarHomeSetUri.endsWith("/"))) {
                                        calendarHomeSetUri += "/";
                                    }
                                    return calendarHomeSetUri;
                                }
                            }
                        }
                        /*
                         * This is for Kerio Mail Server implementation...
                         */
                        if (name.getValue() instanceof Node) {
                            Node child = (Node) name.getValue();
                            if (child instanceof Element) {
                                String calendarHomeSetUri = ((Element) child).getTextContent();
                                /*
                                 * If the trailing slash is not there, CalendarServer will return a 301 status code and
                                 * we will get a nice DavException with "Moved Permanently" as the error
                                 */
                                if (!(calendarHomeSetUri.endsWith("/"))) {
                                    calendarHomeSetUri += "/";
                                }
                                return calendarHomeSetUri;
                            }
                        }
                        if (name.getValue() instanceof String) {
                            String calendarHomeSetUri = (String) name.getValue();
                            if (!(calendarHomeSetUri.endsWith("/"))) {
                                calendarHomeSetUri += "/";
                            }
                            return calendarHomeSetUri;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean succeeded() {
        return propfind.succeeded(httpResponse);
    }

    public boolean exists() {
        switch (httpResponse.getStatusLine().getStatusCode()) {
            case DavServletResponse.SC_MULTI_STATUS:
            case DavServletResponse.SC_OK:
                return true;
            case DavServletResponse.SC_NOT_FOUND:
            default:
                return false;
        }
    }

    @Override
    public void accept(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }
}

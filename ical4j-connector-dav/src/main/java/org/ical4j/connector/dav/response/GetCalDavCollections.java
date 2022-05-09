package org.ical4j.connector.dav.response;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.jackrabbit.webdav.*;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.ical4j.connector.dav.CalDavCalendarCollection;
import org.ical4j.connector.dav.property.CSDavPropertyName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetCalDavCollections extends AbstractResponseHandler<List<CalDavCalendarCollection>> {

    @Override
    public List<CalDavCalendarCollection> handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        List<CalDavCalendarCollection> collections = new ArrayList<CalDavCalendarCollection>();
        try {
            MultiStatus multiStatus = getMultiStatus(response);
            MultiStatusResponse[] responses = multiStatus.getResponses();
            for (MultiStatusResponse msr : responses) {
                DavPropertySet properties = msr.getProperties(DavServletResponse.SC_OK);
                DavProperty<?> writeForProperty = properties.get(CSDavPropertyName.PROPERTY_PROXY_WRITE_FOR,
                        CSDavPropertyName.NAMESPACE);
                List<CalDavCalendarCollection> writeCollections = getDelegateCollections(writeForProperty);
                for (CalDavCalendarCollection writeCollection : writeCollections) {
                    writeCollection.setReadOnly(false);
                    collections.add(writeCollection);
                }
                DavProperty<?> readForProperty = properties.get(CSDavPropertyName.PROPERTY_PROXY_READ_FOR,
                        CSDavPropertyName.NAMESPACE);
                List<CalDavCalendarCollection> readCollections = getDelegateCollections(readForProperty);
                for (CalDavCalendarCollection readCollection : readCollections) {
                    readCollection.setReadOnly(true);
                    collections.add(readCollection);
                }
            }
        } catch (DavException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return collections;
    }

    @SuppressWarnings("unchecked")
    protected List<CalDavCalendarCollection> getDelegateCollections(DavProperty<?> proxyDavProperty)
            throws ParserConfigurationException, IOException, DavException {

        List<CalDavCalendarCollection> delegatedCollections = new ArrayList<CalDavCalendarCollection>();

        /*
         * Zimbra check: Zimbra advertise calendar-proxy, but it will return 404 in propstat if Enable delegation for
         * Apple iCal CalDAV client is not enabled
         */
        if (proxyDavProperty != null) {
            Object propertyValue = proxyDavProperty.getValue();
            List<Node> response;

            if (propertyValue instanceof List) {
                response = (List<Node>) proxyDavProperty.getValue();
                if (response != null) {
                    for (Node objectInArray: response) {
                        if (objectInArray instanceof Element) {
                            DavProperty<?> newProperty = DefaultDavProperty
                                    .createFromXml((Element) objectInArray);
                            if ((newProperty.getName().getName().equals((DavConstants.XML_RESPONSE)))
                                    && (newProperty.getName().getNamespace().equals(DavConstants.NAMESPACE))) {
                                List<Node> responseChilds = (List<Node>) newProperty.getValue();
                                for (Node responseChild : responseChilds) {
                                    if (responseChild instanceof Element) {
                                        DavProperty<?> responseChildElement = DefaultDavProperty
                                                .createFromXml((Element) responseChild);
                                        if (responseChildElement.getName().getName().equals(DavConstants.XML_PROPSTAT)) {
                                            List<Node> propStatChilds = (List<Node>) responseChildElement
                                                    .getValue();
                                            for (Node propStatChild : propStatChilds) {
                                                if (propStatChild instanceof Element) {
                                                    DavProperty<?> propStatChildElement = DefaultDavProperty
                                                            .createFromXml((Element) propStatChild);
                                                    if (propStatChildElement.getName().getName()
                                                            .equals(DavConstants.XML_PROP)) {
                                                        List<Node> propChilds = (List<Node>) propStatChildElement
                                                                .getValue();
                                                        for (Node propChild : propChilds) {
                                                            if (propChild instanceof Element) {
                                                                DavProperty<?> propChildElement = DefaultDavProperty
                                                                        .createFromXml((Element) propChild);
                                                                if (propChildElement.getName().equals(
                                                                        SecurityConstants.PRINCIPAL_URL)) {
                                                                    List<Node> principalUrlChilds = (List<Node>) propChildElement
                                                                            .getValue();
                                                                    for (Node principalUrlChild : principalUrlChilds) {
                                                                        if (principalUrlChild instanceof Element) {
                                                                            DavProperty<?> principalUrlElement = DefaultDavProperty
                                                                                    .createFromXml((Element) principalUrlChild);
                                                                            if (principalUrlElement.getName().getName()
                                                                                    .equals(DavConstants.XML_HREF)) {
                                                                                String principalsUri = (String) principalUrlElement
                                                                                        .getValue();
                                                                                //XXX: Need to reimplement..
//                                                                                String urlForcalendarHomeSet = findCalendarHomeSet(getHostURL()
//                                                                                        + principalsUri);
//                                                                                delegatedCollections.addAll(getCollectionsForHomeSet(this,urlForcalendarHomeSet));
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return delegatedCollections;
    }
}

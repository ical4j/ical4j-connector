package org.ical4j.connector.dav.response;

import org.apache.http.HttpResponse;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.ical4j.connector.dav.CardDavCollection;
import org.ical4j.connector.dav.property.CSDavPropertyName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetCardDavCollections extends AbstractResponseHandler<List<CardDavCollection>> {

    @Override
    public List<CardDavCollection> handleResponse(HttpResponse response) throws IOException {
        List<CardDavCollection> collections = new ArrayList<CardDavCollection>();

        try {
            var multiStatus = getMultiStatus(response);
            var responses = multiStatus.getResponses();
            for (var msr : responses) {
                var properties = msr.getProperties(DavServletResponse.SC_OK);
                DavProperty<?> writeForProperty = properties.get(CSDavPropertyName.PROPERTY_PROXY_WRITE_FOR,
                        CSDavPropertyName.NAMESPACE);
                collections.addAll(getDelegateCollections(writeForProperty));
                DavProperty<?> readForProperty = properties.get(CSDavPropertyName.PROPERTY_PROXY_READ_FOR,
                        CSDavPropertyName.NAMESPACE);
                collections.addAll(getDelegateCollections(readForProperty));
            }
        } catch (DavException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return collections;
    }

    protected List<CardDavCollection> getDelegateCollections(DavProperty<?> proxyDavProperty)
            throws ParserConfigurationException, IOException, DavException {
        /*
         * Zimbra check: Zimbra advertise calendar-proxy, but it will return 404 in propstat if Enable delegation for
         * Apple iCal CardDav client is not enabled
         */
        if (proxyDavProperty != null) {
            var propertyValue = proxyDavProperty.getValue();
            List<Node> response;

            if (propertyValue instanceof List) {
                response = (List<Node>) proxyDavProperty.getValue();
                if (response != null) {
                    for (var objectInArray : response) {
                        if (objectInArray instanceof Element) {
                            DavProperty<?> newProperty = DefaultDavProperty
                                    .createFromXml((Element) objectInArray);
                            if ((newProperty.getName().getName().equals((DavConstants.XML_RESPONSE)))
                                    && (newProperty.getName().getNamespace().equals(DavConstants.NAMESPACE))) {
                                List<Node> responseChilds = (List<Node>) newProperty.getValue();
                                for (var responseChild : responseChilds) {
                                    if (responseChild instanceof Element) {
                                        DavProperty<?> responseChildElement = DefaultDavProperty
                                                .createFromXml((Element) responseChild);
                                        if (responseChildElement.getName().getName().equals(DavConstants.XML_PROPSTAT)) {
                                            List<Node> propStatChilds = (List<Node>) responseChildElement
                                                    .getValue();
                                            for (var propStatChild : propStatChilds) {
                                                if (propStatChild instanceof Element) {
                                                    DavProperty<?> propStatChildElement = DefaultDavProperty
                                                            .createFromXml((Element) propStatChild);
                                                    if (propStatChildElement.getName().getName()
                                                            .equals(DavConstants.XML_PROP)) {
                                                        List<Node> propChilds = (List<Node>) propStatChildElement
                                                                .getValue();
                                                        for (var propChild : propChilds) {
                                                            if (propChild instanceof Element) {
                                                                DavProperty<?> propChildElement = DefaultDavProperty
                                                                        .createFromXml((Element) propChild);
                                                                if (propChildElement.getName().equals(
                                                                        SecurityConstants.PRINCIPAL_URL)) {
                                                                    List<Node> principalUrlChilds = (List<Node>) propChildElement
                                                                            .getValue();
                                                                    for (var principalUrlChild : principalUrlChilds) {
                                                                        if (principalUrlChild instanceof Element) {
                                                                            DavProperty<?> principalUrlElement = DefaultDavProperty
                                                                                    .createFromXml((Element) principalUrlChild);
                                                                            if (principalUrlElement.getName().getName()
                                                                                    .equals(DavConstants.XML_HREF)) {
                                                                                var principalsUri = (String) principalUrlElement
                                                                                        .getValue();
                                                                                //XXX: need to reimplement
//                                                                                String urlForcalendarHomeSet = findAddressBookHomeSet(getHostURL()
//                                                                                        + principalsUri);
//                                                                                return getCollectionsForHomeSet(this,
//                                                                                        urlForcalendarHomeSet);
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
            } else if (propertyValue instanceof Element) {
                System.out.println(((Element)propertyValue).getNodeName());
                System.out.println(((Element)propertyValue).getChildNodes());
            }
        }
        return new ArrayList<>();
    }
}

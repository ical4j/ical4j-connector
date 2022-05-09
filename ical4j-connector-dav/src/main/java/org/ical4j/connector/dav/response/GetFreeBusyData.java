package org.ical4j.connector.dav.response;

import net.fortuna.ical4j.data.ParserException;
import org.apache.http.HttpResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.ical4j.connector.dav.ScheduleResponse;
import org.ical4j.connector.dav.property.CalDavPropertyName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetFreeBusyData extends AbstractResponseHandler<List<ScheduleResponse>> {

    @Override
    public List<ScheduleResponse> handleResponse(HttpResponse response) throws IOException {
        List<ScheduleResponse> responses = new ArrayList<>();
        try {
            Document xmlDoc = DomUtil.parseDocument(response.getEntity().getContent());
            NodeList nodes = xmlDoc.getElementsByTagNameNS(CalDavPropertyName.NAMESPACE.getURI(),
                    DavPropertyName.XML_RESPONSE);
            for (int nodeItr = 0; nodeItr < nodes.getLength(); nodeItr++) {
                responses.add(new ScheduleResponse((Element) nodes.item(nodeItr)));
            }
        } catch (ParserConfigurationException | SAXException | ParserException e) {
            throw new RuntimeException(e);
        }
        return responses;
    }
}

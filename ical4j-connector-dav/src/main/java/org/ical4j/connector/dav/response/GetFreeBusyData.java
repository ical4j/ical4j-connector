package org.ical4j.connector.dav.response;

import net.fortuna.ical4j.data.ParserException;
import org.apache.http.HttpResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.ical4j.connector.dav.ScheduleResponse;
import org.ical4j.connector.dav.property.CalDavPropertyName;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the response for retrieving free/busy data from a CalDAV server.
 * This class processes the HTTP response to extract free/busy schedule responses
 * and returns them as a list of ScheduleResponse objects.
 */
public class GetFreeBusyData extends AbstractResponseHandler<List<ScheduleResponse>> {

    @Override
    public List<ScheduleResponse> handleResponse(HttpResponse response) throws IOException {
        List<ScheduleResponse> responses = new ArrayList<>();
        try {
            var xmlDoc = DomUtil.parseDocument(response.getEntity().getContent());
            var nodes = xmlDoc.getElementsByTagNameNS(CalDavPropertyName.NAMESPACE.getURI(),
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

package org.ical4j.connector.dav.response;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import org.apache.http.HttpResponse;
import org.ical4j.connector.MediaType;

import java.io.IOException;
import java.io.InputStream;

public class GetVCardResource extends AbstractResponseHandler<VCard> {

    @Override
    public VCard handleResponse(HttpResponse response) throws IOException {
        InputStream content = getContent(response, MediaType.VCARD_4_0);
        if (content != null) {
            try {
                VCardBuilder builder = new VCardBuilder(content);
                return builder.build();
            } catch (ParserException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}

package net.fortuna.ical4j.connector.dav;

import net.fortuna.ical4j.connector.FailedOperationException;
import net.fortuna.ical4j.connector.dav.response.GetVCardResource;
import net.fortuna.ical4j.vcard.VCard;

import java.io.IOException;

public interface CardDavSupport extends WebDavSupport {

    /**
     * Save card data.
     * @param uri
     * @param card
     * @throws IOException
     */
    void put(String uri, VCard card, String etag) throws IOException, FailedOperationException;

    default VCard getVCard(String path) throws IOException {
        return get(path, new GetVCardResource());
    }
}

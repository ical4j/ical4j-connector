package org.ical4j.connector.dav;

import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.dav.response.GetVCardResource;

import java.io.IOException;

/**
 * CardDavSupport provides methods for managing VCard objects in a CardDAV collection.
 * It extends WebDavSupport to include operations specific to CardDAV, such as saving and retrieving VCard data.
 *
 * @see WebDavSupport
 */
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

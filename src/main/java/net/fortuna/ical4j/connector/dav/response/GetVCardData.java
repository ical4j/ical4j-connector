package net.fortuna.ical4j.connector.dav.response;

import net.fortuna.ical4j.connector.dav.property.CardDavPropertyName;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GetVCardData extends AbstractResponseHandler<List<VCard>> {

    @Override
    public List<VCard> handleResponse(HttpResponse response) {
        try {
            MultiStatus multiStatus = getMultiStatus(response);
            return Arrays.stream(multiStatus.getResponses())
                    .filter(msr -> msr.getProperties(HttpStatus.SC_OK).get(CardDavPropertyName.ADDRESS_DATA) != null)
                    .map(msr -> {
                        String value = (String) msr.getProperties(HttpStatus.SC_OK).get(CardDavPropertyName.ADDRESS_DATA).getValue();
                        try {
                            return new VCardBuilder(new StringReader(value)).build();
                        } catch (IOException | ParserException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());
        } catch (DavException e) {
            throw new RuntimeException(e);
        }
    }
}

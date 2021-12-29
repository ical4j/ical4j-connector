package net.fortuna.ical4j.connector.dav.response;

import net.fortuna.ical4j.connector.MediaType;
import org.apache.http.*;
import org.apache.http.client.ResponseHandler;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractResponseHandler<T> implements ResponseHandler<T> {

    protected InputStream getContent(HttpResponse response, MediaType mediaType) throws IOException {
        HttpEntity httpEntity = response.getEntity();
        if (httpEntity != null && httpEntity.getContentType().getValue().startsWith(mediaType.getContentType())) {
            return httpEntity.getContent();
        }
        return null;
    }

    protected <R> List<R> getHeaders(HttpResponse response, String name, Function<Header, R> mapper) {
        return Arrays.stream(response.getHeaders(name)).map(mapper).collect(Collectors.toList());
    }

    protected <R> List<R> getHeaderElements(HttpResponse response, String name, Function<HeaderElement, R> mapper) {
        return Arrays.stream(response.getHeaders(name)).flatMap(h -> Arrays.stream(h.getElements())).map(mapper)
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Document getResponseBodyAsDocument(HttpEntity entity) throws IOException {

        if (entity == null) {
            return null;
        } else {
            // read response and try to build a xml document
            InputStream in = entity.getContent();
            try {
                return DomUtil.parseDocument(in);
            } catch (ParserConfigurationException ex) {
                throw new IOException("XML parser configuration error", ex);
            } catch (SAXException ex) {
                throw new IOException("XML parsing error", ex);
            } finally {
                in.close();
            }
        }
    }

    public MultiStatus getResponseBodyAsMultiStatus(HttpResponse response) throws DavException {
        try {
            Document doc = getResponseBodyAsDocument(response.getEntity());
            if (doc == null) {
                throw new DavException(response.getStatusLine().getStatusCode(), "no response body");
            }
            return MultiStatus.createFromXml(doc.getDocumentElement());
        } catch (IOException ex) {
            throw new DavException(response.getStatusLine().getStatusCode(), ex);
        }
    }

    protected MultiStatus getMultiStatus(HttpResponse response) throws DavException {
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_MULTI_STATUS) {
            throw new RuntimeException("Unexpected status code");
        }
        return getResponseBodyAsMultiStatus(response);
    }
}

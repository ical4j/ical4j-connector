package org.ical4j.connector.dav;

import org.apache.jackrabbit.webdav.*;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

import java.net.MalformedURLException;

class CalDavResourceFactory implements DavResourceFactory {

    private DavClientFactory clientFactory;

    public CalDavResourceFactory() {
        this(new DavClientFactory().withPreemptiveAuth(true).withFollowRedirects(true));
    }

    public CalDavResourceFactory(DavClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public DavResource createResource(DavResourceLocator locator, DavServletRequest request,
                                      DavServletResponse response) throws DavException {

        try {
            return new CalDavResource(this, locator, new DavPropertySet(),
                    clientFactory.newInstance(locator.getRepositoryPath()), null);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DavResource createResource(DavResourceLocator locator, DavSession session) throws DavException {
        try {
            return new CalDavResource(this, locator, new DavPropertySet(),
                    clientFactory.newInstance(locator.getRepositoryPath()), null);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}

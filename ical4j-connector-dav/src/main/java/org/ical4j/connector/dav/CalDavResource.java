package org.ical4j.connector.dav;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

import java.io.IOException;

public class CalDavResource extends AbstractDavResource<CalDavSupport> {

    public CalDavResource(DavResourceFactory factory, DavResourceLocator locator, DavPropertySet properties,
                          CalDavSupport client, CalDavResource parent) {

        super(factory, locator, properties, client, parent);
    }

    @Override
    public void spool(OutputContext outputContext) throws IOException {

    }

    @Override
    public void addMember(DavResource resource, InputContext inputContext) throws DavException {

    }

    @Override
    public void removeMember(DavResource member) throws DavException {

    }
}

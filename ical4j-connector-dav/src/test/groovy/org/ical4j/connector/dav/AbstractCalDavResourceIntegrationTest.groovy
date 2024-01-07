package org.ical4j.connector.dav


import org.apache.http.client.CredentialsProvider
import org.apache.jackrabbit.webdav.property.DavPropertyName
import org.apache.jackrabbit.webdav.property.DavPropertySet

abstract class AbstractCalDavResourceIntegrationTest extends AbstractIntegrationTest {

    abstract String getPathPrefix();

    abstract PathResolver getPathResolver();

    abstract CredentialsProvider getCredentialsProvider()

    def 'test get property names'() {
        given: 'a dav client'
        CalDavLocatorFactory locatorFactory = [getPathPrefix(), getPathResolver()]
        DefaultDavClient client = new DefaultDavClient(getContainerUrl(), new DavClientConfiguration())
        client.begin(getCredentialsProvider())
        
        and: 'a caldav resource'
        CalDavResourceFactory resourceFactory = []
        CalDavResource resource = [resourceFactory, locatorFactory.createResourceLocator('', getContainerUrl() + '/newcol'),
                                   new DavPropertySet(), client, null]

        when: 'property names are requested'
        DavPropertyName[] propertyNames = resource.getPropertyNames()

        then: 'the result is as expected'
        propertyNames.length > 0
    }
}

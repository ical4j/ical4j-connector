package org.ical4j.connector.dav;

import org.apache.jackrabbit.webdav.AbstractLocatorFactory;

public class CalDavLocatorFactory extends AbstractLocatorFactory {

    private final PathResolver pathResolver;

    public CalDavLocatorFactory(String pathPrefix, PathResolver pathResolver) {
        super(pathPrefix);
        this.pathResolver = pathResolver;
    }

    @Override
    protected String getRepositoryPath(String resourcePath, String wspPath) {
        return pathResolver.getRepositoryPath(resourcePath, wspPath);
    }

    @Override
    protected String getResourcePath(String repositoryPath, String wspPath) {
        return pathResolver.getResourceName(repositoryPath, wspPath);
    }
}

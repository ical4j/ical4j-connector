package org.ical4j.connector.dav;

import org.apache.jackrabbit.webdav.AbstractLocatorFactory;

public class CalDavLocatorFactory extends AbstractLocatorFactory {

    private final PathResolver pathResolver;

    public CalDavLocatorFactory(PathResolver pathResolver) {
        super(pathResolver.getRootPath());
        this.pathResolver = pathResolver;
    }

    @Override
    protected String getRepositoryPath(String resourcePath, String wspPath) {
        return pathResolver.getRepositoryRoot(resourcePath, wspPath);
    }

    @Override
    protected String getResourcePath(String repositoryPath, String wspPath) {
        return pathResolver.getResourcePath(repositoryPath, wspPath);
    }
}

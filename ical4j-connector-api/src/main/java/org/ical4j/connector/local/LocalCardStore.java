package org.ical4j.connector.local;

import org.ical4j.connector.ObjectStore;

import java.io.File;
import java.io.IOException;

/**
 * LocalCardStore is a concrete implementation of ObjectStore for managing vCard objects
 * stored in a local file system. It extends AbstractLocalObjectStore to provide basic functionality
 * for storing and retrieving vCard objects.
 */
public class LocalCardStore extends AbstractLocalObjectStore<LocalCardCollection>
        implements ObjectStore<LocalCardCollection> {

    public LocalCardStore(File root) {
        super(root);
    }

    @Override
    protected LocalCardCollection newCollection(String id, String workspace) throws IOException {
        return new LocalCardCollection(new File(getWorkspaceDir(workspace), id));
    }
}

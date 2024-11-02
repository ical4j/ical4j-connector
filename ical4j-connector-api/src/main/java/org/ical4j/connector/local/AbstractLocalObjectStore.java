package org.ical4j.connector.local;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.AbstractObjectStore;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

abstract class AbstractLocalObjectStore<T, C extends AbstractLocalObjectCollection<T>> extends AbstractObjectStore<T, C> {

    private final File root;

    AbstractLocalObjectStore(File root) {
        this.root = Objects.requireNonNull(root);
        if (root.exists() && !root.isDirectory()) {
            throw new IllegalArgumentException("Root must be a directory");
        } else if (!root.exists() && !root.mkdirs()) {
            throw new IllegalArgumentException("Unable to initialise root directory");
        }
    }

    protected File getRoot() {
        return root;
    }

    @Override
    public boolean connect() throws ObjectStoreException {
        return false;
    }

    @Override
    public boolean connect(String username, char[] password) throws ObjectStoreException {
        return false;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public C addCollection(String id) throws ObjectStoreException {
        return addCollection(id, DEFAULT_WORKSPACE);
    }

    @Override
    public C addCollection(String id, String workspace) throws ObjectStoreException {
        var collectionDir = new File(getWorkspaceDir(workspace), id);
        if ((collectionDir.exists() && !collectionDir.isDirectory()) ||
                (!collectionDir.exists() && !collectionDir.mkdirs())) {
            throw new ObjectStoreException("Unable to initialise collection");
        }
        C collection = null;
        try {
            collection = getCollection(id);
        } catch (ObjectNotFoundException e) {
            try {
                collection = newCollection(id, workspace);
            } catch (IOException ex) {
                throw new ObjectStoreException(ex);
            }
        }

        // notify listeners..
        fireOnAddEvent(this, collection);

        return collection;
    }

    protected abstract C newCollection(String id, String workspace) throws IOException;

    @Override
    public C addCollection(String id, String displayName, String description, String[] supportedComponents, Calendar timezone) throws ObjectStoreException {
        return addCollection(id, displayName, description, supportedComponents, timezone, DEFAULT_WORKSPACE);
    }

    @Override
    public C addCollection(String id, String displayName, String description, String[] supportedComponents,
                           Calendar timezone, String workspace) throws ObjectStoreException {
        C collection = addCollection(id, workspace);
        try {
            collection.setDisplayName(displayName);
            collection.setDescription(description);
            collection.setSupportedComponents(supportedComponents);
            collection.setTimeZone(timezone);
        } catch (IOException e) {
            throw new ObjectStoreException(e);
        }

        // notify listeners..
        fireOnAddEvent(this, collection);

        return collection;
    }

    @Override
    public C removeCollection(String id) throws ObjectNotFoundException, ObjectStoreException {
        C collection = getCollection(id);
        collection.delete();

        // notify listeners..
        fireOnRemoveEvent(this, collection);

        return collection;
    }

    @Override
    public C getCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        return getCollection(id, DEFAULT_WORKSPACE);
    }

    @Override
    public C getCollection(String id, String workspace) throws ObjectStoreException, ObjectNotFoundException {
        var collectionDir = new File(getWorkspaceDir(workspace), id);
        if (!collectionDir.exists() || !collectionDir.isDirectory()) {
            throw new ObjectNotFoundException("Unable to retrieve collection");
        }
        try {
            return newCollection(id, workspace);
        } catch (IOException e) {
            throw new ObjectStoreException(e);
        }
    }

    @Override
    public List<C> getCollections() {
        return getCollections(DEFAULT_WORKSPACE);
    }

    @Override
    public List<C> getCollections(String workspace) {
        return Arrays.stream(Objects.requireNonNull(getWorkspaceDir(workspace).list())).map(name -> {
            try {
                return newCollection(name, workspace);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    protected File getWorkspaceDir(String workspace) {
        var workspaceDir = new File(root, workspace);
        if (!workspaceDir.exists() && !workspaceDir.mkdir()) {
            throw new IllegalArgumentException("Invalid workspace");
        }
        return workspaceDir;
    }

    @Override
    public List<String> listWorkspaceIds() {
        return Arrays.asList(Objects.requireNonNull(root.list()));
    }
}

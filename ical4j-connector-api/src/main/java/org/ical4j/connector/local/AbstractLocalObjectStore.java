package org.ical4j.connector.local;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract  class AbstractLocalObjectStore<C extends AbstractLocalObjectCollection<?>> implements ObjectStore<C> {

    private final File root;

    AbstractLocalObjectStore(File root) {
        if (root.exists() && !root.isDirectory()) {
            throw new IllegalArgumentException("Root must be a directory");
        }
        this.root = root;
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
        File collectionDir = new File(root, id);
        if ((collectionDir.exists() && !collectionDir.isDirectory()) ||
                (!collectionDir.exists() && !collectionDir.mkdirs())) {
            throw new ObjectStoreException("Unable to initialise collection");
        }
        C collection = null;
        try {
            collection = getCollection(id);
        } catch (ObjectNotFoundException e) {
            try {
                collection = newCollection(id);
            } catch (IOException ex) {
                throw new ObjectStoreException(ex);
            }
        }
        return collection;
    }

    protected abstract C newCollection(String id) throws IOException;

    @Override
    public C addCollection(String id, String displayName, String description, String[] supportedComponents, Calendar timezone) throws ObjectStoreException {
        C collection = addCollection(id);
        try {
            collection.setDisplayName(displayName);
            collection.setDescription(description);
            collection.setSupportedComponents(supportedComponents);
            collection.setTimeZone(timezone);
        } catch (IOException e) {
            throw new ObjectStoreException(e);
        }
        return collection;
    }

    @Override
    public C removeCollection(String id) {
        return null;
    }

    @Override
    public C getCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        File collectionDir = new File(root, id);
        if (!collectionDir.exists() || !collectionDir.isDirectory()) {
            throw new ObjectNotFoundException("Unable to retrieve collection");
        }
        try {
            return newCollection(id);
        } catch (IOException e) {
            throw new ObjectStoreException(e);
        }
    }

    @Override
    public List<C> getCollections() {
        return Arrays.stream(root.list()).map(name -> {
            try {
                return newCollection(name);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }
}

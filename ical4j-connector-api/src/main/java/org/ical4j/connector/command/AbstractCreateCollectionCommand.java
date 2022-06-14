package org.ical4j.connector.command;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;

public abstract class AbstractCreateCollectionCommand<T extends ObjectCollection<?>> implements Runnable {

    private final ObjectStore<T> store;

    private String collectionName;

    private T collection;

    public AbstractCreateCollectionCommand(ObjectStore<T> store) {
        this.store = store;
    }

    public AbstractCreateCollectionCommand<T> withCollectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    public T getCollection() {
        return collection;
    }

    @Override
    public void run() {
        try {
            this.collection = store.addCollection(collectionName);
        } catch (ObjectStoreException e) {
            throw new RuntimeException(e);
        }
    }
}

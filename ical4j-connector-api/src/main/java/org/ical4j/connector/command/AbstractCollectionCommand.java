package org.ical4j.connector.command;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;

import java.util.function.Consumer;

public abstract class AbstractCollectionCommand<T extends ObjectCollection<?>, R> extends AbstractStoreCommand<T, R> {

    private final String collectionName;

    public AbstractCollectionCommand(String collectionName, Consumer<R> consumer) {
        super(consumer);
        this.collectionName = collectionName;
    }

    public AbstractCollectionCommand(String collectionName, Consumer<R> consumer, ObjectStore<T> store) {
        super(consumer, store);
        this.collectionName = collectionName;
    }

    public T getCollection() throws ObjectStoreException, ObjectNotFoundException {
        return getStore().getCollection(collectionName);
    }
}

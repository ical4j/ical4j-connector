package org.ical4j.connector.command;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.util.function.Consumer;

/**
 * Subclasses perform operations in the context of a specific calendar or card collection.
 *
 * @param <T> the supported collection type for a configured data store
 * @param <R> the command result consumer
 */
public abstract class AbstractCollectionCommand<T extends ObjectCollection<?>, R> extends AbstractStoreCommand<T, R> {

    @CommandLine.Option(names = {"-collection"})
    private String collectionName;

    public AbstractCollectionCommand() {
        this(ObjectCollection.DEFAULT_COLLECTION);
    }

    public AbstractCollectionCommand(String collectionName) {
        this.collectionName = collectionName;
    }

    public AbstractCollectionCommand(String collectionName, Consumer<R> consumer) {
        super(consumer);
        this.collectionName = collectionName;
    }

    public AbstractCollectionCommand(String collectionName, ObjectStore<T> store) {
        super(store);
        this.collectionName = collectionName;
    }

    public AbstractCollectionCommand(String collectionName, Consumer<R> consumer, ObjectStore<T> store) {
        super(consumer, store);
        this.collectionName = collectionName;
    }

    public AbstractCollectionCommand<T, R> withCollectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    public T getCollection() throws ObjectStoreException, ObjectNotFoundException {
        return getStore().getCollection(collectionName);
    }
}

package org.ical4j.connector.command;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreFactory;

import java.util.function.Consumer;

/**
 * Subclasses provide functionality that requires data store connectivity.
 *
 * @param <T> the supported collection type for a configured data store
 * @param <R> the command result consumer
 */
public abstract class AbstractStoreCommand<T extends ObjectCollection<?>, R> extends AbstractCommand<R> {

    private final ObjectStore<T> store;

    public AbstractStoreCommand() {
        this.store = new ObjectStoreFactory().newInstance();
    }

    public AbstractStoreCommand(ObjectStore<T> store) {
        this.store = store;
    }

    public AbstractStoreCommand(Consumer<R> consumer) {
        super(consumer);
        this.store = new ObjectStoreFactory().newInstance();
    }

    public AbstractStoreCommand(Consumer<R> consumer, ObjectStore<T> store) {
        super(consumer);
        this.store = store;
    }

    public ObjectStore<T> getStore() {
        return store;
    }
}

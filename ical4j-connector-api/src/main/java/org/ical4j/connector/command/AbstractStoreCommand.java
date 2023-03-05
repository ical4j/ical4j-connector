package org.ical4j.connector.command;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreFactory;

import java.util.function.Consumer;

public abstract class AbstractStoreCommand<T extends ObjectCollection<?>, R> extends AbstractCommand<R> {

    private final ObjectStore<T> store;

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

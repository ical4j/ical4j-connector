package org.ical4j.connector.command;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreFactory;

public abstract class AbstractCommand<T extends ObjectCollection<?>> implements Runnable {

    private final ObjectStore<T> store;

    public AbstractCommand() {
        this.store = new ObjectStoreFactory().newInstance();
    }

    public AbstractCommand(ObjectStore<T> store) {
        this.store = store;
    }

    public ObjectStore<T> getStore() {
        return store;
    }
}

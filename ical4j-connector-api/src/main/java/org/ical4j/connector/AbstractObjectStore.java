package org.ical4j.connector;

import org.ical4j.connector.event.ListenerList;
import org.ical4j.connector.event.ObjectStoreListener;

/**
 * Abstract base class for object stores.
 * Provides basic support for managing listeners.
 * @param <T>
 * @param <C>
 */
public abstract class AbstractObjectStore<T, C extends ObjectCollection<T>> implements ObjectStore<T, C> {

    /**
     * Registered event listeners.
     */
    private final ListenerList<ObjectStoreListener<T>> listenerList;

    public AbstractObjectStore() {
        this(new ListenerList<>());
    }

    public AbstractObjectStore(ListenerList<ObjectStoreListener<T>> listenerList) {
        this.listenerList = listenerList;
    }

    @Override
    public ListenerList<ObjectStoreListener<T>> getObjectStoreListeners() {
        return listenerList;
    }
}

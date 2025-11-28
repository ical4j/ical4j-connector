package org.ical4j.connector;

import org.ical4j.connector.event.ListenerList;
import org.ical4j.connector.event.ObjectStoreListener;

/**
 * Abstract base class for object stores.
 * Provides basic support for managing listeners.
 * @param <C>
 */
public abstract class AbstractObjectStore<C extends ObjectCollection<?>> implements ObjectStore<C> {

    /**
     * Registered event listeners.
     */
    private final ListenerList<ObjectStoreListener<C>> listenerList;

    public AbstractObjectStore() {
        this(new ListenerList<>());
    }

    public AbstractObjectStore(ListenerList<ObjectStoreListener<C>> listenerList) {
        this.listenerList = listenerList;
    }

    @Override
    public ListenerList<ObjectStoreListener<C>> getObjectStoreListeners() {
        return listenerList;
    }
}

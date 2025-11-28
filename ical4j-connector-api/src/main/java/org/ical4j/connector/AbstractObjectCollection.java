package org.ical4j.connector;

import org.ical4j.connector.event.ListenerList;
import org.ical4j.connector.event.ObjectCollectionListener;

/**
 * Provides basic support for object collections.
 * @param <T>
 */
public abstract class AbstractObjectCollection<T> implements ObjectCollection<T> {

    private final ListenerList<ObjectCollectionListener<T>> listenerList;

    public AbstractObjectCollection() {
        this(new ListenerList<>());
    }

    public AbstractObjectCollection(ListenerList<ObjectCollectionListener<T>> listenerList) {
        this.listenerList = listenerList;
    }

    @Override
    public ListenerList<ObjectCollectionListener<T>> getObjectCollectionListeners() {
        return listenerList;
    }
}

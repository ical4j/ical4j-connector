package org.ical4j.connector;

import org.ical4j.connector.event.ListenerList;
import org.ical4j.connector.event.ObjectStoreEvent;
import org.ical4j.connector.event.ObjectStoreListener;

/**
 * Provide support for notifying collection listeners of events.
 */
public interface ObjectStoreListenerSupport<T> {

    ListenerList<ObjectStoreListener<T>> getObjectStoreListeners();

    /**
     * Register listener for collection events.
     * @param listener a collection listener
     */
    default void addObjectStoreListener(ObjectStoreListener<T> listener) {
        getObjectStoreListeners().add(listener);
    }

    /**
     * Unregister listener for collection events.
     * @param listener a collection listener
     */
    default void removeObjectStoreListener(ObjectStoreListener<T> listener) {
        getObjectStoreListeners().remove(listener);
    }

    default void fireOnAddEvent(ObjectStore<T, ? extends ObjectCollection<T>> source, ObjectCollection<T> object) {
        getObjectStoreListeners().getAll().forEach(listener -> listener.collectionAdded(
                new ObjectStoreEvent<T>(source, object)));
    }

    default void fireOnRemoveEvent(ObjectStore<T, ? extends ObjectCollection<T>> source, ObjectCollection<T> object) {
        getObjectStoreListeners().getAll().forEach(listener -> listener.collectionRemoved(
                new ObjectStoreEvent<>(source, object)));
    }
}

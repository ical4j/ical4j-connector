package org.ical4j.connector;

import org.ical4j.connector.event.ListenerList;
import org.ical4j.connector.event.ObjectStoreEvent;
import org.ical4j.connector.event.ObjectStoreListener;

/**
 * Provide support for notifying collection listeners of events.
 */
public interface ObjectStoreListenerSupport<C extends ObjectCollection<?>> {

    ListenerList<ObjectStoreListener<C>> getObjectStoreListeners();

    /**
     * Register listener for collection events.
     * @param listener a collection listener
     */
    default void addObjectStoreListener(ObjectStoreListener<C> listener) {
        getObjectStoreListeners().add(listener);
    }

    /**
     * Unregister listener for collection events.
     * @param listener a collection listener
     */
    default void removeObjectStoreListener(ObjectStoreListener<C> listener) {
        getObjectStoreListeners().remove(listener);
    }

    default void fireOnAddEvent(ObjectStore<C> source, C collection) {
        getObjectStoreListeners().getAll().forEach(listener -> listener.collectionAdded(
                new ObjectStoreEvent<>(source, collection)));
    }

    default void fireOnRemoveEvent(ObjectStore<C> source, C collection) {
        getObjectStoreListeners().getAll().forEach(listener -> listener.collectionRemoved(
                new ObjectStoreEvent<>(source, collection)));
    }
}

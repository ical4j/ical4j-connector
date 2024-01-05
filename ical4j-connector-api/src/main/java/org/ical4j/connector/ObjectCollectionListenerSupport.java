package org.ical4j.connector;

import org.ical4j.connector.event.ListenerList;
import org.ical4j.connector.event.ObjectCollectionEvent;
import org.ical4j.connector.event.ObjectCollectionListener;

/**
 * Provide support for notifying collection listeners of events.
 */
public interface ObjectCollectionListenerSupport<T> {

    ListenerList<ObjectCollectionListener<T>> getObjectCollectionListeners();

    /**
     * Register listener for collection events.
     * @param listener a collection listener
     */
    default void addObjectCollectionListener(ObjectCollectionListener<T> listener) {
        getObjectCollectionListeners().add(listener);
    }

    /**
     * Unregister listener for collection events.
     * @param listener a collection listener
     */
    default void removeObjectCollectionListener(ObjectCollectionListener<T> listener) {
        getObjectCollectionListeners().remove(listener);
    }

    default void fireOnAddEvent(ObjectCollection<T> source, T object) {
        getObjectCollectionListeners().getAll().forEach(listener -> listener.onAdd(
                new ObjectCollectionEvent<>(source, object)));
    }

    default void fireOnRemoveEvent(ObjectCollection<T> source, T object) {
        getObjectCollectionListeners().getAll().forEach(listener -> listener.onRemove(
                new ObjectCollectionEvent<>(source, object)));
    }

    default void fireOnMergeEvent(ObjectCollection<T> source, T object) {
        getObjectCollectionListeners().getAll().forEach(listener -> listener.onMerge(
                new ObjectCollectionEvent<>(source, object)));
    }

    default void fireOnReplaceEvent(ObjectCollection<T> source, T object) {
        getObjectCollectionListeners().getAll().forEach(listener -> listener.onReplace(
                new ObjectCollectionEvent<>(source, object)));
    }
}

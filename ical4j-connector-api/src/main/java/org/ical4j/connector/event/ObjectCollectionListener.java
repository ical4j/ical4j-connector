package org.ical4j.connector.event;

/**
 * Listener interface for receiving events from an {@link ObjectCollection}.
 * Implementations of this interface can be registered to listen for changes
 * in the collection, such as additions, removals, merges, and replacements of objects.
 *
 * @param <T> the type of objects in the collection
 */
public interface ObjectCollectionListener<T> {

    void onAdd(ObjectCollectionEvent<T> event);

    void onRemove(ObjectCollectionEvent<T> event);

    void onMerge(ObjectCollectionEvent<T> event);

    void onReplace(ObjectCollectionEvent<T> event);
}

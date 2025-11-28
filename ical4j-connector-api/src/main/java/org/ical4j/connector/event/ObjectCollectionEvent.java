package org.ical4j.connector.event;

import org.ical4j.connector.ObjectCollection;

import java.util.EventObject;

/**
 * Represents an event that occurs in an {@link ObjectCollection}.
 * This event is used to notify listeners about changes to the collection,
 * such as the addition or removal of an object.
 *
 * @param <T> the type of objects in the collection
 */
public class ObjectCollectionEvent<T> extends EventObject {

    private final T object;

    public ObjectCollectionEvent(ObjectCollection<T> source, T object) {
        super(source);
        this.object = object;
    }

    public T getObject() {
        return object;
    }
}

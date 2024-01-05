package org.ical4j.connector.event;

import org.ical4j.connector.ObjectCollection;

import java.util.EventObject;

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

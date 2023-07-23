package org.ical4j.connector.event;

public interface ObjectCollectionListener<T> {

    void onAdd(ObjectCollectionEvent<T> event);

    void onRemove(ObjectCollectionEvent<T> event);

    void onMerge(ObjectCollectionEvent<T> event);

    void onReplace(ObjectCollectionEvent<T> event);
}

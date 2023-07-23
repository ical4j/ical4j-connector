package org.ical4j.connector.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ListenerList<T> {

    private final List<T> listeners;

    public ListenerList() {
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public boolean add(T listener) {
        return listeners.add(listener);
    }

    public boolean remove(T listener) {
        return listeners.remove(listener);
    }

    @SuppressWarnings("unchecked")
    public <R extends T> List<R> get(Class<R> type) {
        return (List<R>) listeners.stream().filter(type::isInstance).collect(Collectors.toList());
    }

    public List<T> getAll() {
        return listeners;
    }
}

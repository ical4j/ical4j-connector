package org.ical4j.connector.command;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.util.List;
import java.util.function.Consumer;

@CommandLine.Command(name = "list-collections", description = "List collections in an object store")
public class ListCollections extends AbstractStoreCommand<ObjectCollection<?>, List<ObjectCollection<?>>> {

    public ListCollections() {
        super(list -> {list.forEach(System.out::println);});
    }

    public ListCollections(Consumer<List<ObjectCollection<?>>> consumer) {
        super(consumer);
    }

    public ListCollections(Consumer<List<ObjectCollection<?>>> consumer, ObjectStore<ObjectCollection<?>> store) {
        super(consumer, store);
    }

    @Override
    public void run() {
        try {
            getConsumer().accept(getStore().getCollections());
        } catch (ObjectStoreException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

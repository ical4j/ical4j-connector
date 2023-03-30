package org.ical4j.connector.command;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.util.function.Consumer;

@CommandLine.Command(name = "delete-collection", description = "Purge a collection")
public class DeleteCollection extends AbstractStoreCommand<ObjectCollection<?>, ObjectCollection<?>> {

    @CommandLine.Option(names = {"-name"})
    private String collectionName;

    public DeleteCollection() {
        super();
    }

    public DeleteCollection(Consumer<ObjectCollection<?>> consumer) {
        super(consumer);
    }

    public DeleteCollection(Consumer<ObjectCollection<?>> consumer, ObjectStore<ObjectCollection<?>> store) {
        super(consumer, store);
    }

    public DeleteCollection withCollectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    @Override
    public void run() {
        try {
            getConsumer().accept(getStore().removeCollection(collectionName));
        } catch (ObjectStoreException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

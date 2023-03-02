package org.ical4j.connector.command;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.util.function.Consumer;

@CommandLine.Command(name = "get-collection", description = "Retrieve a collection")
public class GetCollectionDetails extends AbstractStoreCommand<ObjectCollection<?>, ObjectCollection<?>> {

    private String collectionName;

    public GetCollectionDetails() {
        super(collection -> {});
    }

    public GetCollectionDetails(Consumer<ObjectCollection<?>> consumer) {
        super(consumer);
    }

    public GetCollectionDetails(Consumer<ObjectCollection<?>> consumer, ObjectStore<ObjectCollection<?>> store) {
        super(consumer, store);
    }

    public GetCollectionDetails withCollectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    @Override
    public void run() {
        try {
            getConsumer().accept(getStore().getCollection(collectionName));
        } catch (ObjectStoreException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

package org.ical4j.connector.command;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.util.function.Consumer;

@CommandLine.Command(name = "create-collection", description = "Create a new collection")
public class CreateCollection extends AbstractStoreCommand<ObjectCollection<?>, ObjectCollection<?>> {

    @CommandLine.Option(names = {"-X", "--name"})
    private String collectionName;

    private String[] supportedComponents;

    public CreateCollection() {
        super(collection -> {});
    }

    public CreateCollection(Consumer<ObjectCollection<?>> consumer) {
        super(consumer);
    }

    public CreateCollection(Consumer<ObjectCollection<?>> consumer, ObjectStore<ObjectCollection<?>> store) {
        super(consumer, store);
    }

    public CreateCollection withCollectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    public CreateCollection withSupportedComponents(String...supportedComponents) {
        this.supportedComponents = supportedComponents;
        return this;
    }

    @Override
    public void run() {
        try {
            getConsumer().accept(getStore().addCollection(collectionName));
        } catch (ObjectStoreException e) {
            throw new RuntimeException(e);
        }
    }
}

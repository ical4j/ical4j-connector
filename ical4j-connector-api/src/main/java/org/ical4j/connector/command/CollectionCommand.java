package org.ical4j.connector.command;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.util.List;
import java.util.function.Consumer;

import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;
import static org.ical4j.connector.command.DefaultOutputHandlers.STDOUT_LIST_PRINTER;

@CommandLine.Command(name = "collection", description = "Command group for collection operations",
        subcommands = {CollectionCommand.GetCollectionDetails.class, CollectionCommand.ListCollections.class,
        CollectionCommand.CreateCollection.class, CollectionCommand.UpdateCollection.class,
                CollectionCommand.DeleteCollection.class, CollectionCommand.ListObjectUids.class},
        mixinStandardHelpOptions = true)
public class CollectionCommand {
    @CommandLine.Command(name = "create", description = "Create a new collection")
    public static class CreateCollection extends AbstractStoreCommand<ObjectCollection<?>, ObjectCollection<?>> {

        @CommandLine.Option(names = {"-name"})
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

    @CommandLine.Command(name = "delete", description = "Purge a collection")
    public static class DeleteCollection extends AbstractStoreCommand<ObjectCollection<?>, ObjectCollection<?>> {

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

    @CommandLine.Command(name = "get", description = "Retrieve a collection")
    public static class GetCollectionDetails extends AbstractStoreCommand<ObjectCollection<?>, ObjectCollection<?>> {

        @CommandLine.Option(names = {"-name"})
        private String collectionName;

        public GetCollectionDetails() {
            super();
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

    @CommandLine.Command(name = "list", description = "List collections in an object store")
    public static class ListCollections extends AbstractStoreCommand<ObjectCollection<?>, List<ObjectCollection<?>>> {

        public ListCollections() {
            super(STDOUT_LIST_PRINTER());
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

    @CommandLine.Command(name = "update", description = "Update an object collection")
    public static class UpdateCollection extends AbstractStoreCommand<ObjectCollection<?>, Void> {

        public UpdateCollection() {
            super();
        }

        public UpdateCollection(Consumer<Void> consumer) {
            super(consumer);
        }

        public UpdateCollection(Consumer<Void> consumer, ObjectStore<ObjectCollection<?>> store) {
            super(consumer, store);
        }

        @Override
        public void run() {

        }
    }

    @CommandLine.Command(name = "list-uids", description = "List object UIDs within a collection")
    public static class ListObjectUids extends AbstractCollectionCommand<ObjectCollection<?>, List<String>> {

        public ListObjectUids() {
            super(DEFAULT_COLLECTION, STDOUT_LIST_PRINTER());
        }

        public ListObjectUids(ObjectStore<ObjectCollection<?>> store) {
            super(DEFAULT_COLLECTION, STDOUT_LIST_PRINTER(), store);
        }

        public ListObjectUids(String collectionName, ObjectStore<ObjectCollection<?>> store) {
            super(collectionName, STDOUT_LIST_PRINTER(), store);
        }

        @Override
        public void run() {
            try {
                getConsumer().accept(getCollection().listObjectUids());
            } catch (ObjectStoreException | ObjectNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

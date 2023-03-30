package org.ical4j.connector.command;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.property.Uid;
import org.ical4j.connector.*;
import picocli.CommandLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;
import static org.ical4j.connector.command.DefaultOutputHandlers.STDOUT_LIST_PRINTER;

@CommandLine.Command(name = "card", description = "Command group for card operations",
        subcommands = {CardCommand.GetCard.class, CardCommand.ListCards.class, CardCommand.CreateCard.class,
                CardCommand.UpdateCard.class, CardCommand.DeleteCard.class},
        mixinStandardHelpOptions = true)
public class CardCommand {
    @CommandLine.Command(name = "create", description = "Persist vCard object from input data")
    public static class CreateCard extends AbstractCardCommand<Uid> {

        public CreateCard(String collectionName, Consumer<Uid> consumer) {
            super(collectionName, consumer);
        }

        public CreateCard(CardStore<CardCollection> store) {
            super(DEFAULT_COLLECTION, store);
        }


        @Override
        public void run() {
            try {
                getConsumer().accept(getCollection().addCard(getCard()));
            } catch (ObjectStoreException | ConstraintViolationException | ObjectNotFoundException | ParserException |
                     IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CommandLine.Command(name = "delete", description = "Delete vCard objects with specified UID")
    public static class DeleteCard extends AbstractCollectionCommand<CardCollection, VCard> {

        @CommandLine.Option(names = {"-uid"})
        private String cardUid;

        public DeleteCard() {
            super();
        }

        public DeleteCard(String collectionName, Consumer<VCard> consumer) {
            super(collectionName, consumer);
        }

        public DeleteCard(String collectionName, Consumer<VCard> consumer, ObjectStore<CardCollection> store) {
            super(collectionName, consumer, store);
        }

        public DeleteCard withCardUid(String cardUid) {
            this.cardUid = cardUid;
            return this;
        }

        @Override
        public void run() {
            try {
                getConsumer().accept(getCollection().removeCard(cardUid));
            } catch (FailedOperationException | ObjectNotFoundException | ObjectStoreException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CommandLine.Command(name = "get", description = "Retrieve a vCard object with specified UID")
    public static class GetCard extends AbstractCollectionCommand<CardCollection, VCard> {

        @CommandLine.Option(names = {"-uid"})
        private String cardUid;

        public GetCard() {
            super();
        }

        public GetCard(String collectionName, Consumer<VCard> consumer) {
            super(collectionName, consumer);
        }

        public GetCard(String collectionName, Consumer<VCard> consumer, ObjectStore<CardCollection> store) {
            super(collectionName, consumer, store);
        }

        public GetCard withCardUid(String cardUid) {
            this.cardUid = cardUid;
            return this;
        }

        @Override
        public void run() {
            try {
                getConsumer().accept(getCollection().getCard(cardUid));
            } catch (FailedOperationException | ObjectNotFoundException | ObjectStoreException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CommandLine.Command(name = "list", description = "List cards in a card collection")
    public static class ListCards extends AbstractCollectionCommand<CardCollection, List<VCard>> {

        public ListCards() {
            super(DEFAULT_COLLECTION, STDOUT_LIST_PRINTER());
        }

        public ListCards(String collectionName, Consumer<List<VCard>> consumer) {
            super(collectionName, consumer);
        }

        @Override
        public void run() {
            try {
                List<VCard> cards = new ArrayList<>();
                for (String uid : getCollection().listObjectUids()) {
                    cards.add(getCollection().getCard(uid));
                }
                getConsumer().accept(cards);
            } catch (ObjectStoreException | ObjectNotFoundException | FailedOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CommandLine.Command(name = "update", description = "Persist vCard object from input data")
    public static class UpdateCard extends AbstractCardCommand<Uid[]> {

        public UpdateCard() {
            super();
        }

        public UpdateCard(String collectionName, Consumer<Uid[]> consumer) {
            super(collectionName, consumer);
        }

        public UpdateCard(String collectionName, ObjectStore<CardCollection> store) {
            super(collectionName, store);
        }

        @Override
        public void run() {
            try {
                getConsumer().accept(getCollection().merge(getCard()));
            } catch (ObjectStoreException | ConstraintViolationException | ObjectNotFoundException | IOException |
                     ParserException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

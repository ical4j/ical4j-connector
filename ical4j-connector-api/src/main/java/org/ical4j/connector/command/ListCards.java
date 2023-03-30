package org.ical4j.connector.command;

import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;
import static org.ical4j.connector.command.DefaultOutputHandlers.STDOUT_LIST_PRINTER;

@CommandLine.Command(name = "list-cards", description = "List cards in a card collection")
public class ListCards extends AbstractCollectionCommand<CardCollection, List<VCard>> {

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

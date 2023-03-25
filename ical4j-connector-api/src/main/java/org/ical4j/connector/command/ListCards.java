package org.ical4j.connector.command;

import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.CardCollection;
import picocli.CommandLine;

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
//        try {
//            getConsumer().accept(getCollection().listObjectUids());
//        } catch (ObjectStoreException | ObjectNotFoundException e) {
//            throw new RuntimeException(e);
//        }
    }
}

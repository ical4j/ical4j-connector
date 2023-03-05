package org.ical4j.connector.command;

import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.*;
import picocli.CommandLine;

import java.util.function.Consumer;

import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;

@CommandLine.Command(name = "delete-card", description = "Delete vCard objects with specified UID")
public class DeleteCard extends AbstractCollectionCommand<CardCollection, VCard> {

    private String cardUid;

    public DeleteCard() {
        super(DEFAULT_COLLECTION, vCard -> {});
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

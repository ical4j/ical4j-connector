package org.ical4j.connector.command;

import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.*;
import picocli.CommandLine;

import java.util.function.Consumer;

import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;

@CommandLine.Command(name = "get-card", description = "Retrieve a vCard object with specified UID")
public class GetCard extends AbstractCollectionCommand<CardCollection, VCard> {

    private String cardUid;

    public GetCard() {
        super(DEFAULT_COLLECTION, vCard -> {});
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

package org.ical4j.connector.command;

import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.*;
import picocli.CommandLine;

import java.util.function.Consumer;

@CommandLine.Command(name = "get-card", description = "Retrieve a vCard object with specified UID")
public class GetCard extends AbstractCollectionCommand<CardCollection, VCard> {

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

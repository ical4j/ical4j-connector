package org.ical4j.connector.command;

import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.property.Uid;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.util.function.Consumer;

import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;

@CommandLine.Command(name = "update-card", description = "Persist vCard object from input data")
public class UpdateCard extends AbstractCollectionCommand<CardCollection, Uid[]> {

    private VCard card;

    public UpdateCard() {
        super(DEFAULT_COLLECTION, card -> {});
    }

    public UpdateCard(String collectionName, Consumer<Uid[]> consumer) {
        super(collectionName, consumer);
    }

    public UpdateCard(String collectionName, Consumer<Uid[]> consumer, ObjectStore<CardCollection> store) {
        super(collectionName, consumer, store);
    }

    public UpdateCard withCard(VCard card) {
        this.card = card;
        return this;
    }

    @Override
    public void run() {
        try {
            getConsumer().accept(getCollection().merge(card));
        } catch (ObjectStoreException | ConstraintViolationException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

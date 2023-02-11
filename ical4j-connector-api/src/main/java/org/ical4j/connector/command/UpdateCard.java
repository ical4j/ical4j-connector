package org.ical4j.connector.command;

import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

@CommandLine.Command(name = "update-card", description = "Persist vCard object from input data")
public class UpdateCard extends AbstractCommand<CardCollection> {

    private final CardCollection collection;

    private VCard card;

    public UpdateCard(CardCollection collection) {
        this.collection = collection;
    }

    public UpdateCard withCard(VCard card) {
        this.card = card;
        return this;
    }

    @Override
    public void run() {
        try {
            collection.merge(card);
        } catch (ObjectStoreException | ConstraintViolationException e) {
            throw new RuntimeException(e);
        }
    }
}

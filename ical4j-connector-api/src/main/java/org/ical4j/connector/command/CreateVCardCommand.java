package org.ical4j.connector.command;

import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

@CommandLine.Command(name = "create-card", description = "Persist vCard object from input data")
public class CreateVCardCommand implements Runnable {

    private final CardCollection collection;

    private VCard card;

    public CreateVCardCommand(CardCollection collection) {
        this.collection = collection;
    }

    public CreateVCardCommand withCard(VCard card) {
        this.card = card;
        return this;
    }

    @Override
    public void run() {
        try {
            collection.addCard(card);
        } catch (ObjectStoreException | ConstraintViolationException e) {
            throw new RuntimeException(e);
        }
    }
}

package org.ical4j.connector.command;

import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.property.Uid;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.CardStore;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

@CommandLine.Command(name = "create-card", description = "Persist vCard object from input data")
public class CreateCard extends AbstractCollectionCommand<CardCollection, Uid> {

    private VCard card;

    public CreateCard(CardStore store) {
        super("default", card -> {}, store);
    }

    public CreateCard withCard(VCard card) {
        this.card = card;
        return this;
    }

    @Override
    public void run() {
        try {
            getConsumer().accept(getCollection().addCard(card));
        } catch (ObjectStoreException | ConstraintViolationException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

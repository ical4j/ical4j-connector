package org.ical4j.connector.command;

import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.ObjectNotFoundException;
import picocli.CommandLine;

@CommandLine.Command(name = "get-card", description = "Retrieve a vCard object with specified UID")
public class GetCard extends AbstractCommand<CardCollection> {

    private final CardCollection collection;

    private VCard card;

    private String cardUid;

    public GetCard(CardCollection collection) {
        this.collection = collection;
    }

    public GetCard withCardUid(String cardUid) {
        this.cardUid = cardUid;
        return this;
    }

    public VCard getCard() {
        return card;
    }

    @Override
    public void run() {
        try {
            this.card = collection.getCard(cardUid);
        } catch (FailedOperationException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

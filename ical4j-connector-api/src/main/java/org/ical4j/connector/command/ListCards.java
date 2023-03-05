package org.ical4j.connector.command;

import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.CardCollection;
import picocli.CommandLine;

import java.util.Collections;
import java.util.List;

@CommandLine.Command(name = "list-cards", description = "List cards in a card collection")
public class ListCards extends AbstractCollectionCommand<CardCollection, List<VCard>> {

    public ListCards() {
        super("default", list -> {});
    }

    @Override
    public void run() {
        getConsumer().accept(Collections.emptyList());
    }
}

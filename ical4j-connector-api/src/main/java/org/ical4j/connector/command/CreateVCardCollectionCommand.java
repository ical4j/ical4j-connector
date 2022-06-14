package org.ical4j.connector.command;

import org.ical4j.connector.CardCollection;
import org.ical4j.connector.CardStore;
import picocli.CommandLine;

@CommandLine.Command(name = "create-card-collection", description = "Create a new vCard collection")
public class CreateVCardCollectionCommand extends AbstractCreateCollectionCommand<CardCollection> {

    public CreateVCardCollectionCommand(CardStore<CardCollection> store) {
        super(store);
    }
}

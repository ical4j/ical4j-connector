package org.ical4j.connector.command;

import org.ical4j.connector.CardCollection;
import org.ical4j.connector.CardStore;
import picocli.CommandLine;

@CommandLine.Command(name = "delete-card-collection", description = "Remove a vCard collection")
public class DeleteVCardCollectionCommand extends AbstractDeleteCollectionCommand<CardCollection> {

    public DeleteVCardCollectionCommand(CardStore<CardCollection> store) {
        super(store);
    }
}

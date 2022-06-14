package org.ical4j.connector.command;

import org.ical4j.connector.CardCollection;
import org.ical4j.connector.CardStore;
import picocli.CommandLine;

@CommandLine.Command(name = "get-card-collection", description = "Retrieve a vCard collection")
public class GetVCardCollectionCommand extends AbstractGetCollectionCommand<CardCollection> {

    public GetVCardCollectionCommand(CardStore<CardCollection> store) {
        super(store);
    }
}

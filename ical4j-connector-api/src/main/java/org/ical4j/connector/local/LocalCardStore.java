package org.ical4j.connector.local;

import org.ical4j.connector.CardStore;

import java.io.File;
import java.io.IOException;

public class LocalCardStore extends AbstractLocalObjectStore<LocalCardCollection>
        implements CardStore<LocalCardCollection> {

    public LocalCardStore(File root) {
        super(root);
    }

    @Override
    protected LocalCardCollection newCollection(String id) throws IOException {
        return new LocalCardCollection(new File(getRoot(), id));
    }
}

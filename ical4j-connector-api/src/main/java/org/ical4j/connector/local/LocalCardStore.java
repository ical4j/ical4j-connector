package org.ical4j.connector.local;

import org.ical4j.connector.CardStore;

import java.io.File;

public class LocalCardStore extends AbstractLocalObjectStore<LocalCardCollection>
        implements CardStore<LocalCardCollection> {

    public LocalCardStore(File root) {
        super(root);
    }

    @Override
    protected LocalCardCollection newCollection(String id) {
        return new LocalCardCollection(new File(getRoot(), id));
    }
}

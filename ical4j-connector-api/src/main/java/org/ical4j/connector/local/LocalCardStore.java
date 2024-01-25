package org.ical4j.connector.local;

import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.CardStore;

import java.io.File;
import java.io.IOException;

public class LocalCardStore extends AbstractLocalObjectStore<VCard, LocalCardCollection>
        implements CardStore<LocalCardCollection> {

    public LocalCardStore(File root) {
        super(root);
    }

    @Override
    protected LocalCardCollection newCollection(String id, String workspace) throws IOException {
        return new LocalCardCollection(new File(getWorkspaceDir(workspace), id));
    }
}

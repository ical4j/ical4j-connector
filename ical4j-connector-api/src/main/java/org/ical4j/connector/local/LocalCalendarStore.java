package org.ical4j.connector.local;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.ObjectStore;

import java.io.File;
import java.io.IOException;

/**
 * LocalCalendarStore is a concrete implementation of ObjectStore for managing calendar objects
 * stored in a local file system. It extends AbstractLocalObjectStore to provide basic functionality
 * for storing and retrieving calendar objects.
 */
public class LocalCalendarStore extends AbstractLocalObjectStore<Calendar, LocalCalendarCollection>
        implements ObjectStore<Calendar, LocalCalendarCollection> {

    public LocalCalendarStore(File root) {
        super(root);
    }

    @Override
    protected LocalCalendarCollection newCollection(String id, String workspace) throws IOException {
        return new LocalCalendarCollection(new File(getWorkspaceDir(workspace), id));
    }
}

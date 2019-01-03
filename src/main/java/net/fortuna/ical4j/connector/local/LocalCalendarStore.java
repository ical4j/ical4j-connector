package net.fortuna.ical4j.connector.local;

import net.fortuna.ical4j.connector.CalendarStore;

import java.io.File;

public class LocalCalendarStore extends AbstractLocalObjectStore<LocalCalendarCollection>
        implements CalendarStore<LocalCalendarCollection> {

    public LocalCalendarStore(File root) {
        super(root);
    }

    @Override
    protected LocalCalendarCollection newCollection(String id) {
        return new LocalCalendarCollection(new File(getRoot(), id));
    }
}

package org.ical4j.connector.local;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.CalendarStore;

import java.io.File;
import java.io.IOException;

public class LocalCalendarStore extends AbstractLocalObjectStore<Calendar, LocalCalendarCollection>
        implements CalendarStore<LocalCalendarCollection> {

    public LocalCalendarStore(File root) {
        super(root);
    }

    @Override
    protected LocalCalendarCollection newCollection(String id) throws IOException {
        return new LocalCalendarCollection(new File(getRoot(), id));
    }
}

package org.ical4j.connector.command;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

@CommandLine.Command(name = "update-calendar", description = "Update calendar object matching input parameters")
public class UpdateCalendarCommand implements Runnable {

    private final CalendarCollection collection;

    private Calendar calendar;

    public UpdateCalendarCommand(CalendarCollection collection) {
        this.collection = collection;
    }

    public UpdateCalendarCommand withCalendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }

    @Override
    public void run() {
        try {
            collection.merge(calendar);
        } catch (ObjectStoreException | FailedOperationException e) {
            throw new RuntimeException(e);
        }
    }
}

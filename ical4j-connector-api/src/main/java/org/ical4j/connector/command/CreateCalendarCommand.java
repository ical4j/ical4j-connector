package org.ical4j.connector.command;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ConstraintViolationException;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

@CommandLine.Command(name = "create-calendar", description = "Persist calendar object from input data")
public class CreateCalendarCommand implements Runnable {

    private final CalendarCollection collection;

    private Calendar calendar;

    public CreateCalendarCommand(CalendarCollection collection) {
        this.collection = collection;
    }

    public CreateCalendarCommand withCalendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }

    @Override
    public void run() {
        try {
            collection.addCalendar(calendar);
        } catch (ObjectStoreException | ConstraintViolationException e) {
            throw new RuntimeException(e);
        }
    }
}

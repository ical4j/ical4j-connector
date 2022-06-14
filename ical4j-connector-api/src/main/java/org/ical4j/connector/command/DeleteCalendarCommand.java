package org.ical4j.connector.command;

import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

@CommandLine.Command(name = "delete-calendar", description = "Delete calendar objects with specified UID")
public class DeleteCalendarCommand implements Runnable {

    private final CalendarCollection collection;

    private String calendarUid;

    public DeleteCalendarCommand(CalendarCollection collection) {
        this.collection = collection;
    }

    public DeleteCalendarCommand withCalendarUid(String calendarUid) {
        this.calendarUid = calendarUid;
        return this;
    }

    @Override
    public void run() {
        try {
            collection.removeCalendar(calendarUid);
        } catch (ObjectStoreException | FailedOperationException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

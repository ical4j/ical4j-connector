package org.ical4j.connector.command;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.*;
import picocli.CommandLine;

@CommandLine.Command(name = "delete-calendar", description = "Delete calendar objects with specified UID")
public class DeleteCalendar extends AbstractCollectionCommand<CalendarCollection, Calendar> {

    @CommandLine.Option(names = {"-I", "--uid"})
    private String calendarUid;

    public DeleteCalendar() {
        super("default", calendar -> {});
    }

    public DeleteCalendar(ObjectStore<CalendarCollection> store) {
        super("default", calendar -> {}, store);
    }

    public DeleteCalendar(String collectionName, ObjectStore<CalendarCollection> store) {
        super("default", calendar -> {}, store);
    }

    public DeleteCalendar withCalendarUid(String calendarUid) {
        this.calendarUid = calendarUid;
        return this;
    }

    @Override
    public void run() {
        try {
            getConsumer().accept(getCollection().removeCalendar(calendarUid));
        } catch (ObjectStoreException | FailedOperationException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

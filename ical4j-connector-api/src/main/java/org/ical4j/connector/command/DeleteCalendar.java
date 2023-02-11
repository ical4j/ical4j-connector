package org.ical4j.connector.command;

import org.ical4j.connector.*;
import picocli.CommandLine;

@CommandLine.Command(name = "delete-calendar", description = "Delete calendar objects with specified UID")
public class DeleteCalendar extends AbstractCalendarCommand {

    @CommandLine.Option(names = {"-I", "--uid"})
    private String calendarUid;

    public DeleteCalendar() {
    }

    public DeleteCalendar(ObjectStore<CalendarCollection> store) {
        super(store);
    }

    public DeleteCalendar(String collectionName, ObjectStore<CalendarCollection> store) {
        super(collectionName, store);
    }

    public DeleteCalendar withCalendarUid(String calendarUid) {
        this.calendarUid = calendarUid;
        return this;
    }

    @Override
    public void run() {
        try {
            getStore().getCollection(getCollectionName()).removeCalendar(calendarUid);
        } catch (ObjectStoreException | FailedOperationException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

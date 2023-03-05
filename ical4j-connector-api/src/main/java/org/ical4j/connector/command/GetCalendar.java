package org.ical4j.connector.command;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.util.function.Consumer;

import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;

@CommandLine.Command(name = "get-calendar", description = "Retrieve a calendar object with specified UID")
public class GetCalendar extends AbstractCollectionCommand<CalendarCollection, Calendar> {

    private String calendarUid;

    public GetCalendar() {
        super(DEFAULT_COLLECTION, calendar -> {});
    }

    public GetCalendar(String collectionName, Consumer<Calendar> consumer) {
        super(collectionName, consumer);
    }

    public GetCalendar(String collectionName, Consumer<Calendar> consumer, ObjectStore<CalendarCollection> store) {
        super(collectionName, consumer, store);
    }

    public GetCalendar withCalendarUid(String calendarUid) {
        this.calendarUid = calendarUid;
        return this;
    }

    @Override
    public void run() {
        try {
            getConsumer().accept(getCollection().getCalendar(calendarUid));
        } catch (ObjectNotFoundException | ObjectStoreException e) {
            throw new RuntimeException(e);
        }
    }
}

package org.ical4j.connector.command;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;
import static org.ical4j.connector.command.DefaultOutputHandlers.STDOUT_LIST_PRINTER;

@CommandLine.Command(name = "list-calendars", description = "List calendar UIDs in a calendar collection")
public class ListCalendars extends AbstractCollectionCommand<CalendarCollection, List<Calendar>> {

    public ListCalendars() {
        super(DEFAULT_COLLECTION, STDOUT_LIST_PRINTER());
    }

    public ListCalendars(String collectionName, Consumer<List<Calendar>> consumer) {
        super(collectionName, consumer);
    }

    public ListCalendars(ObjectStore<CalendarCollection> store) {
        super(DEFAULT_COLLECTION, STDOUT_LIST_PRINTER(), store);
    }

    public ListCalendars(String collectionName, ObjectStore<CalendarCollection> store) {
        super(collectionName, STDOUT_LIST_PRINTER(), store);
    }

    @Override
    public void run() {
        try {
            List<Calendar> calendars = new ArrayList<>();
            for (String uid : getCollection().listObjectUids()) {
                calendars.add(getCollection().getCalendar(uid));
            }
            getConsumer().accept(calendars);
        } catch (ObjectStoreException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

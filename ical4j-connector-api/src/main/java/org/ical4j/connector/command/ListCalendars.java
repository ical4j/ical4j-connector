package org.ical4j.connector.command;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.ObjectStore;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "list-calendars", description = "List calendars in a calendar collection")
public class ListCalendars extends AbstractCollectionCommand<CalendarCollection, List<Calendar>> {

    public ListCalendars() {
        super("default", collection -> {});
    }

    public ListCalendars(ObjectStore<CalendarCollection> store) {
        super("default", collection -> {}, store);
    }

    public ListCalendars(String collectionName, ObjectStore<CalendarCollection> store) {
        super(collectionName, collection -> {}, store);
    }

    @Override
    public void run() {
        //TODO: add support for get all calendars in calendar collection..
    }
}

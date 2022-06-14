package org.ical4j.connector.command;

import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.CalendarStore;
import picocli.CommandLine;

@CommandLine.Command(name = "get-calendar-collection", description = "Retrieve a calendar collection")
public class GetCalendarCollectionCommand extends AbstractGetCollectionCommand<CalendarCollection> {

    public GetCalendarCollectionCommand(CalendarStore<CalendarCollection> store) {
        super(store);
    }
}

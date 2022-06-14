package org.ical4j.connector.command;

import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.CalendarStore;
import picocli.CommandLine;

@CommandLine.Command(name = "create-calendar-collection", description = "Create a new calendar collection")
public class CreateCalendarCollectionCommand extends AbstractCreateCollectionCommand<CalendarCollection> {

    public CreateCalendarCollectionCommand(CalendarStore<CalendarCollection> store) {
        super(store);
    }
}

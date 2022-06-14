package org.ical4j.connector.command;

import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.CalendarStore;
import picocli.CommandLine;

@CommandLine.Command(name = "delete-calendar-collection", description = "Remove a vCard collection")
public class DeleteCalendarCollectionCommand extends AbstractDeleteCollectionCommand<CalendarCollection> {

    public DeleteCalendarCollectionCommand(CalendarStore<CalendarCollection> store) {
        super(store);
    }
}

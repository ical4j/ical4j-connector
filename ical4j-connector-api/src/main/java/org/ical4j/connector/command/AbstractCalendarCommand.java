package org.ical4j.connector.command;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.util.Calendars;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.ObjectStore;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URL;

public abstract class AbstractCalendarCommand extends AbstractCommand<CalendarCollection> {

    @CommandLine.Option(names = {"-X", "--name"})
    private String collectionName;

    private Calendar calendar;

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    private Input input;

    static class Input {
        @CommandLine.Option(names = {"-U", "--url"}, required = true)
        private URL url;

        @CommandLine.Option(names = {"-F", "--file"}, required = true)
        private String filename;

        @CommandLine.Option(names = {"--stdin"}, required = true)
        private boolean stdin;
    }

    public AbstractCalendarCommand() {
    }

    public AbstractCalendarCommand(ObjectStore<CalendarCollection> store) {
        super(store);
    }

    public AbstractCalendarCommand(String collectionName, ObjectStore<CalendarCollection> store) {
        super(store);
        this.collectionName = collectionName;
    }

    public AbstractCalendarCommand withCalendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public Calendar getCalendar() {
        if (input != null) {
            try {
                if (input.filename != null) {
                    calendar = Calendars.load(input.filename);
                } else if (input.url != null) {
                    calendar = Calendars.load(input.url);
                } else if (input.stdin) {
                    final CalendarBuilder builder = new CalendarBuilder();
                    calendar = builder.build(System.in);
                }
            } catch (ConstraintViolationException | IOException | ParserException e) {
                throw new RuntimeException(e);
            }
        }
        return calendar;
    }
}

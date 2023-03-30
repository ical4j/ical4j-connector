package org.ical4j.connector.command;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.util.Calendars;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.ObjectStore;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Provides basis for commands that operate on a single calendar input. This may include things like creation
 * or import operations.
 *
 * @param <T> the command result consumer
 */
public abstract class AbstractCalendarCommand<T> extends AbstractCollectionCommand<CalendarCollection, T> {

    @CommandLine.ArgGroup(multiplicity = "1")
    protected Input input;

    static class Input {
        @CommandLine.Option(names = {"-url"}, required = true)
        protected URL url;

        @CommandLine.Option(names = {"-file"}, required = true)
        protected String filename;

        @CommandLine.Option(names = {"-", "--stdin"}, required = true)
        protected boolean stdin;
    }

    private Calendar calendar;

    public AbstractCalendarCommand() {
    }

    public AbstractCalendarCommand(String collectionName) {
        super(collectionName);
    }

    public AbstractCalendarCommand(String collectionName, Consumer<T> consumer) {
        super(collectionName, consumer);
    }

    public AbstractCalendarCommand(String collectionName, ObjectStore<CalendarCollection> store) {
        super(collectionName, store);
    }

    public AbstractCalendarCommand<T> withCalendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }

    public Calendar getCalendar() throws ParserException, IOException {
        if (calendar == null) {
            if (input.filename != null) {
                calendar = Calendars.load(input.filename);
            } else if (input.url != null) {
                calendar = Calendars.load(input.url);
            } else if (input.stdin) {
                final CalendarBuilder builder = new CalendarBuilder();
                calendar = builder.build(System.in);
            }
        }
        return calendar;
    }
}

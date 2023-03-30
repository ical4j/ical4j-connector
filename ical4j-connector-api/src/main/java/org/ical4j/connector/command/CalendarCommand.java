package org.ical4j.connector.command;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.property.Uid;
import org.ical4j.connector.*;
import picocli.CommandLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;
import static org.ical4j.connector.command.DefaultOutputHandlers.STDOUT_LIST_PRINTER;

@CommandLine.Command(name = "calendar", description = "Command group for calendar operations",
        subcommands = {CalendarCommand.GetCalendar.class, CalendarCommand.ListCalendars.class,
                CalendarCommand.CreateCalendar.class, CalendarCommand.UpdateCalendar.class,
                CalendarCommand.DeleteCalendar.class},
        mixinStandardHelpOptions = true)
public class CalendarCommand {


    /**
     * Create a new calendar in the specified collection using provided calendar data. The calendar data should
     * include only components of a single type and with the same UID.
     *
     * The required inputs for this command are:
     * * collectionName -> the collection to add the calendar to
     * * calendarData -> data used to construct an iCalendar object
     * * outputHandler -> defines how output is handled on successful command completion
     *
     * The outputs for this command are:
     * * uid -> the UID of the calendar that was successfully added to the collection
     *
     * Command configuration options include:
     * * defaultCollection -> the default collection used if no collectionName is specified
     * * defaultOutputHandler -> the default output handler if none is specified
     * * generateUid -> whether to create a UID for the calendar if none exists
     * * overrideUid -> always replace any existing UID with an internally generated one (implies generateUID = true)
     *
     * To import multiple different components use {@link ImportCalendars}.
     *
     */
    @CommandLine.Command(name = "create", description = "Persist calendar object from input data",
            mixinStandardHelpOptions = true)
    public static class CreateCalendar extends AbstractCalendarCommand<Uid> {

        public CreateCalendar() {
            super(DEFAULT_COLLECTION);
        }


        public CreateCalendar(CalendarStore<CalendarCollection> store) {
            super(DEFAULT_COLLECTION, store);
        }

        public CreateCalendar(String collectionName, Consumer<Uid> consumer) {
            super(collectionName, consumer);
        }

        public CreateCalendar(String collectionName, CalendarStore<CalendarCollection> store) {
            super(collectionName, store);
        }

        @Override
        public void run() {
            try {
                getConsumer().accept(getCollection().addCalendar(getCalendar()));
            } catch (ObjectStoreException | ConstraintViolationException | ObjectNotFoundException | ParserException |
                     IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CommandLine.Command(name = "delete", description = "Delete calendar objects with specified UID")
    public static class DeleteCalendar extends AbstractCollectionCommand<CalendarCollection, Calendar> {

        @CommandLine.Option(names = {"-uid"})
        private String calendarUid;

        public DeleteCalendar() {
            super();
        }

        public DeleteCalendar(String collectionName, Consumer<Calendar> consumer) {
            super(collectionName, consumer);
        }

        public DeleteCalendar(ObjectStore<CalendarCollection> store) {
            super(DEFAULT_COLLECTION, store);
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
                getConsumer().accept(getCollection().removeCalendar(calendarUid));
            } catch (ObjectStoreException | FailedOperationException | ObjectNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CommandLine.Command(name = "list", description = "List calendar UIDs in a calendar collection")
    public static class ListCalendars extends AbstractCollectionCommand<CalendarCollection, List<Calendar>> {

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

    @CommandLine.Command(name = "update", description = "Update calendar object matching input parameters")
    public static class UpdateCalendar extends AbstractCalendarCommand<Uid[]> {

        public UpdateCalendar() {
            super();
        }

        public UpdateCalendar(String collectionName, Consumer<Uid[]> consumer) {
            super(collectionName, consumer);
        }

        public UpdateCalendar(ObjectStore<CalendarCollection> store) {
            super(DEFAULT_COLLECTION, store);
        }

        public UpdateCalendar(String collectionName, ObjectStore<CalendarCollection> store) {
            super(collectionName, store);
        }

        @Override
        public void run() {
            try {
                getCollection().merge(getCalendar());
            } catch (ObjectStoreException | FailedOperationException | ObjectNotFoundException | ParserException |
                     IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CommandLine.Command(name = "get", description = "Retrieve a calendar object with specified UID")
    public static class GetCalendar extends AbstractCollectionCommand<CalendarCollection, Calendar> {

        @CommandLine.Option(names = {"-uid"})
        private String calendarUid;

        public GetCalendar() {
            super();
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
}

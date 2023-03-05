package org.ical4j.connector.command;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.property.Uid;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.CalendarStore;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

@CommandLine.Command(name = "create-calendar", description = "Persist calendar object from input data")
public class CreateCalendar extends AbstractCollectionCommand<CalendarCollection, Uid> {

    private Calendar calendar;

    public CreateCalendar() {
        super("default", calendar -> {});
    }

    public CreateCalendar(CalendarStore<CalendarCollection> store) {
        super("default", uid -> {}, store);
    }

    public CreateCalendar(String collectionName, CalendarStore<CalendarCollection> store) {
        super(collectionName, uid -> {}, store);
    }

    public CreateCalendar withCalendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    @Override
    public void run() {
        try {
            getConsumer().accept(getCollection().addCalendar(getCalendar()));
        } catch (ObjectStoreException | ConstraintViolationException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

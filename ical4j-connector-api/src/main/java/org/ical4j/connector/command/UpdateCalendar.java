package org.ical4j.connector.command;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.Uid;
import org.ical4j.connector.*;
import picocli.CommandLine;

import java.util.function.Consumer;

import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;

@CommandLine.Command(name = "update-calendar", description = "Update calendar object matching input parameters")
public class UpdateCalendar extends AbstractCollectionCommand<CalendarCollection, Uid[]> {

    private Calendar calendar;

    public UpdateCalendar() {
        super(DEFAULT_COLLECTION);
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

    public UpdateCalendar withCalendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    @Override
    public void run() {
        try {
            getCollection().merge(getCalendar());
        } catch (ObjectStoreException | FailedOperationException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

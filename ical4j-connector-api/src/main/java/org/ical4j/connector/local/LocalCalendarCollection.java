package org.ical4j.connector.local;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;
import org.ical4j.connector.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LocalCalendarCollection extends AbstractLocalObjectCollection<Calendar> implements CalendarCollection {

    private static final MediaType[] SUPPORTED_MEDIA_TYPES = new MediaType[1];
    static {
        SUPPORTED_MEDIA_TYPES[0] = MediaType.ICALENDAR_2_0;
    }

    public LocalCalendarCollection(File root) throws IOException {
        super(root);
        setDisplayName(root.getName());
    }

    @Override
    public MediaType[] getSupportedMediaTypes() {
        return SUPPORTED_MEDIA_TYPES;
    }

    @Override
    public long getMaxResourceSize() {
        return 0;
    }

    @Override
    public Instant getMinDateTime() {
        return null;
    }

    @Override
    public Instant getMaxDateTime() {
        return null;
    }

    @Override
    public Integer getMaxInstances() {
        return null;
    }

    @Override
    public Integer getMaxAttendeesPerInstance() {
        return null;
    }

    @Override
    public void addCalendar(Calendar calendar) throws ObjectStoreException, ConstraintViolationException {
        Calendar[] uidCalendars = calendar.split();
        for (Calendar c : uidCalendars) {
            Uid uid = c.getUid();
            try {
                Calendar existing = getCalendar(uid.getValue());

                // TODO: potentially merge/replace existing..
                throw new ObjectStoreException("Calendar already exists");
            } catch (ObjectNotFoundException e) {

            }

            try (FileWriter writer = new FileWriter(new File(getRoot(), uid.getValue() + ".ics"))) {
                new CalendarOutputter(false).output(c, writer);
            } catch (IOException e) {
                throw new ObjectStoreException("Error writing calendar file", e);
            }
        }
    }

    @Override
    public Calendar getCalendar(String uid) throws ObjectNotFoundException {
        try {
            return Calendars.load(new File(getRoot(), uid + ".ics").getAbsolutePath());
        } catch (IOException | ParserException e) {
            throw new ObjectNotFoundException(String.format("Calendar not found: %s", uid), e);
        }
    }

    @Override
    public Calendar removeCalendar(String uid) throws FailedOperationException, ObjectNotFoundException {
        Calendar calendar = getCalendar(uid);
        if (!new File(getRoot(), uid + ".ics").delete()) {
            throw new FailedOperationException("Unable to delete calendar: " + uid);
        }
        return calendar;
    }

    @Override
    public void merge(Calendar calendar) {
    }

    @Override
    public Calendar export() {
        return null;
    }

    @Override
    public Iterable<Calendar> getComponents() throws ObjectStoreException {
        List<Calendar> calendars = new ArrayList<>();

        File[] componentFiles = getRoot().listFiles(pathname ->
                !pathname.isDirectory() && pathname.getName().endsWith(".ics"));
        if (componentFiles != null) {
            try {
                for (File file : componentFiles) {
                    calendars.add(Calendars.load(file.getAbsolutePath()));
                }
            } catch (IOException | ParserException e) {
                throw new ObjectStoreException(e);
            }
        }
        return calendars;
    }
}

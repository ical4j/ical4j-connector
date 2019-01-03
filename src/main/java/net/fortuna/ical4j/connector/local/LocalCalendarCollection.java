package net.fortuna.ical4j.connector.local;

import net.fortuna.ical4j.connector.CalendarCollection;
import net.fortuna.ical4j.connector.FailedOperationException;
import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.enums.MediaType;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalCalendarCollection extends AbstractLocalObjectCollection<Calendar> implements CalendarCollection {

    private static MediaType[] SUPPORTED_MEDIA_TYPES = new MediaType[1];
    static {
        SUPPORTED_MEDIA_TYPES[0] = MediaType.ICALENDAR_2_0;
    }

    public LocalCalendarCollection(File root) {
        super(root);
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
    public String getMinDateTime() {
        return null;
    }

    @Override
    public String getMaxDateTime() {
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
        Uid uid = Calendars.getUid(calendar);
        if (uid == null) {
            throw new ConstraintViolationException("A valid UID was not found.");
        }

        try {
            Calendar existing = getCalendar(uid.getValue());

            // TODO: potentially merge/replace existing..
            throw new ObjectStoreException("Calendar already exists");
        } catch (ObjectNotFoundException e) {

        }

        try (FileWriter writer = new FileWriter(new File(getRoot(), uid.getValue() + ".ics"))) {
            new CalendarOutputter(false).output(calendar, writer);
        } catch (IOException e) {
            throw new ObjectStoreException("Error writing calendar file", e);
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
    public void merge(Calendar calendar) throws FailedOperationException, ObjectStoreException {
    }

    @Override
    public Calendar export() throws ObjectStoreException {
        return null;
    }

    @Override
    public Calendar[] getComponents() throws ObjectStoreException {
        List<Calendar> calendars = new ArrayList<>();

        try {
            for (File file : getRoot().listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return !pathname.isDirectory() && pathname.getName().endsWith(".ics");
                }
            })) {
                calendars.add(Calendars.load(file.getAbsolutePath()));
            }
        } catch (IOException | ParserException e) {
            throw new ObjectStoreException(e);
        }

        return calendars.toArray(new Calendar[calendars.size()]);
    }
}

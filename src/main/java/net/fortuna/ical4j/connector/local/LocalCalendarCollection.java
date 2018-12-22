package net.fortuna.ical4j.connector.local;

import net.fortuna.ical4j.connector.CalendarCollection;
import net.fortuna.ical4j.connector.FailedOperationException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.enums.MediaType;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.util.Calendars;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalCalendarCollection extends AbstractLocalObjectCollection<Calendar> implements CalendarCollection {

    public LocalCalendarCollection(File root) {
        super(root);
    }

    @Override
    public MediaType[] getSupportedMediaTypes() {
        return new MediaType[0];
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

    }

    @Override
    public Calendar getCalendar(String uid) {
        return null;
    }

    @Override
    public Calendar removeCalendar(String uid) throws FailedOperationException, ObjectStoreException {
        return null;
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

package org.ical4j.connector.local;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Calendars;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.MediaType;
import org.ical4j.connector.ObjectStoreException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LocalCalendarCollection extends AbstractLocalObjectCollection<Calendar> implements CalendarCollection {

    private static final MediaType[] SUPPORTED_MEDIA_TYPES = new MediaType[1];
    static {
        SUPPORTED_MEDIA_TYPES[0] = MediaType.ICALENDAR_2_0;
    }

    public LocalCalendarCollection(File root) throws IOException {
        super(root);
//        setDisplayName(root.getName());
//        setSupportedComponents(new String[]{"VEVENT", "VTODO", "VJOURNAL"});
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
    public List<String> listObjectUIDs() {
        return Arrays.stream(getObjectFiles()).map(file -> file.getName().split(".ics")[0])
                .collect(Collectors.toList());
    }

    @Override
    public String add(Calendar object) throws ObjectStoreException {
        Uid uid = object.getUid();
        Optional<Calendar> existing = get(uid.getValue());
        if (existing.isPresent()) {
            // TODO: potentially merge/replace existing..
            throw new ObjectStoreException("Calendar already exists");
        }

        try (FileWriter writer = new FileWriter(new File(getRoot(), uid.getValue() + ".ics"))) {
            new CalendarOutputter(false).output(object, writer);
        } catch (IOException e) {
            throw new ObjectStoreException("Error writing calendar file", e);
        }

        // notify listeners..
        fireOnAddEvent(this, object);

        return uid.getValue();
    }

    @Override
    public Optional<Calendar> get(String uid) {
        File calendarFile = new File(getRoot(), uid + ".ics");
        if (!calendarFile.exists()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Calendars.load(calendarFile.getAbsolutePath()));
        } catch (IOException | ParserException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Calendar> removeAll(String... uid) throws FailedOperationException {
        List<Calendar> removed = new ArrayList<>();
        for (String u : uid) {
            File calendarFile = new File(getRoot(), u + ".ics");
            if (calendarFile.exists()) {
                Optional<Calendar> cal = get(u);
                if (cal.isPresent()) {
                    if (!calendarFile.delete()) {
                        throw new FailedOperationException("Unable to delete calendar: " + u);
                    }
                    removed.add(cal.get());
                    fireOnRemoveEvent(this, cal.get());
                }
            }
        }
        return removed;
    }

    @Override
    public Uid[] merge(Calendar calendar) throws ObjectStoreException {
        Calendar[] uidCalendars = calendar.split();
        for (Calendar c : uidCalendars) {
            Uid uid = c.getUid();
            Optional<Calendar> existing = get(uid.getValue());

            if (existing.isPresent()) {
                // TODO: potentially merge/replace existing..
                throw new ObjectStoreException("Calendar already exists");
            }

            try (FileWriter writer = new FileWriter(new File(getRoot(), uid.getValue() + ".ics"))) {
                new CalendarOutputter(false).output(c, writer);
            } catch (IOException e) {
                throw new ObjectStoreException("Error writing calendar file", e);
            }
        }

        // notify listeners..
        fireOnMergeEvent(this, calendar);

        return Arrays.stream(uidCalendars).map(Calendar::getUid).toArray(Uid[]::new);
    }

    @Override
    public Calendar export() {
        Calendar export = new Calendar();
        for (File object : getObjectFiles()) {
            try {
                export = export.merge(Calendars.load(object.getAbsolutePath()));
            } catch (IOException | ParserException e) {
                throw new RuntimeException(e);
            }
        }
        return export;
    }

//    @Override
//    public Iterable<Calendar> getAll() throws ObjectStoreException {
//        List<Calendar> calendars = new ArrayList<>();
//
//        File[] componentFiles = getObjectFiles();
//        if (componentFiles != null) {
//            try {
//                for (File file : componentFiles) {
//                    calendars.add(Calendars.load(file.getAbsolutePath()));
//                }
//            } catch (IOException | ParserException e) {
//                throw new ObjectStoreException(e);
//            }
//        }
//        return calendars;
//    }

    private File[] getObjectFiles() {
        return getRoot().listFiles(pathname ->
                !pathname.isDirectory() && pathname.getName().endsWith(".ics"));
    }
}

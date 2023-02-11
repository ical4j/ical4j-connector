package org.ical4j.connector.command;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.ObjectNotFoundException;
import picocli.CommandLine;

@CommandLine.Command(name = "get-calendar", description = "Retrieve a calendar object with specified UID")
public class GetCalendar extends AbstractCommand<CalendarCollection> {

    private final CalendarCollection collection;

    private String calendarUid;

    private Calendar calendar;

    public GetCalendar(CalendarCollection collection) {
        this.collection = collection;
    }

    public GetCalendar withCalendarUid(String calendarUid) {
        this.calendarUid = calendarUid;
        return this;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    @Override
    public void run() {
        try {
            this.calendar = collection.getCalendar(calendarUid);
        } catch (ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

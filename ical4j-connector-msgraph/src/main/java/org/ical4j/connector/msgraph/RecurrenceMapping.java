package org.ical4j.connector.msgraph;

import com.microsoft.graph.models.DayOfWeek;
import com.microsoft.graph.models.PatternedRecurrence;
import com.microsoft.graph.models.RecurrencePattern;
import com.microsoft.graph.models.RecurrencePatternType;
import com.microsoft.graph.models.RecurrenceRange;
import com.microsoft.graph.models.RecurrenceRangeType;
import com.microsoft.graph.models.WeekIndex;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.transform.recurrence.Frequency;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * Copyright (c) 2026, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Translates between iCalendar {@code RRULE} recurrence and Microsoft Graph's structured
 * {@link PatternedRecurrence}. Coverage is limited to the common cases (see the change design):
 * {@code FREQ=DAILY|WEEKLY|MONTHLY|YEARLY} with {@code INTERVAL}, {@code BYDAY}, {@code BYMONTHDAY},
 * {@code BYMONTH}, and {@code COUNT}/{@code UNTIL} ranges. Sub-daily frequencies and constructs Graph
 * cannot express (e.g. {@code BYSETPOS} beyond a single ordinal) are not mapped. {@code RDATE} and
 * {@code EXDATE} have no Graph equivalent and are handled (dropped) by the callers, not here.
 */
final class RecurrenceMapping {

    private static final DateTimeFormatter RRULE_DATE = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    private static final DateTimeFormatter RRULE_LOCAL_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private static final DateTimeFormatter RRULE_UTC_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private RecurrenceMapping() {
    }

    /**
     * Convert an iCalendar recurrence rule to a Graph {@link PatternedRecurrence}.
     *
     * @param recur     the iCal recurrence rule
     * @param startDate the series start date (from {@code DTSTART}) used for the recurrence range
     * @param zone      the series time zone, used to express {@code UNTIL} as a local end date
     * @return the mapped recurrence, or {@code null} when the rule can't be expressed as a Graph pattern
     */
    static PatternedRecurrence toPatternedRecurrence(Recur<?> recur, LocalDate startDate, ZoneId zone) {
        Frequency frequency = recur.getFrequency();
        if (frequency == null) {
            return null;
        }
        RecurrencePattern pattern = new RecurrencePattern();
        pattern.setInterval(recur.getInterval() > 0 ? recur.getInterval() : 1);

        List<WeekDay> days = recur.getDayList();
        List<Integer> monthDays = recur.getMonthDayList();

        switch (frequency) {
            case DAILY:
                pattern.setType(RecurrencePatternType.Daily);
                break;
            case WEEKLY:
                pattern.setType(RecurrencePatternType.Weekly);
                // Graph requires daysOfWeek for a weekly pattern; an RRULE without BYDAY repeats on DTSTART's weekday..
                pattern.setDaysOfWeek(days.isEmpty() ? List.of(toGraphDay(startDate.getDayOfWeek())) : toGraphDays(days));
                break;
            case MONTHLY:
            case YEARLY:
                boolean yearly = frequency == Frequency.YEARLY;
                if (yearly) {
                    if (recur.getMonthList().size() > 1) {
                        return null;
                    }
                    pattern.setMonth(recur.getMonthList().isEmpty()
                            ? startDate.getMonthValue() : recur.getMonthList().get(0).getMonthOfYear());
                }
                if (!days.isEmpty()) {
                    // Graph relative patterns carry one week index for all days, from BYDAY offsets or BYSETPOS..
                    WeekIndex index = toWeekIndex(days, recur.getSetPosList());
                    if (index == null) {
                        return null;
                    }
                    pattern.setType(yearly ? RecurrencePatternType.RelativeYearly : RecurrencePatternType.RelativeMonthly);
                    pattern.setIndex(index);
                    pattern.setDaysOfWeek(toGraphDays(days));
                } else {
                    if (monthDays.size() > 1 || (!monthDays.isEmpty() && monthDays.get(0) < 1)) {
                        return null;
                    }
                    pattern.setType(yearly ? RecurrencePatternType.AbsoluteYearly : RecurrencePatternType.AbsoluteMonthly);
                    pattern.setDayOfMonth(monthDays.isEmpty() ? startDate.getDayOfMonth() : monthDays.get(0));
                }
                break;
            default:
                // SECONDLY / MINUTELY / HOURLY have no Graph equivalent.
                return null;
        }

        RecurrenceRange range = new RecurrenceRange();
        range.setStartDate(startDate);
        if (recur.getCount() > 0) {
            range.setType(RecurrenceRangeType.Numbered);
            range.setNumberOfOccurrences(recur.getCount());
        } else if (recur.getUntil() != null) {
            range.setType(RecurrenceRangeType.EndDate);
            range.setEndDate(toLocalDate(recur.getUntil(), zone));
        } else {
            range.setType(RecurrenceRangeType.NoEnd);
        }

        PatternedRecurrence recurrence = new PatternedRecurrence();
        recurrence.setPattern(pattern);
        recurrence.setRange(range);
        return recurrence;
    }

    /**
     * Convert a Graph {@link PatternedRecurrence} to an iCalendar {@code RRULE} property value.
     *
     * @param recurrence the Graph recurrence
     * @param zone       the series time zone ({@code DTSTART} zone), or {@code null} for a floating series
     * @param allDay     whether the series is all-day ({@code DTSTART} is a DATE)
     * @return an {@code RRULE} value string (e.g. {@code "FREQ=WEEKLY;INTERVAL=1;BYDAY=MO"}), or
     * {@code null} when the recurrence has no usable pattern
     */
    static String toRruleValue(PatternedRecurrence recurrence, ZoneId zone, boolean allDay) {
        if (recurrence == null || recurrence.getPattern() == null
                || recurrence.getPattern().getType() == null) {
            return null;
        }
        RecurrencePattern pattern = recurrence.getPattern();
        StringBuilder sb = new StringBuilder("FREQ=").append(toFrequency(pattern.getType()));

        if (pattern.getInterval() != null) {
            sb.append(";INTERVAL=").append(pattern.getInterval());
        }

        switch (pattern.getType()) {
            case Weekly:
                appendByDay(sb, pattern.getDaysOfWeek(), 0);
                break;
            case AbsoluteMonthly:
                if (pattern.getDayOfMonth() != null) {
                    sb.append(";BYMONTHDAY=").append(pattern.getDayOfMonth());
                }
                break;
            case RelativeMonthly:
                appendByDay(sb, pattern.getDaysOfWeek(), toOffset(pattern.getIndex()));
                break;
            case AbsoluteYearly:
                if (pattern.getMonth() != null) {
                    sb.append(";BYMONTH=").append(pattern.getMonth());
                }
                if (pattern.getDayOfMonth() != null) {
                    sb.append(";BYMONTHDAY=").append(pattern.getDayOfMonth());
                }
                break;
            case RelativeYearly:
                if (pattern.getMonth() != null) {
                    sb.append(";BYMONTH=").append(pattern.getMonth());
                }
                appendByDay(sb, pattern.getDaysOfWeek(), toOffset(pattern.getIndex()));
                break;
            default:
                break;
        }

        RecurrenceRange range = recurrence.getRange();
        if (range != null && range.getType() != null) {
            if (range.getType() == RecurrenceRangeType.Numbered && range.getNumberOfOccurrences() != null) {
                sb.append(";COUNT=").append(range.getNumberOfOccurrences());
            } else if (range.getType() == RecurrenceRangeType.EndDate && range.getEndDate() != null) {
                sb.append(";UNTIL=").append(toUntil(range, zone, allDay));
            }
        }
        return sb.toString();
    }

    private static void appendByDay(StringBuilder sb, List<DayOfWeek> days, int offset) {
        if (days == null || days.isEmpty()) {
            return;
        }
        sb.append(";BYDAY=");
        for (int i = 0; i < days.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            if (offset != 0) {
                sb.append(offset);
            }
            sb.append(toIcalDay(days.get(i)));
        }
    }

    private static List<DayOfWeek> toGraphDays(List<WeekDay> days) {
        List<DayOfWeek> result = new ArrayList<>();
        for (WeekDay day : days) {
            result.add(toGraphDay(day.getDay()));
        }
        return result;
    }

    private static DayOfWeek toGraphDay(java.time.DayOfWeek day) {
        return toGraphDay(WeekDay.getWeekDay(day).getDay());
    }

    private static DayOfWeek toGraphDay(WeekDay.Day day) {
        switch (day) {
            case SU: return DayOfWeek.Sunday;
            case MO: return DayOfWeek.Monday;
            case TU: return DayOfWeek.Tuesday;
            case WE: return DayOfWeek.Wednesday;
            case TH: return DayOfWeek.Thursday;
            case FR: return DayOfWeek.Friday;
            case SA: return DayOfWeek.Saturday;
            default: return DayOfWeek.Monday;
        }
    }

    private static String toIcalDay(DayOfWeek day) {
        switch (day) {
            case Sunday: return "SU";
            case Monday: return "MO";
            case Tuesday: return "TU";
            case Wednesday: return "WE";
            case Thursday: return "TH";
            case Friday: return "FR";
            case Saturday: return "SA";
            default: return "MO";
        }
    }

    /**
     * Graph's range end date is an inclusive local date. UNTIL must match DTSTART's value type
     * (RFC 5545): a DATE for all-day series, otherwise the end of that day, in UTC for a zoned series
     * or as local time for a floating one.
     */
    private static String toUntil(RecurrenceRange range, ZoneId zone, boolean allDay) {
        LocalDate endDate = range.getEndDate();
        if (allDay) {
            return endDate.format(RRULE_DATE);
        }
        LocalDateTime endOfDay = endDate.atTime(LocalTime.of(23, 59, 59));
        ZoneId rangeZone = TimeZoneMapping.find(range.getRecurrenceTimeZone()).orElse(zone);
        if (rangeZone == null) {
            return endOfDay.format(RRULE_LOCAL_DATE_TIME);
        }
        return endOfDay.atZone(rangeZone).withZoneSameInstant(ZoneOffset.UTC).format(RRULE_UTC_DATE_TIME);
    }

    /**
     * @return the single Graph week index expressed by the BYDAY offsets (or BYSETPOS where the days have
     * no offsets), or {@code null} when there isn't exactly one index Graph supports
     */
    private static WeekIndex toWeekIndex(List<WeekDay> days, List<Integer> setPos) {
        Set<Integer> offsets = new HashSet<>();
        for (WeekDay day : days) {
            offsets.add(day.getOffset());
        }
        if (offsets.size() != 1) {
            return null;
        }
        int offset = offsets.iterator().next();
        if (offset == 0) {
            if (setPos.size() != 1) {
                return null;
            }
            offset = setPos.get(0);
        }
        switch (offset) {
            case 1: return WeekIndex.First;
            case 2: return WeekIndex.Second;
            case 3: return WeekIndex.Third;
            case 4: return WeekIndex.Fourth;
            case -1: return WeekIndex.Last;
            default: return null;
        }
    }

    private static int toOffset(WeekIndex index) {
        if (index == null) {
            return 0;
        }
        switch (index) {
            case First: return 1;
            case Second: return 2;
            case Third: return 3;
            case Fourth: return 4;
            case Last: return -1;
            default: return 0;
        }
    }

    private static String toFrequency(RecurrencePatternType type) {
        switch (type) {
            case Daily: return Frequency.DAILY.name();
            case Weekly: return Frequency.WEEKLY.name();
            case AbsoluteMonthly:
            case RelativeMonthly: return Frequency.MONTHLY.name();
            case AbsoluteYearly:
            case RelativeYearly: return Frequency.YEARLY.name();
            default: return Frequency.DAILY.name();
        }
    }

    /**
     * @return the date of {@code temporal} in the series zone; an {@code UNTIL} in UTC can fall on a
     * different calendar date than the local occurrence it bounds
     */
    private static LocalDate toLocalDate(Temporal temporal, ZoneId zone) {
        if (temporal instanceof LocalDate) {
            return (LocalDate) temporal;
        }
        if (temporal instanceof Instant) {
            return ((Instant) temporal).atZone(zone).toLocalDate();
        }
        if (temporal instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporal).withZoneSameInstant(zone).toLocalDate();
        }
        if (temporal instanceof OffsetDateTime) {
            return ((OffsetDateTime) temporal).atZoneSameInstant(zone).toLocalDate();
        }
        return LocalDate.from(temporal);
    }
}

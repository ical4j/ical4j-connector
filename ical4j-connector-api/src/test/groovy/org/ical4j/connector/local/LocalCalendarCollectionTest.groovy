package org.ical4j.connector.local

import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.ContentBuilder
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.util.Calendars
import net.fortuna.ical4j.util.RandomUidGenerator
import spock.lang.Specification

class LocalCalendarCollectionTest extends Specification {

    def 'test add calendar to collection'() {
        given: 'a local calendar collection'
        LocalCalendarStore calendarStore = [new File('build', 'local')]
        LocalCalendarCollection collection = calendarStore.addCollection('public_holidays')

        and: 'a calendar object'
        Calendar calendar = new ContentBuilder().with {
            calendar {
                prodid '-//Ben Fortuna//iCal4j 1.0//EN'
                version '2.0'
                vevent {
                    uid new RandomUidGenerator().generateUid()
                    dtstamp()
                    dtstart('20090810', parameters: parameters { value 'DATE' })
                    action 'DISPLAY'
                    attach('http://example.com/attachment', parameters: parameters() { value 'URI' })
                }
            }
        }

        when: 'the new calendar is added to the collection'
        collection.addCalendar(calendar)

        then: 'a new calendar file is created'
        new File('build/local/public_holidays',
                "${calendar.getComponent(Component.VEVENT).getProperty(Property.UID).getValue()}.ics").exists()
    }

    def 'test remove calendar from collection'() {
        given: 'a local calendar collection'
        LocalCalendarStore calendarStore = [new File('build', 'local')]
        LocalCalendarCollection collection = calendarStore.addCollection('public_holidays')

        and: 'a calendar object that is added to the collection'
        Calendar calendar = new ContentBuilder().with {
            calendar {
                prodid '-//Ben Fortuna//iCal4j 1.0//EN'
                version '2.0'
                vevent {
                    uid new RandomUidGenerator().generateUid()
                    dtstamp()
                    dtstart('20090810', parameters: parameters { value 'DATE' })
                    action 'DISPLAY'
                    attach('http://example.com/attachment', parameters: parameters() { value 'URI' })
                }
            }
        }
        collection.addCalendar(calendar)

        when: 'the calendar is removed from the collection'
        def removed = collection.removeCalendar(Calendars.getUid(calendar).value)

        then: 'the exsiting calendar file is deleted'
        !new File('build/local/public_holidays',
                "${calendar.getComponent(Component.VEVENT).getProperty(Property.UID).getValue()}.ics").exists()

        and: 'removed calendar is identical to added'
        removed == calendar
    }
}

package org.ical4j.connector.dav


import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.ContentBuilder
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.component.VFreeBusy
import net.fortuna.ical4j.model.property.DtEnd
import net.fortuna.ical4j.model.property.DtStart
import net.fortuna.ical4j.util.RandomUidGenerator
import org.ical4j.connector.ObjectStore
import org.ical4j.connector.ObjectStoreException
import spock.lang.Ignore
import spock.lang.IgnoreIf

import java.time.Instant
import java.time.ZonedDateTime

abstract class AbstractCalendarStoreIntegrationTest extends AbstractIntegrationTest {

    def 'test find calendar home set'() {
        given: 'an object store'
        def store = new CalDavCalendarStore('ical4j-connector', URI.create(getContainerUrl()).toURL(),
            getPathResolver())

        and: 'a connection is established'
        store.connect(new DavSessionConfiguration().withCredentialsProvider(getCredentialsProvider())
                .withUser(getUser()).withWorkspace(getWorkspace()))

        and: 'a collection is created'
        def collection = store.addCollection('testCollection5')

        when: 'calendar home set it requested'
        def calendarHomeSet = store.findCalendarHomeSet()

        then: 'the calendar home set is retrieved'
        calendarHomeSet == expectedValues['calendar-home-set']

        cleanup:
        collection.delete()
    }

    @Ignore('not working for radicale and baikal')
    def 'test collection creation'() {
        given: 'an object store'
        def store = new CalDavCalendarStore('ical4j-connector', URI.create(getContainerUrl()).toURL(),
            getPathResolver())

        and: 'a connection is established'
        store.connect(new DavSessionConfiguration().withCredentialsProvider(getCredentialsProvider())
                .withUser(getUser()).withWorkspace(getWorkspace()))

        when: 'a new collection is added'
        def collection = store.addCollection('test1')
        def collection2 = store.addCollection('test2')

        then: 'the collection is created'
        collection != null

        and: 'collections size matches expected'
        store.getCollections().size() == 1

        cleanup:
        collection.delete()
    }

    def 'test listObjectUIDs returns added event UIDs'() {
        given: 'a connected store with a collection'
        def store = connectedStore()
        def collection = store.addCollection('uids-listed')

        and: 'three calendars with distinct UIDs are added'
        def uid1 = newCalendarUid()
        def uid2 = newCalendarUid()
        def uid3 = newCalendarUid()
        collection.add(newCalendar(uid1))
        collection.add(newCalendar(uid2))
        collection.add(newCalendar(uid3))

        when: 'object UIDs are listed'
        def uids = collection.listObjectUIDs()

        then: 'the result contains exactly the three added UIDs'
        uids.size() == 3
        uids.toSet() == [uid1, uid2, uid3].toSet()

        cleanup:
        collection.delete()
    }

    def 'test listObjectUIDs returns empty list on empty collection'() {
        given: 'a connected store with an empty collection'
        def store = connectedStore()
        def collection = store.addCollection('uids-empty')

        when: 'object UIDs are listed'
        def uids = collection.listObjectUIDs()

        then: 'the result is an empty list (not null)'
        uids != null
        uids.isEmpty()

        cleanup:
        collection.delete()
    }

    def 'test getCollections accepts DEFAULT_WORKSPACE'() {
        given: 'a connected store'
        def store = connectedStore()

        expect: 'null workspace is accepted'
        store.getCollections(null) != null

        and: 'DEFAULT_WORKSPACE is accepted'
        store.getCollections(ObjectStore.DEFAULT_WORKSPACE) != null
    }

    def 'test getCollections rejects non-default workspace'() {
        given: 'a connected store'
        def store = connectedStore()

        when:
        store.getCollections('some-other-workspace')

        then:
        thrown(ObjectStoreException)
    }

    def 'test listWorkspaceIds returns default only'() {
        given: 'a connected store'
        def store = connectedStore()

        expect:
        store.listWorkspaceIds() == [ObjectStore.DEFAULT_WORKSPACE]
    }

    def 'test getObjectsByMultiget returns requested events'() {
        given: 'a connected store with three events'
        def store = connectedStore()
        def collection = store.addCollection('multiget')
        def uid1 = newCalendarUid()
        def uid2 = newCalendarUid()
        def uid3 = newCalendarUid()
        collection.add(newCalendar(uid1))
        collection.add(newCalendar(uid2))
        collection.add(newCalendar(uid3))

        when: 'multiget is invoked for two of the three hrefs'
        def base = hrefBase(collection)
        def result = collection.getObjectsByMultiget([base + uid1 + '.ics', base + uid3 + '.ics'])

        then: 'two calendars are returned'
        result.size() == 2

        and: 'the UIDs match'
        result.collect { uidOf(it) }.toSet() == [uid1, uid3].toSet()

        cleanup:
        collection.delete()
    }

    @IgnoreIf({ instance.getClass().simpleName.contains('Baikal') })
    // Baikal returns 403 for the entire multiget if any href is missing, rather than
    // returning per-href status codes. Server-specific behaviour; the spec scenario is
    // satisfied by per-href-status servers like Radicale.
    def 'test getObjectsByMultiget skips missing hrefs'() {
        given: 'a connected store with one event'
        def store = connectedStore()
        def collection = store.addCollection('multiget-missing')
        def uid = newCalendarUid()
        collection.add(newCalendar(uid))

        when: 'multiget is invoked with one valid and one missing href'
        def base = hrefBase(collection)
        def result = collection.getObjectsByMultiget([base + uid + '.ics', base + 'does-not-exist.ics'])

        then: 'only the existing one is returned'
        result.size() == 1
        uidOf(result[0]) == uid

        cleanup:
        collection.delete()
    }

    def 'test getObjectsByMultiget empty input returns empty list'() {
        given: 'a connected store with a collection'
        def store = connectedStore()
        def collection = store.addCollection('multiget-empty')

        when:
        def result = collection.getObjectsByMultiget([])

        then:
        result != null
        result.isEmpty()

        cleanup:
        collection.delete()
    }

    @IgnoreIf({ instance.getClass().simpleName.contains('Radicale') })
    // Radicale's free-busy-query implementation does not appear to populate FREEBUSY
    // intervals from collection events. Server-specific limitation; the test asserts
    // structural correctness only (server returned VFREEBUSY with at least one
    // FREEBUSY interval) on Baikal which does populate them.
    def 'test doFreeBusyQuery returns busy interval for covering event'() {
        given: 'a connected store with a timed event'
        def store = connectedStore()
        def collection = store.addCollection('freebusy-busy')
        def start = ZonedDateTime.parse('2030-01-01T10:00:00Z').toInstant()
        def end = ZonedDateTime.parse('2030-01-01T11:00:00Z').toInstant()
        collection.add(newTimedCalendar(newCalendarUid(), start, end))

        when: 'free-busy is queried over a covering window'
        def queryStart = ZonedDateTime.parse('2030-01-01T00:00:00Z').toInstant()
        def queryEnd = ZonedDateTime.parse('2030-01-02T00:00:00Z').toInstant()
        def result = collection.doFreeBusyQuery(queryStart, queryEnd)

        then: 'the returned Calendar contains a VFREEBUSY'
        result != null
        VFreeBusy fb = result.getComponent(Component.VFREEBUSY).orElse(null)
        fb != null

        and: 'at least one FREEBUSY interval is reported (exact value is server-tz-dependent on Baikal)'
        !fb.getProperties('FREEBUSY').isEmpty()

        cleanup:
        collection.delete()
    }

    @IgnoreIf({ instance.getClass().simpleName.contains('Radicale') })
    // Radicale returns an empty response body for a free-busy-query against an empty
    // range rather than a VFREEBUSY with no FREEBUSY intervals. Server-specific
    // behaviour; the spec scenario is satisfied by spec-compliant servers like Baikal.
    def 'test doFreeBusyQuery returns empty VFREEBUSY for range with no events'() {
        given: 'a connected store with an empty collection'
        def store = connectedStore()
        def collection = store.addCollection('freebusy-empty')

        when: 'free-busy is queried over any window'
        def queryStart = ZonedDateTime.parse('2030-01-01T00:00:00Z').toInstant()
        def queryEnd = ZonedDateTime.parse('2030-01-02T00:00:00Z').toInstant()
        def result = collection.doFreeBusyQuery(queryStart, queryEnd)

        then: 'the result is a Calendar with a VFREEBUSY with no FREEBUSY intervals'
        result != null
        VFreeBusy fb = result.getComponent(Component.VFREEBUSY).orElse(null)
        fb != null
        fb.getProperties('FREEBUSY').isEmpty()

        cleanup:
        collection.delete()
    }

    def 'test export aggregates added events'() {
        given: 'a connected store with three events'
        def store = connectedStore()
        def collection = store.addCollection('export-aggregate')
        def uid1 = newCalendarUid()
        def uid2 = newCalendarUid()
        def uid3 = newCalendarUid()
        collection.add(newCalendar(uid1))
        collection.add(newCalendar(uid2))
        collection.add(newCalendar(uid3))

        when: 'the collection is exported'
        def exported = collection.export()

        then: 'the result aggregates all three VEVENTs'
        def events = exported.getComponents(Component.VEVENT)
        events.size() == 3
        events.collect { it.getRequiredProperty(Property.UID).value }.toSet() == [uid1, uid2, uid3].toSet()

        cleanup:
        collection.delete()
    }

    def 'test export empty collection returns empty calendar'() {
        given: 'a connected store with an empty collection'
        def store = connectedStore()
        def collection = store.addCollection('export-empty')

        when:
        def exported = collection.export()

        then:
        exported != null
        exported.getComponents(Component.VEVENT).isEmpty()

        cleanup:
        collection.delete()
    }

    private CalDavCalendarStore connectedStore() {
        def store = new CalDavCalendarStore('ical4j-connector', URI.create(getContainerUrl()).toURL(),
                getPathResolver())
        store.connect(new DavSessionConfiguration().withCredentialsProvider(getCredentialsProvider())
                .withUser(getUser()).withWorkspace(getWorkspace()))
        store
    }

    private static String newCalendarUid() {
        new RandomUidGenerator().generateUid().value
    }

    private static net.fortuna.ical4j.model.Calendar newCalendar(String uidValue) {
        new ContentBuilder().calendar {
            prodid '-//iCal4j Connector//tests//EN'
            version '2.0'
            vevent {
                uid uidValue
                dtstamp()
                dtstart('20300101', parameters: parameters { value 'DATE' })
                summary 'test event'
            }
        }
    }

    private static final java.time.format.DateTimeFormatter UTC_FMT =
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(java.time.ZoneOffset.UTC)

    private static net.fortuna.ical4j.model.Calendar newTimedCalendar(String uidValue, Instant start, Instant end) {
        def cal = new ContentBuilder().calendar {
            prodid '-//iCal4j Connector//tests//EN'
            version '2.0'
            vevent {
                uid uidValue
                dtstamp()
                summary 'timed test event'
            }
        }
        def vevent = cal.getComponent(Component.VEVENT).get()
        // explicit string construction with Z suffix forces UTC serialization;
        // passing a raw Instant via DtStart<>(instant) ends up written as floating
        // local time which servers (e.g. Baikal) reinterpret in their configured zone.
        vevent.add(new DtStart<>(UTC_FMT.format(start)))
        vevent.add(new DtEnd<>(UTC_FMT.format(end)))
        cal
    }

    static String uidOf(net.fortuna.ical4j.model.Calendar calendar) {
        calendar.getComponent(Component.VEVENT).get().getRequiredProperty(Property.UID).value
    }

    private String hrefBase(CalDavCalendarCollection collection) {
        def repo = getRepositoryPath() ?: ''
        if (repo == '/') repo = ''
        def base = repo + collection.getPath()
        base.endsWith('/') ? base : base + '/'
    }
}

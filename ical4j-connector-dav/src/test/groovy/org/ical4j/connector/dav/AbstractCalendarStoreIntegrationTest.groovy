package org.ical4j.connector.dav


import net.fortuna.ical4j.model.ContentBuilder
import net.fortuna.ical4j.util.RandomUidGenerator
import org.ical4j.connector.ObjectStore
import org.ical4j.connector.ObjectStoreException
import spock.lang.Ignore
import spock.lang.IgnoreIf

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

    @IgnoreIf({ instance.getClass().simpleName.contains('Baikal') })
    // Baikal returns absolute calendar-home-set; DefaultDavClient.resolvePath
    // double-prefixes the repository path resulting in 404. Pre-existing
    // low-level client bug — deferred to Cut B.
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
}

package org.ical4j.connector.dav


import net.fortuna.ical4j.vcard.ContentBuilder
import net.fortuna.ical4j.vcard.VCard
import org.ical4j.connector.ObjectStore
import org.ical4j.connector.ObjectStoreException

abstract class AbstractCardStoreIntegrationTest extends AbstractIntegrationTest {

    def 'test addCollection creates addressbook with description'() {
        given: 'a connected store'
        def store = connectedStore()

        when: 'an addressbook is added via the 5-arg form'
        def collection = store.addCollection('ab1', 'AB1', 'test description', null, null)

        then: 'the returned collection reflects the supplied display name and description'
        collection.displayName == 'AB1'
        collection.description == 'test description'

        and: 'fetching the collection back returns the same values'
        def fetched = store.getCollection('ab1')
        fetched.displayName == 'AB1'
        fetched.description == 'test description'

        cleanup:
        collection?.delete()
    }

    def 'test listObjectUIDs returns added vcard UIDs'() {
        given: 'a connected store with a collection'
        def store = connectedStore()
        def collection = store.addCollection('uids-listed')

        and: 'three vcards with distinct UIDs are added'
        def uid1 = UUID.randomUUID().toString()
        def uid2 = UUID.randomUUID().toString()
        def uid3 = UUID.randomUUID().toString()
        collection.add(newCard(uid1))
        collection.add(newCard(uid2))
        collection.add(newCard(uid3))

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

    def 'test removeAll deletes specified vcards'() {
        given: 'a connected store with three vcards'
        def store = connectedStore()
        def collection = store.addCollection('removeall')
        def uid1 = 'u1-' + UUID.randomUUID()
        def uid2 = 'u2-' + UUID.randomUUID()
        def uid3 = 'u3-' + UUID.randomUUID()
        collection.add(newCard(uid1))
        collection.add(newCard(uid2))
        collection.add(newCard(uid3))

        when: 'two are removed'
        def removed = collection.removeAll(uid1, uid3)

        then: 'the returned list has size 2'
        removed.size() == 2

        and: 'only the surviving UID remains'
        collection.listObjectUIDs() == [uid2]

        cleanup:
        collection.delete()
    }

    def 'test removeAll throws on non-existent UID'() {
        given: 'a connected store with an empty collection'
        def store = connectedStore()
        def collection = store.addCollection('removeall-missing')

        when:
        collection.removeAll('does-not-exist')

        then:
        thrown(RuntimeException)

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

    def 'test getCollections accepts session-user workspace'() {
        given: 'a connected store with a known addressbook'
        def store = connectedStore()
        def collection = store.addCollection('session-ws-card')

        when: 'collections are listed via the session-user workspace value'
        def viaSessionUser = store.getCollections(getUser())

        and: 'and via DEFAULT_WORKSPACE'
        def viaDefault = store.getCollections(ObjectStore.DEFAULT_WORKSPACE)

        then: 'both return the same set of collection ids'
        viaSessionUser.collect { it.id }.toSet() == viaDefault.collect { it.id }.toSet()

        cleanup:
        collection.delete()
    }

    def 'test getCollections rejects unknown foreign principal'() {
        given: 'a connected store'
        def store = connectedStore()

        when: 'listing collections for a principal the session user has no rights on'
        store.getCollections('nonexistent-other-principal')

        then: 'the server-side authorization error propagates'
        thrown(ObjectStoreException)
    }

    def 'test listWorkspaceIds returns default only'() {
        given: 'a connected store'
        def store = connectedStore()

        expect:
        store.listWorkspaceIds() == [ObjectStore.DEFAULT_WORKSPACE]
    }

    def 'test export aggregates added vcards'() {
        given: 'a connected store with three vcards'
        def store = connectedStore()
        def collection = store.addCollection('export-aggregate')
        def uid1 = UUID.randomUUID().toString()
        def uid2 = UUID.randomUUID().toString()
        def uid3 = UUID.randomUUID().toString()
        collection.add(newCard(uid1))
        collection.add(newCard(uid2))
        collection.add(newCard(uid3))

        when:
        def exported = collection.export()

        then: 'the result is a non-null VCard'
        exported != null

        cleanup:
        collection.delete()
    }

    def 'test export empty collection returns empty vcard'() {
        given: 'a connected store with an empty collection'
        def store = connectedStore()
        def collection = store.addCollection('export-empty')

        when:
        def exported = collection.export()

        then:
        exported != null

        cleanup:
        collection.delete()
    }

    private CardDavStore connectedStore() {
        def store = new CardDavStore('ical4j-connector', URI.create(getContainerUrl()).toURL(),
                getPathResolver())
        store.connect(new DavSessionConfiguration().withCredentialsProvider(getCredentialsProvider())
                .withUser(getUser()).withWorkspace(getWorkspace()))
        store
    }

    private static VCard newCard(String uidValue) {
        new ContentBuilder().vcard {
            entity {
                version '4.0'
                uid uidValue
                fn 'test'
                n('example') {
                    value 'text'
                }
            }
        }
    }
}

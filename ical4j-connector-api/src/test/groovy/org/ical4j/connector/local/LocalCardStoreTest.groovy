package org.ical4j.connector.local


import net.fortuna.ical4j.model.DefaultTimeZoneRegistryFactory
import net.fortuna.ical4j.util.Calendars
import net.fortuna.ical4j.vcard.VCard
import org.ical4j.connector.event.ObjectStoreEvent
import org.ical4j.connector.event.ObjectStoreListener

class LocalCardStoreTest extends AbstractLocalTest {

    def 'test create collection'() {
        given: 'a new local card store'
        LocalCardStore cardStore = [storeLocation]

        and: 'a store listener'
        ObjectStoreEvent<VCard> event
        ObjectStoreListener<VCard> listener = { event = it } as ObjectStoreListener
        cardStore.addObjectStoreListener(listener)

        when: 'a new collection is added'
        def collection = cardStore.addCollection('contacts')

        then: 'a local collection directory is added'
        new File(workspaceLocation, 'contacts').exists()

        and: 'the listener is notified'
        event != null && event.collection == collection
    }

    def 'test create and initialise collection'() {
        given: 'a new local card store'
        LocalCardStore cardStore = [storeLocation]

        and: 'a timezone card'
        def timezone = Calendars.wrap(new DefaultTimeZoneRegistryFactory().createRegistry()
                .getTimeZone('Australia/Melbourne').getVTimeZone())

        when: 'a new collection is added'
        def collection = cardStore.addCollection('contacts', 'Contacts',
                'Personal Contacts', ['VCARD'] as String[], timezone)

        then: 'a local collection directory is added'
        new File(workspaceLocation, 'contacts').exists()

        and: 'the collection properties are saved'
        collection.displayName == 'Contacts'
        collection.description == 'Personal Contacts'
        collection.supportedComponentTypes == ['VCARD'] as String[]
        collection.timeZone == timezone
    }
}

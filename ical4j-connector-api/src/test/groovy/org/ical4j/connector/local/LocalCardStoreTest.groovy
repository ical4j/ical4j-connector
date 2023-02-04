package org.ical4j.connector.local

import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.DefaultTimeZoneRegistryFactory
import net.fortuna.ical4j.util.Calendars

class LocalCardStoreTest extends AbstractLocalTest {

    def 'test create collection'() {
        given: 'a new local card store'
        LocalCardStore cardStore = [storeLocation]

        when: 'a new collection is added'
        LocalCardCollection collection = cardStore.addCollection('contacts')

        then: 'a local collection directory is added'
        new File(storeLocation, 'contacts').exists()
    }

    def 'test create and initialise collection'() {
        given: 'a new local card store'
        LocalCardStore cardStore = [storeLocation]

        and: 'a timezone card'
        Calendar timezone = Calendars.wrap(new DefaultTimeZoneRegistryFactory().createRegistry()
                .getTimeZone('Australia/Melbourne').getVTimeZone())

        when: 'a new collection is added'
        LocalCardCollection collection = cardStore.addCollection('contacts', 'Contacts',
                'Personal Contacts', ['VCARD'] as String[], timezone)

        then: 'a local collection directory is added'
        new File(storeLocation, 'contacts').exists()

        and: 'the collection properties are saved'
        collection.displayName == 'Contacts'
        collection.description == 'Personal Contacts'
        collection.supportedComponentTypes == ['VCARD'] as String[]
        collection.timeZone == timezone
    }
}

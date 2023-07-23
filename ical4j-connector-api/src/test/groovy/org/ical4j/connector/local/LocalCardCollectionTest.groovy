package org.ical4j.connector.local


import net.fortuna.ical4j.vcard.ContentBuilder
import net.fortuna.ical4j.vcard.PropertyName
import net.fortuna.ical4j.vcard.VCard
import org.ical4j.connector.event.ObjectCollectionEvent
import org.ical4j.connector.event.ObjectCollectionListener
import spock.lang.Shared

class LocalCardCollectionTest extends AbstractLocalTest {

    @Shared
    VCard card

    def setupSpec() {
        card = new ContentBuilder().vcard() {
            version '4.0'
            uid UUID.randomUUID().toString()
            fn 'test'
            n('example') {
                value 'text'
            }
            photo(value: 'http://example.com', parameters: [value('uri')])
        }
    }

    def 'test add card to collection'() {
        given: 'a local card collection'
        LocalCardStore cardStore = [storeLocation]
        LocalCardCollection collection = cardStore.addCollection('contacts')

        and: 'a collection listener'
        ObjectCollectionEvent<VCard> event
        ObjectCollectionListener<VCard> listener = { event = it } as ObjectCollectionListener
        collection.addObjectCollectionListener(listener)

        when: 'the new card is added to the collection'
        collection.addCard(card)

        then: 'a new card file is created'
        new File(storeLocation,
                "contacts/${card.getRequiredProperty(PropertyName.UID as String).getValue()}.vcf").exists()

        and: 'the listener is notified'
        event != null && event.object == card
    }

    def 'test remove card from collection'() {
        given: 'a local card collection'
        LocalCardStore cardStore = [storeLocation]
        LocalCardCollection collection = cardStore.addCollection('contacts')

        and: 'a card object added'
        collection.addCard(card)

        when: 'the card is removed'
        def removed = collection.removeCard(card.getRequiredProperty(PropertyName.UID as String).value)

        then: 'the existing card file is deleted'
        !new File(storeLocation,
                "contacts/${card.getRequiredProperty(PropertyName.UID as String).getValue()}.vcf").exists()

        and: 'removed card is identical to added'
        removed == card
    }

    def 'test get card from collection'() {
        given: 'a local card collection'
        LocalCardStore cardStore = [storeLocation]
        LocalCardCollection collection = cardStore.addCollection('contacts')

        and: 'a card object added'
        collection.addCard(card)

        when: 'the card is removed'
        def retrieved = collection.getCard(card.getRequiredProperty(PropertyName.UID as String).value)

        then: 'retrieved card is identical to added'
        retrieved == card
    }

    def 'test list object uids in collection'() {
        given: 'a local card collection'
        LocalCardStore cardStore = [storeLocation]
        LocalCardCollection collection = cardStore.addCollection('contacts')

        and: 'a card object that is added to the collection'
        collection.addCard(card)

        when: 'the collection object uids are listed'
        def uids = collection.listObjectUids()

        then: 'the added calendar uid is in the list'
        uids.contains(card.getRequiredProperty(PropertyName.UID as String).value)
    }

    def 'test export collection'() {
        given: 'a local card collection'
        LocalCardStore cardStore = [storeLocation]
        LocalCardCollection collection = cardStore.addCollection('contacts')

        and: 'a card object that is added to the collection'
        collection.addCard(card)

        when: 'the collection is exported'
        def export = collection.export()

        then: 'the exported collection is identical to added'
        export == [card] as VCard[]
    }
}

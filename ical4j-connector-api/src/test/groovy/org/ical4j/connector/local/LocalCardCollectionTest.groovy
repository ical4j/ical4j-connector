package org.ical4j.connector.local

import net.fortuna.ical4j.vcard.ContentBuilder
import net.fortuna.ical4j.vcard.PropertyName

class LocalCardCollectionTest extends AbstractLocalTest {

    def 'test add card to collection'() {
        given: 'a local card collection'
        LocalCardStore cardStore = [storeLocation]
        LocalCardCollection collection = cardStore.addCollection('contacts')

        and: 'a card object'
        def card = new ContentBuilder().vcard() {
            version '4.0'
            uid UUID.randomUUID().toString()
            fn 'test'
            n('example') {
                value 'text'
            }
            photo(value: 'http://example.com', parameters: [value('uri')])
        }

        when: 'the new card is added to the collection'
        collection.addCard(card)

        then: 'a new card file is created'
        new File(storeLocation,
                "contacts/${card.getRequiredProperty(PropertyName.UID as String).getValue()}.vcf").exists()
    }

    def 'test remove card from collection'() {
        given: 'a local card collection'
        LocalCardStore cardStore = [storeLocation]
        LocalCardCollection collection = cardStore.addCollection('contacts')

        and: 'a card object added'
        def card = new ContentBuilder().vcard() {
            version '4.0'
            uid UUID.randomUUID().toString()
            fn 'test'
            n('example') {
                value 'text'
            }
            photo(value: 'http://example.com', parameters: [value('uri')])
        }
        collection.addCard(card)

        when: 'the card is removed'
        def removed = collection.removeCard(card.getRequiredProperty(PropertyName.UID as String).value)

        then: 'the existing card file is deleted'
        !new File(storeLocation,
                "contacts/${card.getRequiredProperty(PropertyName.UID as String).getValue()}.vcf").exists()

        and: 'removed card is identical to added'
        removed == card
    }
}

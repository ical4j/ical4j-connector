package net.fortuna.ical4j.connector.local

import net.fortuna.ical4j.vcard.ContentBuilder
import net.fortuna.ical4j.vcard.Property
import spock.lang.Specification

class LocalCardCollectionTest extends Specification {

    def 'test add card to collection'() {
        given: 'a local card collection'
        LocalCardStore cardStore = [new File('build', 'local')]
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
        new File('build/local/contacts',
                "${card.getProperty(Property.Id.UID).getValue()}.vcf").exists()
    }
}

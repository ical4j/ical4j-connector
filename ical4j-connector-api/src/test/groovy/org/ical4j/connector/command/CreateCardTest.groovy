package org.ical4j.connector.command


import net.fortuna.ical4j.vcard.VCard
import org.ical4j.connector.CardCollection
import org.ical4j.connector.CardStore
import spock.lang.Specification

class CreateCardTest extends Specification {

    def 'test create card'() {
        given: 'a mock card store'
        CardStore store = Mock()
        CardCollection collection = Mock()

        and: 'a vcard instance'
        VCard card = []

        when: 'a create card command is run'
        new CreateCard(store).withCard(card).run()

        then: 'collection add card is invoked'
        1 * store.getCollection('default') >> collection
        1 * collection.addCard(card)
    }
}

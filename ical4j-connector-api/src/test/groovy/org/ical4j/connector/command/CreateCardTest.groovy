package org.ical4j.connector.command


import net.fortuna.ical4j.vcard.VCard
import org.ical4j.connector.CardCollection
import spock.lang.Specification

class CreateCardTest extends Specification {

    def 'test create card'() {
        given: 'a mock card collection'
        CardCollection collection = Mock()

        and: 'a vcard instance'
        VCard card = []

        when: 'a create card command is run'
        new CreateCard(collection).withCard(card).run()

        then: 'collection add card is invoked'
        1 * collection.addCard(card)
    }
}

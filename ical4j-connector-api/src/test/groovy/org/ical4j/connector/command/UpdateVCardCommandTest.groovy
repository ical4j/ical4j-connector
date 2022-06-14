package org.ical4j.connector.command


import net.fortuna.ical4j.vcard.VCard
import org.ical4j.connector.CardCollection
import spock.lang.Specification

class UpdateVCardCommandTest extends Specification {

    def 'test update card'() {
        given: 'a mock card collection'
        CardCollection collection = Mock()

        and: 'a vcard instance'
        VCard card = []

        when: 'an update card command is run'
        new UpdateVCardCommand(collection).withCard(card).run()

        then: 'collection merge is invoked'
        1 * collection.merge(card)
    }
}

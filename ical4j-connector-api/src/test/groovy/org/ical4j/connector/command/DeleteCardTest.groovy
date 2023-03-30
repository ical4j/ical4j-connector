package org.ical4j.connector.command


import org.ical4j.connector.CardCollection
import org.ical4j.connector.CardStore
import spock.lang.Specification

class DeleteCardTest extends Specification {

    def 'test delete card'() {
        given: 'a mock card collection'
        CardStore store = Mock()
        CardCollection collection = Mock()

        when: 'a delete card command is run'
        new CardCommand.DeleteCard('default', (card) -> {}, store).withCardUid('1234').run()

        then: 'collection remove card is invoked'
        1 * store.getCollection('default') >> collection
        1 * collection.removeCard('1234')
    }
}

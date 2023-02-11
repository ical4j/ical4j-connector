package org.ical4j.connector.command


import org.ical4j.connector.CardCollection
import spock.lang.Specification

class DeleteCardTest extends Specification {

    def 'test delete card'() {
        given: 'a mock card collection'
        CardCollection collection = Mock()

        when: 'a delete card command is run'
        new DeleteCard(collection).withCardUid('1234').run()

        then: 'collection remove card is invoked'
        1 * collection.removeCard('1234')
    }
}

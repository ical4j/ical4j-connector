package org.ical4j.connector.command


import org.ical4j.connector.CardCollection
import spock.lang.Specification

class GetCardTest extends Specification {

    def 'test get card'() {
        given: 'a mock card collection'
        CardCollection collection = Mock()

        when: 'a get card command is run'
        new GetCard(collection).withCardUid('1234').run()

        then: 'collection get card is invoked'
        1 * collection.getCard('1234')
    }
}

package org.ical4j.connector.command


import org.ical4j.connector.CardStore
import spock.lang.Specification

class GetVCardCollectionCommandTest extends Specification {

    def 'test get collection'() {
        given: 'a mock card store'
        CardStore store = Mock()

        when: 'a get vcard collection command is run'
        new GetVCardCollectionCommand(store).withCollectionName('testCollection').run()

        then: 'store get collection is invoked'
        1 * store.getCollection('testCollection')
    }
}

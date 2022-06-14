package org.ical4j.connector.command


import org.ical4j.connector.CardStore
import spock.lang.Specification

class DeleteVCardCollectionCommandTest extends Specification {

    def 'test delete collection'() {
        given: 'a mock card store'
        CardStore store = Mock()

        when: 'a delete vcard collection command is run'
        new DeleteVCardCollectionCommand(store).withCollectionName('testCollection').run()

        then: 'store remove collection is invoked'
        1 * store.removeCollection('testCollection')
    }
}

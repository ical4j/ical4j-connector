package org.ical4j.connector.command


import org.ical4j.connector.ObjectStore
import spock.lang.Specification

class DeleteCollectionTest extends Specification {

    def 'test delete collection'() {
        given: 'a mock card store'
        ObjectStore store = Mock()

        when: 'a delete collection command is run'
        new DeleteCollection((collection) -> {}, store).withCollectionName('testCollection').run()

        then: 'store remove collection is invoked'
        1 * store.removeCollection('testCollection')
    }
}

package org.ical4j.connector.command


import org.ical4j.connector.ObjectStore
import spock.lang.Specification

class CreateCollectionTest extends Specification {

    def 'test create collection'() {
        given: 'a mock card store'
        ObjectStore store = Mock()

        when: 'a create collection command is run'
        new CreateCollection(store).withCollectionName('testCollection').run()

        then: 'store add collection is invoked'
        1 * store.addCollection('testCollection')
    }
}

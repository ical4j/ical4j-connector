package org.ical4j.connector.command


import org.ical4j.connector.CardStore
import spock.lang.Specification

class CreateVCardCollectionCommandTest extends Specification {

    def 'test create collection'() {
        given: 'a mock card store'
        CardStore store = Mock()

        when: 'a create vcard collection command is run'
        new CreateVCardCollectionCommand(store).withCollectionName('testCollection').run()

        then: 'store add collection is invoked'
        1 * store.addCollection('testCollection')
    }
}

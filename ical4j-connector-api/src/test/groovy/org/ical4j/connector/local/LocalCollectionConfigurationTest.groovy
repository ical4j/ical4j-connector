package org.ical4j.connector.local

import spock.lang.Specification

class LocalCollectionConfigurationTest extends Specification {

    def 'test set display name'() {
        given: 'a temp config directory'
        def configDir = File.createTempDir()

        and: 'a local configuration'
        LocalCollectionConfiguration configuration = [configDir]

        when: 'display name is set'
        configuration.displayName = 'Test Display Name'

        then: 'display name is persisted in memory'
        configuration.displayName == 'Test Display Name'

        and: 'and on disk'
        new LocalCollectionConfiguration(configDir).displayName == 'Test Display Name'
    }
}

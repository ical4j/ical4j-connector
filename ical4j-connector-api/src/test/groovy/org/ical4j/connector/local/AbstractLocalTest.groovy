package org.ical4j.connector.local

import spock.lang.Shared
import spock.lang.Specification

abstract class AbstractLocalTest extends Specification {

    @Shared
    File storeLocation, workspaceLocation

    def setup() {
        storeLocation = ['build', getClass().name]
        workspaceLocation = [storeLocation, 'default']
    }

    def cleanup() {
        storeLocation.deleteDir()
    }
}

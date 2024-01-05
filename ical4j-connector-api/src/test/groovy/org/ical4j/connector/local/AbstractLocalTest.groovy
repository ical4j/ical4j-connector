package org.ical4j.connector.local

import spock.lang.Shared
import spock.lang.Specification

abstract class AbstractLocalTest extends Specification {

    @Shared
    File storeLocation

    def setup() {
        storeLocation = ['build', getClass().name]
    }

    def cleanup() {
        storeLocation.deleteDir()
    }
}

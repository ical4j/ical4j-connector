package org.ical4j.connector.dav

import spock.lang.Specification

class PathResolverTest extends Specification {

    def 'verify principal path resolution'() {
        when: 'a principal path is resolved'
        def path = pathResolver.getPrincipalPath('testcal')

        then: 'the result is as expected'
        path == expectedPath

        where:
        pathResolver                            | expectedPath
        PathResolver.Defaults.RADICALE        | '/testcal'
        PathResolver.Defaults.BAIKAL          | '/testcal'
        PathResolver.Defaults.BEDEWORK        | '/principals/users/testcal/'
        PathResolver.Defaults.CALENDAR_SERVER | '/testcal/'
    }

    def 'verify calendar path resolution'() {
        when: 'a calendar path is resolved'
        def path = pathResolver.getRepositoryPath('testcal', '')

        then: 'the result is as expected'
        path == expectedPath

        where:
        pathResolver                            | expectedPath
        PathResolver.Defaults.RADICALE        | '/testcal'
        PathResolver.Defaults.BAIKAL          | '/dav.php/calendars/testcal'
        PathResolver.Defaults.BEDEWORK        | '/ucaldav/users/testcal'
        PathResolver.Defaults.CALENDAR_SERVER | '/dav/testcal'
    }

    def 'verify calendar id resolution'() {
        when: 'a calendar id is resolved from a path'
        def id = pathResolver.getResourceName(repositoryPath, '')

        then: 'the result is as expected'
        id == 'testcal'

        where:
        pathResolver                            | repositoryPath
        PathResolver.Defaults.RADICALE        | '/testcal'
        PathResolver.Defaults.BAIKAL          | '/calendars/testcal'
        PathResolver.Defaults.BEDEWORK        | '/users/testcal'
        PathResolver.Defaults.CALENDAR_SERVER | '/testcal'
    }
}

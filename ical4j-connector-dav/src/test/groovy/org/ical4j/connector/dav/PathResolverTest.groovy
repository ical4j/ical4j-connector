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
        PathResolver.Defaults.BAIKAL          | '/principals/testcal'
        PathResolver.Defaults.BEDEWORK        | '/principals/users/testcal/'
        PathResolver.Defaults.CALENDAR_SERVER | '/testcal/'
        PathResolver.Defaults.NEXTCLOUD       | '/principals/users/testcal/'
    }

    def 'verify calendar path resolution'() {
        when: 'a calendar path is resolved'
        def path = pathResolver.getCalendarPath('testcal', '')

        then: 'the result is as expected'
        path == expectedPath

        where:
        pathResolver                            | expectedPath
        PathResolver.Defaults.RADICALE        | '/testcal'
        PathResolver.Defaults.BAIKAL          | '/calendars/testcal'
        PathResolver.Defaults.BEDEWORK        | '/ucaldav/users/testcal'
        PathResolver.Defaults.CALENDAR_SERVER | '/dav/testcal'
        PathResolver.Defaults.NEXTCLOUD       | '/calendars/testcal/'
    }

    def 'verify calendar id resolution'() {
        when: 'a calendar id is resolved from a path'
        def id = pathResolver.getResourceName(repositoryPath, '')

        then: 'the result is as expected'
        id == 'testcal'

        where:
        pathResolver                            | repositoryPath
        PathResolver.Defaults.RADICALE        | '/admin/testcal'
        PathResolver.Defaults.BAIKAL          | '/calendars/testcal'
        PathResolver.Defaults.BEDEWORK        | '/users/testcal'
        PathResolver.Defaults.CALENDAR_SERVER | '/testcal'
        PathResolver.Defaults.NEXTCLOUD       | '/calendars/admin/testcal/'
        PathResolver.Defaults.NEXTCLOUD       | '/addressbooks/users/admin/testcal/'
    }

    def 'verify nextcloud collection paths include the workspace'() {
        expect: 'calendar and address book paths follow the Nextcloud layout'
        PathResolver.Defaults.NEXTCLOUD.getCalendarPath('personal', 'alice@example.com') ==
                '/calendars/alice@example.com/personal/'
        PathResolver.Defaults.NEXTCLOUD.getCardPath('contacts', 'alice@example.com') ==
                '/addressbooks/users/alice@example.com/contacts/'
        PathResolver.Defaults.NEXTCLOUD.getResourceName('/calendars/alice@example.com/personal/', '') == 'personal'
    }
}

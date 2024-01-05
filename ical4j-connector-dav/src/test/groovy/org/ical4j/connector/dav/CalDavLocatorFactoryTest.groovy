package org.ical4j.connector.dav

import org.apache.jackrabbit.webdav.DavResourceLocator
import spock.lang.Ignore
import spock.lang.Specification

@Ignore('Remove dav resource from scope until other methods are stable')
class CalDavLocatorFactoryTest extends Specification {

    def 'test locator creation from href'() {
        given: 'a locator factory'
        CalDavLocatorFactory locatorFactory = [prefix, pathResolver]

        when: 'a locator is created'
        DavResourceLocator locator = locatorFactory.createResourceLocator(prefix, href)

        then: 'it behaves as expected'
        locator.factory == locatorFactory
        locator.prefix == prefix
        locator.workspacePath == workspacePath
        locator.resourcePath == resourcePath
        locator.workspaceName == workspaceName
        locator.rootLocation == isRootLocation

        where:
        pathResolver                    | prefix                         | href                                         | workspacePath | workspaceName | resourcePath      | isRootLocation
        PathResolver.Defaults.RADICALE | 'https://dav.example.com'                         | 'https://dav.example.com'                                      | null     | null    | null             | true
        PathResolver.Defaults.RADICALE | 'https://www.example.com/dav'                     | 'https://www.example.com/dav'                                  | null     | null    | null             | true
        PathResolver.Defaults.RADICALE | 'https://www.example.com/dav'                     | 'https://www.example.com/dav/admin'                            | '/admin' | 'admin' | '/admin'         | false
        PathResolver.Defaults.RADICALE | 'https://www.example.com/dav'                     | 'https://www.example.com/dav/admin/testcal'                    | '/admin' | 'admin' | '/admin/testcal' | false

        PathResolver.Defaults.BAIKAL   | 'https://dav.example.com/dav.php'                 | 'https://dav.example.com/dav.php'                              | null     | null    | null             | true
        PathResolver.Defaults.BAIKAL   | 'https://www.example.com/dav/dav.php'             | 'https://www.example.com/dav/dav.php'                          | null     | null    | null             | true
        PathResolver.Defaults.BAIKAL   | 'https://www.example.com/dav/dav.php'             | 'https://www.example.com/dav/dav.php/admin'                    | '/admin' | 'admin' | '/admin'         | false
        PathResolver.Defaults.BAIKAL   | 'https://www.example.com/dav/dav.php'             | 'https://www.example.com/dav/dav.php/admin/testcal'            | '/admin' | 'admin' | '/admin/testcal' | false

        PathResolver.Defaults.GCAL     | 'https://apidata.googleusercontent.com/caldav/v2' | 'https://apidata.googleusercontent.com/caldav/v2'              | null     | null    | null             | true
        PathResolver.Defaults.GCAL     | 'https://apidata.googleusercontent.com/caldav/v2' | 'https://apidata.googleusercontent.com/caldav/v2/calid/user'   | '/calid' | 'calid' | '/calid/user'    | false
        PathResolver.Defaults.GCAL     | 'https://apidata.googleusercontent.com/caldav/v2' | 'https://apidata.googleusercontent.com/caldav/v2/calid/events' | '/calid' | 'calid' | '/calid/events'  | false

        PathResolver.Defaults.SOGO     | 'https://www.example.com/SOGo/dav'                | 'https://www.example.com/SOGo/dav'                             | null     | null    | null             | true
        PathResolver.Defaults.SOGO     | 'https://www.example.com/SOGo/dav'                | 'https://www.example.com/SOGo/dav/admin'                       | '/admin' | 'admin' | '/admin'         | false
        PathResolver.Defaults.SOGO     | 'https://www.example.com/SOGo/dav'                | 'https://www.example.com/SOGo/dav/admin/testcal'               | '/admin' | 'admin' | '/admin/testcal' | false

    }

    def 'test locator creation from workspace and resource path'() {
        given: 'a locator factory'
        CalDavLocatorFactory locatorFactory = [prefix, pathResolver]

        when: 'a locator is created'
        DavResourceLocator locator = locatorFactory.createResourceLocator(prefix, workspacePath, path, path == resourcePath)

        then: 'it behaves as expected'
        locator.factory == locatorFactory
        locator.prefix == prefix
        locator.workspacePath == workspacePath
        locator.resourcePath == resourcePath
        locator.workspaceName == workspaceName
        !locator.rootLocation
        locator.getHref(false) == href

        where:
        pathResolver                    | prefix                                            | href                                         | workspacePath | workspaceName | path      | resourcePath
        PathResolver.Defaults.RADICALE | 'https://www.example.com/dav'                     | 'https://www.example.com/dav/admin/testcal'                    | '/admin' | 'admin' | '/admin/testcal'     | '/testcal'
        PathResolver.Defaults.BAIKAL   | 'https://www.example.com/dav/dav.php'             | 'https://www.example.com/dav/dav.php/calendars/admin/testcal'            | '/admin' | 'admin' | '/calendars/admin/testcal'     | '/admin/testcal'
        PathResolver.Defaults.GCAL     | '/caldav/v2' | '/caldav/v2/calid/events' | '/calid' | 'calid' | '/calid/events'      | '/calid/events'
        PathResolver.Defaults.SOGO     | 'https://www.example.com/SOGo/dav'                | 'https://www.example.com/SOGo/dav/admin/testcal'               | '/admin' | 'admin' | '/admin/testcal'     | '/admin/testcal'

        PathResolver.Defaults.RADICALE | 'https://www.example.com/dav'                     | 'https://www.example.com/dav/admin/testcal'                    | '/admin' | 'admin' | '/testcal'           | '/admin/testcal'
        PathResolver.Defaults.BAIKAL   | 'https://www.example.com/dav/dav.php'             | 'https://www.example.com/dav/dav.php/admin/testcal'            | '/admin' | 'admin' | '/calendars/testcal' | '/admin/testcal'
        PathResolver.Defaults.GCAL     | '/caldav/v2' | '/caldav/v2/calid/events' | '/calid' | 'calid' | '/calid/events'      | null
        PathResolver.Defaults.SOGO     | 'https://www.example.com/SOGo/dav'                | 'https://www.example.com/SOGo/dav/admin/testcal'               | '/admin' | 'admin' | '/testcal'           | '/admin/testcal'

    }
}

/**
 * Copyright (c) 2012, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ical4j.connector.dav;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementations resolve host path elements.
 * 
 * @author fortuna
 * 
 *         Created on: 02/04/2009
 * 
 *         $Id$
 */
public interface PathResolver {

    /**
     * Return the principal collection path for a DAV server implementation.
     * @param calendarId a calendar id
     * @return a string representing the path to the principal collection for a calendar
     */
    String getPrincipalPath(String calendarId);

    /**
     * Return the resource path (e.g. calendar collection) for a DAV server implementation.
     * @param resourcePath a path component representing a resource
     * @return a string representing the repository path to the resource
     */
    String getCalendarPath(String resourcePath, String wspPath);

    String getCardPath(String resourcePath, String wspPath);

    String getResourceName(String repositoryPath, String wspPath);

    /**
     * Default path resolver configurations for some popular CalDAV/CardDAV implementations.
     *
     * Each configuration provides a pattern for the follow path types:
     * - principals, or who has access to the calendar/card resources
     * - calendar collections, containers for icalendar objects
     * - card collections, containers for vcard objects
     *
     * A configuration also provides a general path pattern that is used to extract the component paths
     * from a full repository path, such that:
     * - pathPattern(principalPath(resource))[resource] == resource
     * - pathPattern(calendarPath(resource))[resource] == resource
     * - pathPattern(cardPath(resource))[resource] == resource
     */
    enum Defaults implements PathResolver {

        CHANDLER( "/%s/", "/users/%s", "",
                "/users/(?<resource>\\w+)/?"),

        RADICALE( "/%s",
                "/%2$s/%1$s",
                "/%2$s/%1$s",
                "\\/(?<wsp>[\\w]+)\\/(?<resource>[\\w]+)"),

        BAIKAL( "/principals/%s",
                "/calendars/%2$s/%1$s",
                "/addressbooks/%2$s/%1$s",
                "\\/(?<type>(principals|calendars|addressbooks))\\/((?<wsp>[\\w]+)\\/)?(?<resource>[\\w]+)"),

        CGP("/", "/", "","/(?<resource>\\w+)/?"),

        KMS("/", "/", "","/(?<resource>\\w+)/?"),

        ZIMBRA("/principals/users/%s/", "/dav/%s/", "",
                "/(?<resource>\\w+)/?"),

        ICAL_SERVER( "/principals/users/%s/", "/dav/%s/", "",
                "/(?<resource>\\w+)/?"),

        CALENDAR_SERVER( "/%s/", "/dav/%s", "","/(?<resource>\\w+)/?"),

        GCAL( "/%s/user", "/%s/events",  "",
                "/(?<resource>\\w+)/events/?"),

        SOGO("/%s/", "/%s/", "","/(?<resource>\\w+)/?"),

        DAVICAL("/%s/", "/%s/", "","/(?<resource>\\w+)/?"),

        BEDEWORK("/principals/users/%s/", "/ucaldav/users/%s", "",
                "/users/(?<resource>\\w+)/?"),

        ORACLE_CS("/principals/%s/", "/home/%s/", "",
                "/home/(?<resource>\\w+)/?"),

        GENERIC("/%s", "/%s", "","/(?<resource>\\w+)");

        private final String principalPath;

        private final String calendarPath;

        private final String cardPath;

        private final Pattern pathPattern;

        Defaults(String principalPath, String calendarPath, String cardPath, String pathPattern) {
            this.principalPath = principalPath;
            this.calendarPath = calendarPath;
            this.cardPath = cardPath;
            this.pathPattern = Pattern.compile(pathPattern);
        }

        /**
         * Resolves the path component for a user's calendar store URL.
         *
         * @param resourceName a username
         * @return the user path for a server implementation
         */
        @Override
        public String getCalendarPath(String resourceName, String wspPath) {
            if (wspPath != null && !wspPath.isEmpty()) {
                return String.format("/%s", String.format(calendarPath, resourceName, wspPath))
                        .replaceAll("/+", "/");
            } else {
                return String.format("/%s", String.format(calendarPath, resourceName, ""))
                        .replaceAll("/+", "/");
            }
        }

        @Override
        public String getCardPath(String resourcePath, String wspPath) {
            if (wspPath != null && !wspPath.isEmpty()) {
                return String.format("/%s", String.format(cardPath, resourcePath, wspPath))
                        .replaceAll("/+", "/");
            } else {
                return String.format("/%s", String.format(cardPath, resourcePath, ""))
                        .replaceAll("/+", "/");
            }
        }

        /**
         * Reverse resolution of a calendar identifier from a repository path.
         * @param repositoryPath a repository path
         * @return a calendar id
         */
        @Override
        public String getResourceName(String repositoryPath, String wspPath) {
            Matcher matcher = pathPattern.matcher(repositoryPath);
//            if (matcher.matches() && matcher.groupCount() > 0) {
//                if (wspPath != null && !wspPath.isEmpty()) {
//                    return String.format("%s/%s", wspPath, matcher.group(1));
//                } else {
//                    return matcher.group(1);
//                }
//            }
            if (matcher.matches()) {
                return matcher.group("resource");
            }
            return null;
        }

        /**
         * Resolves the path component for a principal URL.
         *
         * @param calendarId a username
         * @return the principal path for a server implementation
         */
        @Override
        public String getPrincipalPath(String calendarId) {
            return String.format(principalPath, calendarId);
        }
    }
}

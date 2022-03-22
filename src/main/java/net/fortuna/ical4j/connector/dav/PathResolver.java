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
package net.fortuna.ical4j.connector.dav;

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
     * Return the root path for a DAV server implementation.
     * @return a string representing the root path of a repository URL.
     */
    String getRootPath();

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
    String getRepositoryRoot(String resourcePath, String wspPath);

    String getResourcePath(String repositoryPath, String wspPath);

    enum Defaults implements PathResolver {

        CHANDLER("/dav", "/%s/", "/users/%s",
                "/users/(\\w+)/?"),

        RADICALE("", "/", "/%s", "/([\\w]+)/?"),

        BAIKAL("/dav.php", "/", "/calendars/%s",
                "/calendars/([\\w]+)/?"),

        CGP("/CalDAV","/", "/", "/(\\w+)/?"),

        KMS("/caldav","/", "/", "/(\\w+)/?"),

        ZIMBRA("","/principals/users/%s/", "/dav/%s/",
                "/(\\w+)/?"),

        ICAL_SERVER("", "/principals/users/%s/", "/dav/%s/",
                "/(\\w+)/?"),

        CALENDAR_SERVER("/dav", "/%s/", "/%s", "/(\\w+)/?"),

        GCAL("/caldav/v2", "/%s/user", "/%s/events",
                "/\\w+/events/?"),

        SOGO("/dav","/%s/", "/%s/", "/(\\w+)/?"),

        DAVICAL("/caldav.php","/%s/", "/%s/", "/(\\w+)/?"),

        BEDEWORK("/ucaldav","/principals/users/%s/", "/users/%s",
                "/users/(\\w+)/?"),

        ORACLE_CS("/dav","/principals/%s/", "/home/%s/",
                "/home/(\\w+)/?"),

        GENERIC("","/%s", "/%s", "/(\\w+)");

        private final String rootPath;

        private final String principalPathBase;

        private final String calendarPathBase;

        private final Pattern calendarPathPattern;

        Defaults(String rootPath, String principalPathBase, String calendarPathBase,
                 String calendarPathPattern) {
            this.rootPath = rootPath;
            this.principalPathBase = principalPathBase;
            this.calendarPathBase = calendarPathBase;
            this.calendarPathPattern = Pattern.compile("^" + rootPath + calendarPathPattern + "$");
        }

        @Override
        public String getRootPath() {
            return rootPath;
        }

        /**
         * Resolves the path component for a user's calendar store URL.
         *
         * @param resourcePath a username
         * @return the user path for a server implementation
         */
        @Override
        public String getRepositoryRoot(String resourcePath, String wspPath) {
            if (wspPath != null && !wspPath.isEmpty()) {
                return rootPath + String.format("%s/%s", wspPath, String.format(calendarPathBase, resourcePath));
            } else {
                return rootPath + String.format(calendarPathBase, resourcePath);
            }
        }

        /**
         * Reverse resolution of a calendar identifier from a repository path.
         * @param repositoryPath a repository path
         * @return a calendar id
         */
        @Override
        public String getResourcePath(String repositoryPath, String wspPath) {
            Matcher matcher = calendarPathPattern.matcher(repositoryPath);
            if (matcher.matches() && matcher.groupCount() > 0) {
                if (wspPath != null && !wspPath.isEmpty()) {
                    return String.format("%s/%s", wspPath, matcher.group(1));
                } else {
                    return matcher.group(1);
                }
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
            return rootPath + String.format(principalPathBase, calendarId);
        }
    }
}

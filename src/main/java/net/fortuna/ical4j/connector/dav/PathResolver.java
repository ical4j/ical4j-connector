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

/**
 * Implementations resolve host path elements.
 * 
 * @author fortuna
 * 
 *         Created on: 02/04/2009
 * 
 *         $Id$
 */
public enum PathResolver {

    /**
	 *
	 */
    CHANDLER( "/dav/%s/", "/dav/users/%s"),

    RADICALE("/%s", "/%s/"),

    BAIKAL("/calendars/%s", "/calendars/%s/"),

    /**
	 * 
	 */
    CGP("/CalDAV/", "/CalDAV/"),

    /**
	 * 
	 */
    KMS("/caldav/", null),

    /**
	 * 
	 */
    ZIMBRA("/principals/users/%s/", "/dav/%s/"),

    /**
	 * 
	 */
    ICAL_SERVER("/principals/users/%s/", "/dav/%s/"),

    /**
	 * 
	 */
    CALENDAR_SERVER("/dav/%s/", "/dav/%s/"),
    
    GCAL("/calendar/dav/%s/user/", "/calendar/dav/%s/events/"),

    SOGO("/SOGo/dav/%s/", "/SOGo/dav/%s/"),
    
    DAVICAL("/caldav.php/%s/", "/caldav.php/%s/"),
    
    BEDEWORK("/ucaldav/principals/users/%s/", "/ucaldav/users/%s/"),

    ORACLE_CS("/dav/principals/%s/", "/dav/home/%s/"),
    
    GENERIC("%s", "%s");


    private final String userPathBase;

    private final String principalPathBase;

    PathResolver(String principalPathBase, String userPathBase) {
        this.principalPathBase = principalPathBase;
        this.userPathBase = userPathBase;
    }

    /**
     * Resolves the path component for a user's calendar store URL.
     * @param username a username
     * @return the user path for a server implementation
     */
    public String getUserPath(String username) {
        return String.format(userPathBase, username);
    }

    /**
     * Resolves the path component for a principal URL.
     * @param username a username
     * @return the principal path for a server implementation
     */
    public String getPrincipalPath(String username) {
        return String.format(principalPathBase, username);
    }
}

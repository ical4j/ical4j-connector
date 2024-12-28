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
package org.ical4j.connector.dav.property;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.Namespace;

/**
 * Properties that belongs to the CalendarServer namespace.
 * 
 * @author probert
 * 
 */
public interface CSDavPropertyName {

    /**
     * To improve on performance, this specification defines a new "calendar collection entity tag" (CTag) WebDAV
     * property that is defined on calendar collections. When the calendar collection changes, the CTag value changes.
     * Source : <a href="https://trac.calendarserver.org/browser/CalendarServer/trunk/doc/Extensions/caldav-ctag.txt">https://trac.calendarserver.org/browser/CalendarServer/trunk/doc/Extensions/caldav-ctag.txt</a>
     */
    String PROPERTY_CTAG = "getctag";

    /**
     * Specific to CalendarServer, but I can't find the description
     */
    String PROPERTY_DROP_HOME_URL = "dropbox-home-URL";

    /**
     * Provides the URI of the pubsub node to subscribe to in order to receive a notification whenever a
     * resource within this calendar home has changed.
     * <a href="http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-pubsubdiscovery.txt">http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-pubsubdiscovery.txt</a>
     */
    String PROPERTY_XMPP_URI = "xmpp-uri";

    /**
     * Identify the URL of the notification collection owned by the associated principal resource.
     * <a href="http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-sharing-02.txt">http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-sharing-02.txt</a>
     */
    String PROPERTY_NOTIFICATION_URL = "notification-URL";

    /**
     * Provides the hostname of the XMPP server a client should connect to for subscribing to notifications.
     * <a href="http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-pubsubdiscovery.txt">http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-pubsubdiscovery.txt</a>
     */
    String PROPERTY_XMPP_SERVER = "xmpp-server";

    /**
     * Auto-accept is currently supported for location and resource records. Specific to Calendar/iCal Server
     */
    String PROPERTY_AUTO_SCHEDULE = "auto-schedule";

    /**
     */
    String PROPERTY_SOURCE = "source";

    /**
     */
    String PROPERTY_SUBSCRIBED_STRIP_ALARMS = "subscribed-strip-alarms";

    /**
     */
    String PROPERTY_SUBSCRIBED_STRIP_ATTACHMENTS = "subscribed-strip-attachments";

    /**
     */
    String PROPERTY_SUBSCRIBED_STRIP_TODOS = "subscribed-strip-todos";

    /**
     */
    String PROPERTY_REFRESHRATE = "refreshrate";

    /**
     */
    String PROPERTY_PUSH_TRANSPORTS = "push-transports";

    /**
     */
    String PROPERTY_PUSHKEY = "pushkey";

    /**
     * Namespace used by CalendarServer (calendarserver.org).
     */
    Namespace NAMESPACE = Namespace.getNamespace("S", "http://calendarserver.org/ns/");

    /**
     * To improve on performance, this specification defines a new "calendar collection entity tag" (CTag) WebDAV
     * property that is defined on calendar collections. When the calendar collection changes, the CTag value changes.
     * Source : <a href="https://trac.calendarserver.org/browser/CalendarServer/trunk/doc/Extensions/caldav-ctag.txt">https://trac.calendarserver.org/browser/CalendarServer/trunk/doc/Extensions/caldav-ctag.txt</a>
     */
    DavPropertyName CTAG = DavPropertyName.create(PROPERTY_CTAG, NAMESPACE);

    /**
     * TODO: description missing. Stuff coming from Apple's Calendar Server
     */
    String PROPERTY_PROXY_WRITE_FOR = "calendar-proxy-write-for";
    /**
     * TODO: description missing. Stuff coming from Apple's Calendar Server
     */
    DavPropertyName PROXY_WRITE_FOR = DavPropertyName.create(PROPERTY_PROXY_WRITE_FOR, NAMESPACE);

    /**
     * TODO: description missing. Stuff coming from Apple's Calendar Server
     */
    String PROPERTY_PROXY_READ_FOR = "calendar-proxy-read-for";
    /**
     * TODO: description missing. Stuff coming from Apple's Calendar Server
     */
    DavPropertyName PROXY_READ_FOR = DavPropertyName.create(PROPERTY_PROXY_READ_FOR, NAMESPACE);

    /**
     * Specific to CalendarServer, but I can't find the description
     */
    DavPropertyName DROP_HOME_URL = DavPropertyName.create(PROPERTY_DROP_HOME_URL, NAMESPACE);

    /**
     * Provides the URI of the pubsub node to subscribe to in order to receive a notification whenever a resource within
     * this calendar home has changed.
     * <a href="http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions">http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions</a>
     * /caldav-pubsubdiscovery.txt
     */
    DavPropertyName XMPP_URI = DavPropertyName.create(PROPERTY_XMPP_URI, NAMESPACE);

    /**
     * Identify the URL of the notification collection owned by the associated principal resource.
     * <a href="http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-sharing-02.txt">http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-sharing-02.txt</a>
     */
    DavPropertyName NOTIFICATION_URL = DavPropertyName.create(PROPERTY_NOTIFICATION_URL, NAMESPACE);

    /**
     * Provides the hostname of the XMPP server a client should connect to for subscribing to notifications.
     * <a href="http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-pubsubdiscovery.txt">http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-pubsubdiscovery.txt</a>
     */
    DavPropertyName XMPP_SERVER = DavPropertyName.create(PROPERTY_XMPP_SERVER, NAMESPACE);

    /**
     * Auto-accept is currently supported for location and resource records. Specific to Calendar/iCal Server
     */
    DavPropertyName AUTO_SCHEDULE = DavPropertyName.create(PROPERTY_AUTO_SCHEDULE, NAMESPACE);

    /**
   */
    DavPropertyName SOURCE = DavPropertyName.create(DavConstants.PROPERTY_SOURCE, NAMESPACE);

    /**
   */
    DavPropertyName SUBSCRIBED_STRIP_ALARMS = DavPropertyName.create(
            PROPERTY_SUBSCRIBED_STRIP_ALARMS, NAMESPACE);

    /**
   */
    DavPropertyName SUBSCRIBED_STRIP_ATTACHMENTS = DavPropertyName.create(
            PROPERTY_SUBSCRIBED_STRIP_ATTACHMENTS, NAMESPACE);

    /**
   */
    DavPropertyName SUBSCRIBED_STRIP_TODOS = DavPropertyName.create(PROPERTY_SUBSCRIBED_STRIP_TODOS,
            NAMESPACE);

    /**
   */
    DavPropertyName REFRESHRATE = DavPropertyName.create(PROPERTY_REFRESHRATE, NAMESPACE);

    /**
   */
    DavPropertyName PUSH_TRANSPORTS = DavPropertyName.create(PROPERTY_PUSH_TRANSPORTS, NAMESPACE);

    /**
   */
    DavPropertyName PUSHKEY = DavPropertyName.create(PROPERTY_PUSHKEY, NAMESPACE);

}

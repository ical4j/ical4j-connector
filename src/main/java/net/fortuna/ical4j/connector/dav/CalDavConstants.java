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

import org.apache.jackrabbit.webdav.xml.Namespace;

/**
 * $Id$
 * 
 * Created on 19/11/2008
 * 
 * @author Ben
 * 
 */
public interface CalDavConstants extends DavConstants {

    /**
     * Default namespace.
     */
    public static final Namespace CALDAV_NAMESPACE = Namespace.getNamespace("C", "urn:ietf:params:xml:ns:caldav");

    /**
     * Namespace used by CalendarServer (calendarserver.org).
     */
    public static final Namespace CS_NAMESPACE = Namespace.getNamespace("S", "http://calendarserver.org/ns/");

    /**
     * Namespace used by the iCal client from Apple.
     */
    public static final Namespace ICAL_NAMESPACE = Namespace.getNamespace("I", "http://apple.com/ns/ical/");

    /**
     * CardDAV namespace
     */
    public static final Namespace CARDDAV_NAMESPACE = Namespace.getNamespace("C", "urn:ietf:params:xml:ns:carddav");
    
    /**
     * To improve on performance, this specification defines a new "calendar collection entity tag" (CTag) WebDAV
     * property that is defined on calendar collections. When the calendar collection changes, the CTag value changes.
     * Source : https://trac.calendarserver.org/browser/CalendarServer/trunk/doc/Extensions/caldav-ctag.txt
     */
    public static final String PROPERTY_CTAG = "getctag";

    /**
     * Purpose: Provides a human-readable description of the calendar collection. RFC : rfc4791
     */
    public static final String PROPERTY_CALENDAR_DESCRIPTION = "calendar-description";

    /**
     * Apple's iCal client use this property to store the color of the calendar, set by the user in iCal.
     */
    public static final String PROPERTY_CALENDAR_COLOR = "calendar-color";

    /**
     * 
     */
    public static final String PROPERTY_CALENDAR_ORDER = "calendar-order";

    /**
     * Purpose: Identify the calendars that contribute to the free-busy information for the owner of the scheduling
     * https://tools.ietf.org/html/draft-desruisseaux-caldav-sched-04
     * 
     * THIS PROPERTY WAS REMOVED IN DRAFT 05 AND THE OFFICIAL RFC (6638)
     */
    public static final String PROPERTY_FREE_BUSY_SET = "calendar-free-busy-set";

    /**
     * Purpose: Identifies the URL of any WebDAV collections that contain calendar collections owned by the associated
     * principal resource. RFC : rfc4791
     */
    public static final String PROPERTY_CALENDAR_HOME_SET = "calendar-home-set";

    /**
     * The property to identify a "calendar" resource-type for the collection.
     */
    public static final String PROPERTY_RESOURCETYPE_CALENDAR = "calendar";

    /**
     * TODO: description missing. Stuff coming from Apple's Calendar Server
     */
    public static final String PROPERTY_PROXY_WRITE_FOR = "calendar-proxy-write-for";

    /**
     * TODO: description missing. Stuff coming from Apple's Calendar Server
     */
    public static final String PROPERTY_PROXY_READ_FOR = "calendar-proxy-read-for";

    /**
     * Identify the calendar addresses of the associated principal resource.
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String PROPERTY_USER_ADDRESS_SET = "calendar-user-address-set";

    /**
     * Identifies the calendar user type of the associated principal resource. Its value is the same as the iCalendar "CUTYPE".
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String PROPERTY_USER_TYPE= "calendar-user-type";
    
    /**
     * Identify the URL of the scheduling Inbox collection owned by the associated principal resource.
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String PROPERTY_SCHEDULE_INBOX_URL = "schedule-inbox-URL";

    /**
     * Identify the URL of the scheduling Outbox collection owned by the associated principal resource.
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String PROPERTY_SCHEDULE_OUTBOX_URL = "schedule-outbox-URL";

    /**
     * Specific to CalendarServer, but I can't find the description
     */
    public static final String PROPERTY_DROP_HOME_URL = "dropbox-home-URL";

    /**
     * Provides the URI of the pubsub node to subscribe to in order to receive a notification whenever a 
     * resource within this calendar home has changed.
     * http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-pubsubdiscovery.txt
     */
    public static final String PROPERTY_XMPP_URI = "xmpp-uri";

    /**
     * Identify the URL of the notification collection owned by the associated principal resource.
     * http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-sharing-02.txt
     */
    public static final String PROPERTY_NOTIFICATION_URL = "notification-URL";

    /**
     * Provides the hostname of the XMPP server a client should connect to for subscribing to notifications.
     * http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-pubsubdiscovery.txt
     */
    public static final String PROPERTY_XMPP_SERVER = "xmpp-server";

    /**
     * Specifies the calendar component types (e.g., VEVENT, VTODO, etc.) that calendar object resources can contain in
     * the calendar collection.
     */
    public static final String PROPERTY_SUPPORTED_CALENDAR_COMPONENT_SET = "supported-calendar-component-set";

    /**
     * Specifies a supported component type (e.g., VEVENT, VTODO, etc.)
     */
    public static final String PROPERTY_COMPONENT = "comp";

    /**
     * Determines whether the calendar object resources in a calendar collection will affect the owner's freebusy.
     */
    public static final String PROPERTY_SCHEDULE_CALENDAR_TRANSP = "schedule-calendar-transp";

    /**
     * Specifies a default calendar for an attendee that will automatically have new scheduling messages deposited into
     * it when they arrive.
     */
    public static final String PROPERTY_SCHEDULE_DEFAULT_CALENDAR_URL = "schedule-default-calendar-URL";

    /**
     * The CALDAV:calendar-timezone property is used to specify the time zone the server should rely on to resolve
     * "date" values and "date with local time" values (i.e., floating time) to "date with UTC time" values.
     */
    public static final String PROPERTY_CALENDAR_TIMEZONE = "calendar-timezone";

    /**
     * Auto-accept is currently supported for location and resource records. Specific to Calendar/iCal Server
     */
    public static final String PROPERTY_AUTO_SCHEDULE = "auto-schedule";

    /**
     */
    public static final String PROPERTY_SOURCE = "source";

    /**
     */
    public static final String PROPERTY_SUBSCRIBED_STRIP_ALARMS = "subscribed-strip-alarms";

    /**
     */
    public static final String PROPERTY_SUBSCRIBED_STRIP_ATTACHMENTS = "subscribed-strip-attachments";

    /**
     */
    public static final String PROPERTY_SUBSCRIBED_STRIP_TODOS = "subscribed-strip-todos";

    /**
     */
    public static final String PROPERTY_REFRESHRATE = "refreshrate";

    /**
     */
    public static final String PROPERTY_PUSH_TRANSPORTS = "push-transports";

    /**
     */
    public static final String PROPERTY_PUSHKEY = "pushkey";

    /**
     * 
     */
    public static final String PROPERTY_SUPPORTED_CALENDAR_DATA = "supported-calendar-data";

    /**
     * 
     */
    public static final String PROPERTY_MAX_RESOURCE_SIZE = "max-resource-size";

    /**
     * 
     */
    public static final String PROPERTY_MIN_DATE_TIME = "min-date-time";

    /**
     * 
     */
    public static final String PROPERTY_MAX_DATE_TIME = "max-date-time";

    /**
     * 
     */
    public static final String PROPERTY_MAX_INSTANCES = "max-instances";

    /**
     * 
     */
    public static final String PROPERTY_MAX_ATTENDEES_PER_INSTANCE = "max-attendees-per-instance";

    /**
     * 
     */
    public static final String PROPERTY_CALENDAR_DATA = "calendar-data";

    /**
     * 
     */
    public static final String PROPERTY_RECIPIENT = "recipient";

    /**
     * 
     */
    public static final String PROPERTY_REQUEST_STATUS = "request-status";
    
    /**
     * 
     */
    public static final String PROPERTY_CALENDAR_QUERY = "calendar-query";
    
    /**
     * 
     */
    public static final String PROPERTY_FILTER = "filter";
    
    /**
     * 
     */
    public static final String PROPERTY_COMP_FILTER = "comp-filter";
    
    /**
     * 
     */
    public static final String PROPERTY_TIME_RANGE = "time-range";
    
    /**
     * 
     */
    public static final String ATTRIBUTE_NAME = "name";
    
    /**
     * 
     */
    public static final String ATTRIBUTE_START = "start";
    
    /**
     * 
     */
    public static final String ATTRIBUTE_END = "end";
    

    /**
     * Identifies the URL of any WebDAV collections that contain address book collections 
     * owned by the associated principal resource. rfc6352
     */
    public static final String PROPERTY_ADDRESSBOOK_HOME_SET = "addressbook-home-set";
    
    /**
     * Specifies what media types are allowed for address object resources in an address 
     * book collection. rfc6352
     */
    public static final String PROPERTY_SUPPORTED_ADDRESS_DATA = "supported-address-data";
    
    /**
     * for carddav
     */
    public static final String PROPERTY_MAX_IMAGE_SIZE = "max-image-size";
    
    /**
     * Specifies one of the following:
     *
     * 1.  The parts of an address object resource that should be
     *     returned by a given address book REPORT request, and the media
     *     type and version for the returned data; or
     *
     * 2.  The content of an address object resource in a response to an
     *     address book REPORT request.
     *     
     * RFC 6352
     */
    public static final String PROPERTY_ADDRESS_DATA = "address-data";
    
    /**
     * Servers MAY reject requests to create a
     * scheduling object resource with an iCalendar "UID" property value
     * already in use by another scheduling object resource owned by the
     * same user in other calendar collections.  Servers SHOULD report 
     * the URL of the scheduling object resource that is already making
     * use of the same "UID" property value in the DAV:href element.
     * 
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String UNIQUE_SCHEDULING_OBJECT_RESOURCE = "unique-scheduling-object-resource";

    /**
     * All the calendar components in a
     * scheduling object resource MUST contain the same "ORGANIZER"
     * property value when present
     * 
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String SAME_ORGANIZER_IN_ALL_COMPONENTS = "same-organizer-in-all-components";
    
    /**
     * Servers MAY impose restrictions on modifications allowed by an "Organizer".
     * 
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String ALLOWED_ORGANIZER_SCHEDULING_OBJECT_CHANGE = "allowed-organizer-scheduling-object-change";
    
    /**
     * Servers MAY impose restrictions on modifications allowed by an "Attendee", 
     * subject to the allowed changes specified in Section 3.2.2.1
     * 
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String ALLOWED_ATTENDEE_SCHEDULING_OBJECT_CHANGE = "allowed-attendee-scheduling-object-change";

    /**
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String DEFAULT_CALENDAR_NEEDED = "default-calendar-needed";
    /**
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String VALID_SCHEDULE_DEFAULT_CALENDAR_URL = "valid-schedule-default-calendar-URL";
    /**
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String VALID_SCHEDULING_MESSAGE = "valid-scheduling-message";
    /**
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String VALID_ORGANIZER = "valid-organizer";
    /**
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String SCHEDULE_DELIVER = "schedule-deliver";
    /**
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String SCHEDULE_DELIVER_INVITE = "schedule-deliver-invite";
    /**
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String SCHEDULE_DELIVER_REPLY = "schedule-deliver-reply";
    /**
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String SCHEDULE_QUERY_FREEBUSY = "schedule-query-freebusy";
    /**
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String SCHEDULE_SEND = "schedule-send";
    /**
     * https://tools.ietf.org/html/rfc6638
     */
    public static final String SCHEDULE_SEND_INVITE = "schedule-send-invite";
    
    /**
     * The CALDAV:schedule-send-reply privilege controls the sending of
     * scheduling messages by "Attendees".
     * 
     * https://tools.ietf.org/html/rfc6638#section-6.2.3
     */
    public static final String SCHEDULE_SEND_REPLY = "schedule-send-reply";
    
    /**
     * The CALDAV:schedule-send-freebusy privilege controls the use of the
     * POST method to submit scheduling messages that specify the scheduling
     * method "REQUEST" with a "VFREEBUSY" calendar component.
     * 
     * https://tools.ietf.org/html/rfc6638#section-6.2.4
     */
    public static final String SCHEDULE_SEND_FREEBUSY = "schedule-send-freebusy";

    /**
     * Determines whether the calendar object resources in a
     *  calendar collection will affect the owner's busy time information. 
     * 
     * https://tools.ietf.org/html/rfc6638#section-9.1
     */
    public static final String SCHEDULE_CALENDAR_TRANSP = "schedule-calendar-transp";
    
    /**
     * Specifies a default calendar for an "Attendee" where new
     *  scheduling object resources are created.
     * 
     * https://tools.ietf.org/html/rfc6638#section-9.2
     */
    public static final String SCHEDULE_DEFAULT_CALENDAR_URL = "schedule-default-calendar-URL";
    
    /**
     * Indicates whether a scheduling object resource has had a
     * "consequential" change made to it.
     * 
     * https://tools.ietf.org/html/rfc6638#section-9.3
     */
    public static final String SCHEDULE_TAG = "schedule-tag";
    
    /**
     * Contains the set of responses for a POST method request (for scheduling)
     * 
     * https://tools.ietf.org/html/rfc6638#section-10.1
     */
    public static final String SCHEDULE_RESPONSE = "schedule-response";
    
    /**
     * Contains a single response for a POST method request (for scheduling)
     * 
     * https://tools.ietf.org/html/rfc6638#section-10.2
     */
    public static final String RESPONSE = "response";
    
    /**
     * The calendar user address that the enclosing response for a POST method request is for.
     * 
     * https://tools.ietf.org/html/rfc6638#section-10.3
     */
    public static final String RECIPIENT = "recipient";
    
    /**
     * The iTIP "REQUEST-STATUS" property value for a scheduling response.
     * 
     * https://tools.ietf.org/html/rfc6638#section-10.4
     */
    public static final String REQUEST_STATUS = "request-status";
    
    /**
     * Defines a "VAVAILABILITY" component that will be used in calculating 
     * free-busy time when an iTIP free-busy request is targeted at the 
     * calendar user who owns the Inbox.
     * 
     * http://tools.ietf.org/html/draft-daboo-calendar-availability-03
     */
    public static final String CALENDAR_AVAIBILITY = "calendar-availability";
    
    /**
     * Enumerates the sets of component restrictions the server is
     * willing to allow the client to specify in MKCALENDAR or extended
     * MKCOL requests.
     * 
     * http://tools.ietf.org/html/draft-daboo-caldav-extensions-01
     */
    public static final String SUPPORTED_CALENDAR_COMPONENT_SETS = "supported-calendar-component-sets";
    
    /**
     * A default alarm applied to "VEVENT" components whose "DTSTART" property value 
     * type is "DATE-TIME"
     * 
     * http://tools.ietf.org/html/draft-daboo-valarm-extensions-04
     */
    public static final String DEFAULT_ALARM_VEVENT_DATETIME = "default-alarm-vevent-datetime";
    
    /**
     * A default alarm applied to "VEVENT" components whose "DTSTART" property value type is "DATE"
     * 
     * http://tools.ietf.org/html/draft-daboo-valarm-extensions-04
     */
    public static final String DEFAULT_ALARM_VEVENT_DATE = "default-alarm-vevent-date";
    
    /**
     * A default alarm applied to "VTODO" components whose "DUE" or "DTSTART" 
     * property value type is "DATE-TIME"
     * 
     * http://tools.ietf.org/html/draft-daboo-valarm-extensions-04
     */
    public static final String DEFAULT_ALARM_VTODO_DATETIME = "default-alarm-vtodo-datetime";
    
    /**
     * A default alarm applied to "VTODO" components whose "DUE" or "DTSTART" 
     * property value type is "DATE", or when neither of those properties is present
     * 
     * http://tools.ietf.org/html/draft-daboo-valarm-extensions-04
     */
    public static final String DEFAULT_ALARM_VTODO_DATE = "default-alarm-vtodo-date";
}

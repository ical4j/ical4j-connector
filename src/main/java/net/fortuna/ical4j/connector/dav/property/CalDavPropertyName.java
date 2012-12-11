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
package net.fortuna.ical4j.connector.dav.property;

import net.fortuna.ical4j.connector.dav.CalDavConstants;

import org.apache.jackrabbit.webdav.property.DavPropertyName;

/**
 * $Id$
 * 
 * Created on 19/11/2008
 * 
 * @author Ben
 * 
 */
public class CalDavPropertyName {

    /**
     * 
     */
    public static final DavPropertyName CALENDAR_DESCRIPTION = DavPropertyName.create(
            CalDavConstants.PROPERTY_CALENDAR_DESCRIPTION, CalDavConstants.CALDAV_NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName CALENDAR_TIMEZONE = DavPropertyName.create(
            CalDavConstants.PROPERTY_CALENDAR_TIMEZONE, CalDavConstants.CALDAV_NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName SUPPORTED_CALENDAR_COMPONENT_SET = DavPropertyName.create(
            CalDavConstants.PROPERTY_SUPPORTED_CALENDAR_COMPONENT_SET, CalDavConstants.CALDAV_NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName SUPPORTED_CALENDAR_DATA = DavPropertyName.create(
            CalDavConstants.PROPERTY_SUPPORTED_CALENDAR_DATA, CalDavConstants.CALDAV_NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName MAX_RESOURCE_SIZE = DavPropertyName.create(
            CalDavConstants.PROPERTY_MAX_RESOURCE_SIZE, CalDavConstants.CALDAV_NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName MIN_DATE_TIME = DavPropertyName.create(CalDavConstants.PROPERTY_MIN_DATE_TIME,
            CalDavConstants.CALDAV_NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName MAX_DATE_TIME = DavPropertyName.create(CalDavConstants.PROPERTY_MAX_DATE_TIME,
            CalDavConstants.CALDAV_NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName MAX_INSTANCES = DavPropertyName.create(CalDavConstants.PROPERTY_MAX_INSTANCES,
            CalDavConstants.CALDAV_NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName MAX_ATTENDEES_PER_INSTANCE = DavPropertyName.create(
            CalDavConstants.PROPERTY_MAX_ATTENDEES_PER_INSTANCE, CalDavConstants.CALDAV_NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName CALENDAR_DATA = DavPropertyName.create(CalDavConstants.PROPERTY_CALENDAR_DATA,
            CalDavConstants.CALDAV_NAMESPACE);

    /**
     * Property from a draft (draft-desruisseaux-ischedule-01)
     */
    public static final DavPropertyName RECIPIENT = DavPropertyName.create(CalDavConstants.PROPERTY_RECIPIENT,
            CalDavConstants.CALDAV_NAMESPACE);

    /**
     * Property from a draft (draft-desruisseaux-ischedule-01)
     */
    public static final DavPropertyName REQUEST_STATUS = DavPropertyName.create(
            CalDavConstants.PROPERTY_REQUEST_STATUS, CalDavConstants.CALDAV_NAMESPACE);

    public static final DavPropertyName COMPONENT = DavPropertyName.create(CalDavConstants.PROPERTY_COMPONENT,
            CalDavConstants.CALDAV_NAMESPACE);

    /**
     * Purpose: Identify the calendars that contribute to the free-busy information for the owner of the scheduling
     * https://tools.ietf.org/html/draft-desruisseaux-caldav-sched-04
     * 
     *  THIS PROPERTY WAS REMOVED IN DRAFT 05 AND THE OFFICIAL RFC (6638)
     *  
     */
    public static final DavPropertyName FREE_BUSY_SET = DavPropertyName.create(CalDavConstants.PROPERTY_FREE_BUSY_SET,
            CalDavConstants.CALDAV_NAMESPACE);

    /**
     * Purpose: Identifies the URL of any WebDAV collections that contain calendar collections owned by the associated
     * principal resource. RFC : rfc4791
     */
    public static final DavPropertyName CALENDAR_HOME_SET = DavPropertyName.create(CalDavConstants.PROPERTY_CALENDAR_HOME_SET,
            CalDavConstants.CALDAV_NAMESPACE);

    /**
     * Identify the calendar addresses of the associated principal resource.
     * http://tools.ietf.org/html/rfc6638
     */
    public static final DavPropertyName USER_ADDRESS_SET = DavPropertyName.create(
            CalDavConstants.PROPERTY_USER_ADDRESS_SET, CalDavConstants.CALDAV_NAMESPACE);

    /**
     * Identifies the calendar user type of the associated principal resource. Its value is the same as the iCalendar "CUTYPE".
     * https://tools.ietf.org/html/rfc6638
     */
    public static final DavPropertyName USER_TYPE = DavPropertyName.create(
            CalDavConstants.PROPERTY_USER_TYPE, CalDavConstants.CALDAV_NAMESPACE);

    /**
     * Identify the URL of the scheduling Inbox collection owned by the associated principal resource.
     * http://tools.ietf.org/html/rfc6638
     */
    public static final DavPropertyName SCHEDULE_INBOX_URL = DavPropertyName.create(
            CalDavConstants.PROPERTY_SCHEDULE_INBOX_URL, CalDavConstants.CALDAV_NAMESPACE);

    /**
     * Identify the URL of the scheduling Outbox collection owned by the associated principal resource.
     * http://tools.ietf.org/html/rfc6638
     */
    public static final DavPropertyName SCHEDULE_OUTBOX_URL = DavPropertyName.create(
            CalDavConstants.PROPERTY_SCHEDULE_OUTBOX_URL, CalDavConstants.CALDAV_NAMESPACE);

    /**
     * Determines whether the calendar object resources in a calendar collection will affect the owner's freebusy.
     */
    public static final DavPropertyName SCHEDULE_CALENDAR_TRANSP = DavPropertyName.create(
            CalDavConstants.PROPERTY_SCHEDULE_CALENDAR_TRANSP, CalDavConstants.CALDAV_NAMESPACE);

    /**
     * Specifies a default calendar for an attendee that will automatically have new scheduling messages deposited into
     * it when they arrive.
     */
    public static final DavPropertyName SCHEDULE_DEFAULT_CALENDAR_URL = DavPropertyName.create(
            CalDavConstants.PROPERTY_SCHEDULE_DEFAULT_CALENDAR_URL, CalDavConstants.CALDAV_NAMESPACE);

}

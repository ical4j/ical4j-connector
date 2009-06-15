/**
 * Copyright (c) 2009, Ben Fortuna
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
    public static final DavPropertyName CALENDAR_DESCRIPTION = DavPropertyName.create("calendar-description",
            CalDavConstants.NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName CALENDAR_TIMEZONE = DavPropertyName.create("calendar-timezone",
            CalDavConstants.NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName SUPPORTED_CALENDAR_COMPONENT_SET = DavPropertyName.create(
            "supported-calendar-component-set", CalDavConstants.NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName SUPPORTED_CALENDAR_DATA = DavPropertyName.create("supported-calendar-data",
            CalDavConstants.NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName MAX_RESOURCE_SIZE = DavPropertyName.create("max-resource-size",
            CalDavConstants.NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName MIN_DATE_TIME = DavPropertyName.create("min-date-time",
            CalDavConstants.NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName MAX_DATE_TIME = DavPropertyName.create("max-date-time",
            CalDavConstants.NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName MAX_INSTANCES = DavPropertyName.create("max-instances",
            CalDavConstants.NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName MAX_ATTENDEES_PER_INSTANCE = DavPropertyName.create(
            "max-attendees-per-instance", CalDavConstants.NAMESPACE);

    /**
     * 
     */
    public static final DavPropertyName CALENDAR_DATA = DavPropertyName.create("calendar-data",
            CalDavConstants.NAMESPACE);
}

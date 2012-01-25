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
package net.fortuna.ical4j.connector.dav.enums;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public enum SupportedFeature {

  ACCESS_CONTROL("access-control"),
  CALENDAR_ACCESS("calendar-access"),
  CALENDAR_SCHEDULE("calendar-schedule"),
  CALENDAR_AUTO_SCHEDULE("calendar-auto-schedule"),
  CALENDAR_AVAILABILITY("calendar-availability"),
  INBOX_AVAILABILITY("inbox-availability"),
  CALENDAR_PROXY("calendar-proxy"),
  CALENDARSERVER_PRIVATE_EVENTS("calendarserver-private-events"),
  CALENDARSERVER_PRIVATE_COMMENTS("calendarserver-private-comments"),
  CALENDARSERVER_SHARING("calendarserver-sharing"),
  CALENDARSERVER_SHARING_NO_SCHEDULING("calendarserver-sharing-no-scheduling"),
  CALENDAR_QUERY_EXTENDED("calendar-query-extended"),
  CALENDAR_DEFAULT_ALARMS("calendar-default-alarms"),
  ADDRESSBOOK("addressbook"),
  EXTENDED_MKCOL("extended-mkcol"),
  CALENDARSERVER_PRINCIPAL_PROPERTY_SEARCH("calendarserver-principal-property-search");

    private String description;

    private static Set<String> index = new HashSet<String>();

    static {
        for (SupportedFeature supportedFeature : SupportedFeature.values()) {
            index.add(supportedFeature.description());
        }
    }

    private SupportedFeature(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    public static ArrayList<String> descriptions() {
        return new ArrayList<String>(index);
    }

    public static SupportedFeature findByDescription(String value) {
        for (SupportedFeature feature : values()) {
            if (feature.description().equals(value)) {
                return feature;
            }
        }
        return null;
    }

}
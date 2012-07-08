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

public enum ResourceType {

  CALENDAR("calendar"),
  FREE_BUSY_URL("free-busy-url"),
  SCHEDULE_OUTBOX("schedule-outbox"),
  NOTIFICATION("notification"),
  DROPBOX_HOME("dropbox-home"),
  CALENDAR_PROXY_WRITE("calendar-proxy-write"),
  CALENDAR_PROXY_READ("calendar-proxy-read"),
  SHARE_OWNER("shared-owner"),
  ADRESSBOOK("addressbook"),
  SUBSCRIBED("subscribed"),
  COLLECTION("collection"),
  SCHEDULE_INBOX("schedule-inbox");
  
    private String description;

    private static Set<String> index = new HashSet<String>();

    static {
        for (ResourceType supportedFeature : ResourceType.values()) {
            index.add(supportedFeature.description());
        }
    }

    private ResourceType(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    public static ArrayList<String> descriptions() {
        return new ArrayList<String>(index);
    }

    public static ResourceType findByDescription(String value) {
        for (ResourceType feature : values()) {
            if (feature.description().equals(value)) {
                return feature;
            }
        }
        return null;
    }

}
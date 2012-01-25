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

import java.util.HashSet;
import java.util.Set;

import net.fortuna.ical4j.connector.CalendarCollection;

/**
 * $Id$
 *
 * Created on 20/02/2008
 *
 * Possible media types support by a {@link CalendarCollection}.
 * @author Ben
 *
 */
public enum MediaType {

    /**
     * iCalendar media type.
     */
    ICALENDAR_2_0("text/calendar", "2.0"),
    
    /**
     * vCard media type.
     */
    VCARD_4_0("text/vcard", "4.0");
    
    private String contentType;
    
    private String version;

    private static Set<String> index = new HashSet<String>();

    static {
        for (MediaType mediaType : MediaType.values()) {
            index.add(mediaType.getContentType());
        }
    }
    
    /**
     * @param contentType
     * @param version
     */
    private MediaType(String contentType, String version) {
        this.contentType = contentType;
        this.version = version;
    }
    
    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    public static MediaType findByContentTypeAndVersion(String contentType, String version) {
        for (MediaType feature : values()) {
            if ((feature.getContentType().equals(contentType)) && (feature.getVersion().equals(version))) {
                return feature;
            }
        }
        return null;
    }
}

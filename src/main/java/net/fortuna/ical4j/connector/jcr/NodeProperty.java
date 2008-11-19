/*
 * $Id$
 *
 * Created on 18/09/2008
 *
 * Copyright (c) 2008, Ben Fortuna
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
 *  o Neither the id of Ben Fortuna nor the names of any other contributors
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
package net.fortuna.ical4j.connector.jcr;

/**
 * @author Ben
 *
 */
public enum NodeProperty {
    
    COLLECTION_ID("ical4j:collection-id"),
    
    DESCRIPTION("ical4j:description"),
    
    DISPLAY_NAME("ical4j:display-id"),
    
    MAX_ATTENDEES("ical4j:max-attendees"),
    
    MAX_DATE_TIME("ical4j:max-date-time"),
    
    MAX_INSTANCES("ical4j:max-instances"),
    
    MAX_RESOURCE_SIZE("ical4j:max-resource-size"),
    
    MIN_DATE_TIME("ical4j:min-date-time"),
    
    UID("ical4j:uid");
    
    private String propertyName;
    
    /**
     * @param propertyName
     */
    private NodeProperty(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @return the propertyName
     */
    public final String getPropertyName() {
        return propertyName;
    }

}

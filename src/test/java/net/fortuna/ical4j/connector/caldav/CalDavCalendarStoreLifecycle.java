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
package net.fortuna.ical4j.connector.caldav;

import net.fortuna.ical4j.connector.CalendarStore;
import net.fortuna.ical4j.connector.ObjectStore;
import net.fortuna.ical4j.connector.ObjectStoreLifecycle;
import net.fortuna.ical4j.connector.dav.CalDavCalendarCollection;
import net.fortuna.ical4j.connector.dav.CalDavCalendarStore;

import org.apache.commons.httpclient.protocol.Protocol;

/**
 * $Id$
 *
 * Created on 01/03/2008
 *
 * @author Ben
 *
 */
public class CalDavCalendarStoreLifecycle implements ObjectStoreLifecycle<CalDavCalendarCollection> {

    protected static final String PRODID = "-//Ben Fortuna//iCal4j Connector 1.0//EN";

    private String host;
    
    private int port;
    
    private Protocol protocol;

    private String path;
    
    private CalendarStore<CalDavCalendarCollection> store;
    
    /**
     * @param id
     */
    public CalDavCalendarStoreLifecycle(String host, int port, String path) {
        this(host, port, Protocol.getProtocol("http"), path);
    }
    
    /**
     * @param host
     * @param port
     * @param protocol
     * @param path
     */
    public CalDavCalendarStoreLifecycle(String host, int port, Protocol protocol, String path) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.path = path;
//        storePath = BASE_STORE_PATH + id + "/";
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStoreLifecycle#getCalendarStore()
     */
    public ObjectStore<CalDavCalendarCollection> getObjectStore() {
        return store;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStoreLifecycle#shutdown()
     */
    public void shutdown() throws Exception {
        store = null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectStoreLifecycle#startup()
     */
    public void startup() throws Exception {
        store = new CalDavCalendarStore(PRODID, host, port, protocol, path);
    }

}

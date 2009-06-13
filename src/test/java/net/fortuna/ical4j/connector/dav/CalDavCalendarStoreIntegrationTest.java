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
package net.fortuna.ical4j.connector.dav;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.fortuna.ical4j.connector.ObjectStoreTest;
import net.fortuna.ical4j.connector.dav.CalDavCalendarCollection;
import net.fortuna.ical4j.connector.dav.PathResolver;

/**
 * $Id$
 *
 * Created on 01/03/2008
 *
 * @author Ben
 *
 */
public class CalDavCalendarStoreIntegrationTest extends TestSuite {
    
    /**
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(CalDavCalendarStoreIntegrationTest.class.getSimpleName());
        
        String host = "mediabase.local";
        int port = 8088;
        String username = "fortuna";
        char[] password = "connector".toCharArray();
        
        suite.addTest(new ObjectStoreTest<CalDavCalendarCollection>("testAddCollection",
                new CalDavCalendarStoreLifecycle(host, port, PathResolver.CHANDLER), username, password));
        suite.addTest(new ObjectStoreTest<CalDavCalendarCollection>("testGetCollection",
                new CalDavCalendarStoreLifecycle(host, port, PathResolver.CHANDLER), username, password));
        suite.addTest(new ObjectStoreTest<CalDavCalendarCollection>("testRemoveCollection",
                new CalDavCalendarStoreLifecycle(host, port, PathResolver.CHANDLER), username, password));
        return suite;
    }

}

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
package org.ical4j.connector.dav;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Ignore;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * $Id$
 *
 * Created on 01/03/2008
 *
 * @author Ben
 *
 */
@Ignore
public class CalDavCalendarStoreIntegrationTest extends TestSuite {
    
    public static Test suite() throws MalformedURLException {
        var suite = new TestSuite(CalDavCalendarStoreIntegrationTest.class.getSimpleName());
        
        final var url = new URL("http://localhost:8088");
        var username = "admin";
        var password = "admin".toCharArray();
        
        suite.addTest(new ObjectStoreTest<>("testAddCollection",
                new CalDavCalendarStoreLifecycle(url, PathResolver.Defaults.CHANDLER), username, password));
        suite.addTest(new ObjectStoreTest<>("testGetCollection",
                new CalDavCalendarStoreLifecycle(url, PathResolver.Defaults.CHANDLER), username, password));
        suite.addTest(new ObjectStoreTest<>("testRemoveCollection",
                new CalDavCalendarStoreLifecycle(url, PathResolver.Defaults.CHANDLER), username, password));
        return suite;
    }

}

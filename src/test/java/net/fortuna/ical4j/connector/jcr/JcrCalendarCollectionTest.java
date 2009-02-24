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
package net.fortuna.ical4j.connector.jcr;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.fortuna.ical4j.connector.CalendarCollectionTest;

/**
 * $Id$
 *
 * Created on 27/02/2008
 *
 * @author Ben
 *
 */
public class JcrCalendarCollectionTest extends AbstractRepositoryTest {
    
    /**
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(JcrCalendarCollectionTest.class.getSimpleName());
        
        suite.addTest(new CalendarCollectionTest<JcrCalendarCollection>("testGetDescription",
                new JcrCalendarStoreLifecycle("JcrCalendarCollection-testGetDescription"), USERNAME, PASSWORD));
        
        suite.addTest(new CalendarCollectionTest<JcrCalendarCollection>("testGetDisplayName",
                new JcrCalendarStoreLifecycle("JcrCalendarCollection-testGetDisplayName"), USERNAME, PASSWORD));
        
        suite.addTest(new CalendarCollectionTest<JcrCalendarCollection>("testGetCalendar",
                new JcrCalendarStoreLifecycle("JcrCalendarCollection-testGetCalendar"), USERNAME, PASSWORD));
        
        suite.addTest(new CalendarCollectionTest<JcrCalendarCollection>("testGetCalendars",
                new JcrCalendarStoreLifecycle("JcrCalendarCollection-testGetCalendars"), USERNAME, PASSWORD));
        
//        suite.addTest(new CalendarCollectionTest("testGetMaxAttendeesPerInstance",
//                new JcrCalendarStoreLifecycle("JcrCalendarCollection-testGetMaxAttendeesPerInstance"), USERNAME, PASSWORD));
        
//        suite.addTest(new CalendarCollectionTest("testGetMaxDateTime",
//                new JcrCalendarStoreLifecycle("JcrCalendarCollection-testGetMaxDateTime"), USERNAME, PASSWORD));
        
//        suite.addTest(new CalendarCollectionTest("testGetMaxInstances",
//                new JcrCalendarStoreLifecycle("JcrCalendarCollection-testGetMaxInstances"), USERNAME, PASSWORD));
        
//        suite.addTest(new CalendarCollectionTest("testGetMaxResourceSize",
//                new JcrCalendarStoreLifecycle("JcrCalendarCollection-testGetMaxResourceSize"), USERNAME, PASSWORD));
        
//        suite.addTest(new CalendarCollectionTest("testGetMinDateTime",
//                new JcrCalendarStoreLifecycle("JcrCalendarCollection-testGetMinDateTime"), USERNAME, PASSWORD));
        
        return suite;
    }
}

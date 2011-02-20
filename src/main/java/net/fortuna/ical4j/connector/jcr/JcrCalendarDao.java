/**
 * Copyright (c) 2011, Ben Fortuna
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

import java.util.List;

import javax.jcr.Session;

import org.jcrom.Jcrom;
import org.jcrom.dao.AbstractJcrDAO;

/**
 * 
 *
 * @author Ben
 *
 * Created on: 20/02/2009
 *
 * $Id$
 */
public class JcrCalendarDao extends AbstractJcrDAO<JcrCalendar> {

    /**
     * @param session a repository session
     * @param jcrom a JCROM instance
     */
    public JcrCalendarDao(Session session, Jcrom jcrom) {
        super(JcrCalendar.class, session, jcrom, new String[] {"mix:versionable"});
    }

    /**
     * @param path a root repository path
     * @param uid a calendar UID
     * @return a list of calendars with the specified UID
     */
    public List<JcrCalendar> findByUid(String path, String uid) {
        return super.findByXPath("/jcr:root" + path + "/*[@uid='" + uid + "']", "*", -1);
    }
}

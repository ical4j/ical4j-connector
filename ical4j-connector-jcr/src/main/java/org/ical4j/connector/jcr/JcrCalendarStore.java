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
package org.ical4j.connector.jcr;

import org.ical4j.connector.CalendarStore;
import org.jcrom.Jcrom;

import javax.jcr.Repository;

/**
 * $Id$
 *
 * Created on: 15/01/2009
 *
 * @author Ben
 *
 */
public class JcrCalendarStore extends AbstractJcrObjectStore<JcrCalendarCollection> 
    implements CalendarStore<JcrCalendarCollection> {
    
    private JcrCalendarCollectionDao collectionDao;
    
    /**
     * @param jcrom a JCROM instance
     * @param repository a repository instance
     * @param path the store repository path
     */
    public JcrCalendarStore(Jcrom jcrom, Repository repository, String path) {
        super(repository, path, jcrom);
        
        // ensure appropriate classes are mapped..
        jcrom.map(JcrCalendarCollection.class);
        jcrom.map(JcrCalendar.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected JcrCalendarCollection newCollection() {
        return new JcrCalendarCollection();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractJcrObjectCollectionDao<JcrCalendarCollection> getCollectionDao() {
        if (collectionDao == null) {
//            synchronized (this) {
//                if (collectionDao == null) {
                    collectionDao = new JcrCalendarCollectionDao(getSession(), getJcrom());
//                }
//            }
        }
        return collectionDao;
    }
}

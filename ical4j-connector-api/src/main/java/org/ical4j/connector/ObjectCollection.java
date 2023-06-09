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
package org.ical4j.connector;


import net.fortuna.ical4j.filter.FilterExpression;

import java.util.List;

/**
 * @param <T> the object type stored by the collection
 * 
 * $Id$
 *
 * Created on 27/09/2008
 *
 * @author Ben
 *
 */
public interface ObjectCollection<T> {

    String DEFAULT_COLLECTION = "default";

    /**
     * @return the collection name
     */
    String getDisplayName();
    
    /**
     * @return the collection description
     */
    String getDescription();

    /**
     * Return a list of object identifiers in the collection
     * @return a list of object identifiers
     */
    List<String> listObjectUids();

    /**
     * Returns all objects stored in the collection.
     * @return an array of collection objects
     * @throws ObjectStoreException where an unexpected error occurs
     */
    Iterable<T> getComponents() throws ObjectStoreException;

    /**
     * Returns a subset of objects that satisfy the specified filter expression.
     * @param filterExpression an iCal4j component filter expression
     * @return an iterable of objects matching the specified filter expressions
     * @throws ObjectStoreException when an unexpected error occurs.
     */
    default Iterable<T> query(FilterExpression filterExpression) throws ObjectStoreException {
        throw new UnsupportedOperationException("Collection filtering not yet supported");
    }

    /**
     * Returns a property value for the collection.
     * @param <T> the property return type
     * @param name the property name
     * @param type the property return type
     * @return a value of the specified type, or null if no property is found
     */
//    <T> T getProperty(String propertyName, Class<T> type);

    /**
     * Remove the collection from the underlying storage implementation.
     */
    void delete() throws ObjectStoreException;
}

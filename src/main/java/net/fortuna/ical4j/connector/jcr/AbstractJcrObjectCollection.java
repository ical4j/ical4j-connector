/*
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

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import net.fortuna.ical4j.connector.ObjectCollection;

import org.jcrom.AbstractJcrEntity;
import org.jcrom.JcrMappingException;
import org.jcrom.annotations.JcrProperty;

/**
 * 
 *
 * @author Ben
 *
 * Created on: 23/01/2009
 *
 * $Id$
 */
public abstract class AbstractJcrObjectCollection extends AbstractJcrEntity implements ObjectCollection {

    /**
     * 
     */
    private static final long serialVersionUID = -7943312823296190389L;

    @JcrProperty private String collectionName;

    @JcrProperty private String description;

    @JcrProperty private String displayName;

    private AbstractJcrObjectStore<? extends AbstractJcrObjectCollection> store;
    
    /**
     * @return the collectionName
     */
    public final String getCollectionName() {
        return collectionName;
    }

    /**
     * @param collectionName the collectionName to set
     */
    public final void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectCollection#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.ObjectCollection#getDisplayName()
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param description the description to set
     */
    public final void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param displayName the displayName to set
     */
    public final void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the store
     */
    public final AbstractJcrObjectStore<? extends AbstractJcrObjectCollection> getStore() {
        return store;
    }

    /**
     * @param store the store to set
     */
    public final void setStore(AbstractJcrObjectStore<? extends AbstractJcrObjectCollection> store) {
        this.store = store;
    }

    /**
     * @return
     * @throws PathNotFoundException
     * @throws JcrMappingException
     * @throws RepositoryException
     */
    protected final Node getNode() throws PathNotFoundException, JcrMappingException, RepositoryException {
        return getStore().getSession().getRootNode().getNode(getStore().getJcrom().getPath(this).substring(1));
    }

}

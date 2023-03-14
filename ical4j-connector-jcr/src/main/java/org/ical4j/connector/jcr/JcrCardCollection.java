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

import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.PropertyName;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.property.Uid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 *
 * @author Ben
 *
 * Created on: 23/01/2009
 *
 * $Id$
 */
public class JcrCardCollection extends AbstractJcrObjectCollection<VCard> implements CardCollection {

    private static final Log LOG = LogFactory.getLog(JcrCardCollection.class);
    
//    @JcrChildNode private List<JcrCard> cards;
    
    private JcrCardDao cardDao;
    
    /**
     * 
     */
    private static final long serialVersionUID = -3923756409472505791L;

    /**
     * 
     */
    public JcrCardCollection() {
//        cards = new ArrayList<JcrCard>();
    }

    @Override
    public List<String> listObjectUids() {
        //TODO: extract UIDs from vcard DAO..
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Uid addCard(VCard card) throws ObjectStoreException, ConstraintViolationException {
        Uid uid = card.getRequiredProperty(PropertyName.UID.toString());
        save(card);
        return uid;
    }

    @Override
    public Uid[] merge(VCard card) throws ObjectStoreException, ConstraintViolationException {
        Uid uid = card.getRequiredProperty(PropertyName.UID.toString());
        VCard existing = null;
        try {
            existing = getCard(uid.getValue());
        } catch (ObjectNotFoundException | FailedOperationException e) {
            throw new ObjectStoreException(e);
        }
        existing.addAll(card.getProperties());
        save(card);
        return Collections.singletonList(uid).toArray(new Uid[0]);
    }

    private void save(VCard card) throws ObjectStoreException {

        // initialise cards node..
        try {
            try {
                getNode().getNode("cards");
            } catch (PathNotFoundException e) {
                getNode().addNode("cards");
            }
        } catch (RepositoryException e) {
            throw new ObjectStoreException("Unexpected error", e);
        }

        JcrCard jcrCard = null;
        boolean update = false;

        Uid uid = card.getRequiredProperty(PropertyName.UID.toString());
        List<JcrCard> jcrCards = getCardDao().findByUid(getStore().getJcrom().getPath(this) + "/cards",
                uid.getValue());
        if (!jcrCards.isEmpty()) {
            jcrCard = jcrCards.get(0);
            update = true;
        }

        if (jcrCard == null) {
            jcrCard = new JcrCard();
        }

        jcrCard.setCard(card);

        if (update) {
            getCardDao().update(jcrCard);
        }
        else {
            getCardDao().create(getStore().getJcrom().getPath(this) + "/cards", jcrCard);
        }
    }

    @Override
    public VCard removeCard(String uid) throws ObjectNotFoundException, FailedOperationException {
        return null;
    }

    @Override
    public VCard getCard(String uid) throws ObjectNotFoundException, FailedOperationException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<VCard> getComponents() throws ObjectStoreException {
        List<VCard> cards = new ArrayList<VCard>();
        List<JcrCard> jcrCards = getCardDao().findAll(getStore().getJcrom().getPath(this) + "/cards");
        for (JcrCard card : jcrCards) {
            try {
                cards.add(card.getCard());
            }
            catch (Exception e) {
                LOG.error("Unexcepted error", e);
            }
        }
        return cards;
    }
    
    /**
     * @return
     */
    private JcrCardDao getCardDao() {
        if (cardDao == null) {
//            synchronized (this) {
//                if (cardDao == null) {
                    cardDao = new JcrCardDao(getStore().getSession(), getStore().getJcrom());
//                }
//            }
        }
        return cardDao;
    }
}

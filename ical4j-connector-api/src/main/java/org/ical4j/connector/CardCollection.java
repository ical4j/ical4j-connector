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
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardList;
import net.fortuna.ical4j.vcard.filter.VCardFilter;
import net.fortuna.ical4j.vcard.property.Uid;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * $Id$
 *
 * Created on 27/09/2008
 *
 * @author Ben
 *
 */
public interface CardCollection extends ObjectCollection<VCard> {

    /**
     * @param card a vCard object instance
     * @return the UID extracted from the vCard
     * @throws ObjectStoreException where an unexpected error occurs
     * @throws ConstraintViolationException where the specified object is not valid
     * @deprecated use {@link ObjectCollection#add(Object)}
     */
    @Deprecated
    default Uid addCard(VCard card) throws ObjectStoreException, ConstraintViolationException {
        return new Uid(add(card));
    }

    /**
     *
     * @param card
     * @return a list of UIDs extracted from the vCard data
     * @throws ObjectStoreException
     * @throws ConstraintViolationException
     */
    Uid[] merge(VCard card) throws ObjectStoreException, ConstraintViolationException;

    /**
     * Remove an existing card from the collection.
     *
     * @param uid the uid of the existing card
     * @return the card object that was removed from the collection
     * @throws ObjectNotFoundException
     * @throws FailedOperationException
     * @deprecated use {@link ObjectCollection#removeAll(String...)}
     *
     */
    @Deprecated
    default VCard removeCard(String uid) throws ObjectNotFoundException, FailedOperationException {
        List<VCard> result = removeAll(uid);
        return result.get(0);
    }

    /**
     *
     * @param uid
     * @return
     * @throws ObjectNotFoundException
     * @throws FailedOperationException
     * @deprecated use {@link ObjectCollection#getAll(String...)}
     */
    @Deprecated
    default VCard getCard(String uid) throws ObjectNotFoundException, FailedOperationException {
        Optional<VCard> card = get(uid);
        return card.orElse(null);
    }

    /**
     *
     * @param uids
     * @return
     * @throws FailedOperationException
     * @deprecated use {@link ObjectCollection#getAll(String...)}
     */
    @Deprecated
    default VCardList getCards(String... uids) throws FailedOperationException {
        List<VCard> cards = new ArrayList<>();
        for (String uid : uids) {
            try {
                cards.add(getCard(uid));
            } catch (ObjectNotFoundException e) {
                LoggerFactory.getLogger(CardCollection.class).warn("Calendar not found: " + uid);
            }
        }
        return new VCardList(cards);
    }

    @Override
    default List<VCard> query(FilterExpression filterExpression) {
        Predicate<VCard> filter = new VCardFilter().predicate(filterExpression);
        return getAll().stream().filter(filter).collect(Collectors.toList());
    }

    /**
     * Exports the entire collection as an array of objects.
     * @return a vCard object array that contains all cards in the collection
     */
    VCard[] export();
}

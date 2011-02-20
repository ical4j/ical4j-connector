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

import java.io.IOException;

import net.fortuna.ical4j.connector.MediaType;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.property.Uid;

import org.jcrom.AbstractJcrEntity;
import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;
import org.jcrom.JcrDataProvider.TYPE;
import org.jcrom.annotations.JcrFileNode;
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
public final class JcrCard extends AbstractJcrEntity {

    /**
     * 
     */
    private static final long serialVersionUID = -3268553509872379349L;
    
    @JcrProperty private String uid;
    
    @JcrFileNode private JcrFile file;

    private VCard card;
    
    /**
     * @return the underlying vCard object instance
     * @throws IOException where a communication error occurs
     * @throws ParserException where vCard parsing fails
     */
    public VCard getCard() throws IOException, ParserException {
        if (card == null) {
            VCardBuilder builder = new VCardBuilder(file.getDataProvider().getInputStream());
            card = builder.build();
        }
        return card;
    }
    
    /**
     * @param card a vCard object instance
     */
    public void setCard(VCard card) {
        this.card = card;
        
        Uid uidProp = (Uid) card.getProperty(Id.UID);
        if (uidProp != null) {
            setName(uidProp.getValue());
            this.uid = uidProp.getValue();
        }
        else {
            setName("card");
        }
        
        file = new JcrFile();
        file.setName("data");
        file.setDataProvider(new JcrDataProviderImpl(TYPE.BYTES, card.toString().getBytes()));
        file.setMimeType(MediaType.VCARD_4_0.getContentType());
        file.setLastModified(java.util.Calendar.getInstance());
    }

    /**
     * @return the uid
     */
    public final String getUid() {
        return uid;
    }
}

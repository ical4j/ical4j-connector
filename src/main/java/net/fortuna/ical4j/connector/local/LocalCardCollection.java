package net.fortuna.ical4j.connector.local;

import net.fortuna.ical4j.connector.CardCollection;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.enums.MediaType;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.VCardOutputter;
import net.fortuna.ical4j.vcard.property.Uid;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LocalCardCollection extends AbstractLocalObjectCollection<VCard> implements CardCollection {

    private static MediaType[] SUPPORTED_MEDIA_TYPES = new MediaType[1];
    static {
        SUPPORTED_MEDIA_TYPES[0] = MediaType.VCARD_4_0;
    }

    public LocalCardCollection(File root) {
        super(root);
    }

    @Override
    public void addCard(VCard card) throws ObjectStoreException, ConstraintViolationException {
        Uid uid = card.getProperty(Property.Id.UID);
        if (uid == null) {
            throw new ConstraintViolationException("A valid UID was not found.");
        }

        VCard existing = getCard(uid.getValue());
        if (existing != null) {
            // TODO: potentially merge/replace existing..
            throw new ObjectStoreException("Card already exists");
        }

        try {
            new VCardOutputter(false).output(card, new FileWriter(new File(getRoot(), uid.getValue() + ".vcf")));
        } catch (IOException e) {
            throw new ObjectStoreException("Error writing card file", e);
        }

    }

    public VCard getCard(String uid) {
        return null;
    }

    @Override
    public VCard[] getComponents() throws ObjectStoreException {
        List<VCard> cards = new ArrayList<>();

        try {
            for (File file : getRoot().listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return !pathname.isDirectory() && pathname.getName().endsWith(".vcf");
                }
            })) {
                VCardBuilder builder = new VCardBuilder(new FileInputStream(file));
                cards.add(builder.build());
            }
        } catch (IOException | ParserException e) {
            throw new ObjectStoreException(e);
        }
        return cards.toArray(new VCard[cards.size()]);
    }
}

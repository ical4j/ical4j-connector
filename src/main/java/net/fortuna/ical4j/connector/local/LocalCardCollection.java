package net.fortuna.ical4j.connector.local;

import net.fortuna.ical4j.connector.*;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.VCardOutputter;
import net.fortuna.ical4j.vcard.property.Uid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalCardCollection extends AbstractLocalObjectCollection<VCard> implements CardCollection {

    private static final MediaType[] SUPPORTED_MEDIA_TYPES = new MediaType[1];
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

        try {
            VCard existing = getCard(uid.getValue());

            // TODO: potentially merge/replace existing..
            throw new ObjectStoreException("Card already exists");
        } catch (ObjectNotFoundException e) {

        }

        try (FileWriter writer = new FileWriter(new File(getRoot(), uid.getValue() + ".vcf"))) {
            new VCardOutputter(false).output(card, writer);
        } catch (IOException e) {
            throw new ObjectStoreException("Error writing card file", e);
        }
    }

    public VCard getCard(String uid) throws ObjectNotFoundException {
        try {
            return new VCardBuilder(new FileInputStream(new File(getRoot(), uid + ".vcf"))).build();
        } catch (IOException | ParserException e) {
            throw new ObjectNotFoundException(String.format("Card not found: %s", uid), e);
        }
    }

    @Override
    public VCard removeCard(String uid) throws ObjectNotFoundException, FailedOperationException {
        VCard card = getCard(uid);
        if (!new File(getRoot(), uid + ".vcf").delete()) {
            throw new FailedOperationException("Unable to delete card: " + uid);
        }
        return card;
    }

    @Override
    public VCard[] getComponents() throws ObjectStoreException {
        List<VCard> cards = new ArrayList<>();

        File[] componentFiles = getRoot().listFiles(pathname ->
                !pathname.isDirectory() && pathname.getName().endsWith(".vcf"));
        if (componentFiles != null) {
            try {
                for (File file : componentFiles) {
                    VCardBuilder builder = new VCardBuilder(new FileInputStream(file));
                    cards.add(builder.build());
                }
            } catch (IOException | ParserException e) {
                throw new ObjectStoreException(e);
            }
        }
        return cards.toArray(new VCard[0]);
    }
}

package org.ical4j.connector.local;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.PropertyName;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.VCardOutputter;
import net.fortuna.ical4j.vcard.property.Uid;
import org.ical4j.connector.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
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
        Uid uid = card.getRequiredProperty(PropertyName.UID.toString());

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
            return new VCardBuilder(Files.newInputStream(new File(getRoot(), uid + ".vcf").toPath())).build();
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
    public Iterable<VCard> getComponents() throws ObjectStoreException {
        List<VCard> cards = new ArrayList<>();

        File[] componentFiles = getRoot().listFiles(pathname ->
                !pathname.isDirectory() && pathname.getName().endsWith(".vcf"));
        if (componentFiles != null) {
            try {
                for (File file : componentFiles) {
                    VCardBuilder builder = new VCardBuilder(Files.newInputStream(file.toPath()));
                    cards.add(builder.build());
                }
            } catch (IOException | ParserException e) {
                throw new ObjectStoreException(e);
            }
        }
        return cards;
    }
}

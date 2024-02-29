package org.ical4j.connector.local;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.PropertyName;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.VCardOutputter;
import net.fortuna.ical4j.vcard.property.Uid;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.MediaType;
import org.ical4j.connector.ObjectStoreException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class LocalCardCollection extends AbstractLocalObjectCollection<VCard> implements CardCollection {

    private static final MediaType[] SUPPORTED_MEDIA_TYPES = new MediaType[1];
    static {
        SUPPORTED_MEDIA_TYPES[0] = MediaType.VCARD_4_0;
    }

    public LocalCardCollection(File root) throws IOException {
        super(root);
    }

    @Override
    public List<String> listObjectUIDs() {
        return Arrays.stream(getObjectFiles()).map(file -> file.getName().split(".vcf")[0])
                .collect(Collectors.toList());
    }

    @Override
    public String add(VCard card) throws ObjectStoreException, ConstraintViolationException {
        Uid uid = card.getRequiredProperty(PropertyName.UID);

        Optional<VCard> existing = get(uid.getValue());
        if (existing.isPresent()) {
            // TODO: potentially merge/replace existing..
            throw new ObjectStoreException("Card already exists");
        }

        try (FileWriter writer = new FileWriter(new File(getRoot(), uid.getValue() + ".vcf"))) {
            new VCardOutputter(false).output(card, writer);
        } catch (IOException e) {
            throw new ObjectStoreException("Error writing card file", e);
        }

        // notify listeners..
        fireOnAddEvent(this, card);

        return uid.getValue();
    }

    @Override
    public Uid[] merge(VCard card) throws ObjectStoreException, ConstraintViolationException {
        Uid uid = card.getRequiredProperty(PropertyName.UID.toString());
        Optional<VCard> existing = get(uid.getValue());
        existing.ifPresent(vCard -> vCard.with(VCard.MERGE, card.getProperties()));
        save(card);

        // notify listeners..
        fireOnMergeEvent(this, card);

        return Collections.singletonList(uid).toArray(new Uid[0]);
    }

    private void save(VCard card) throws ObjectStoreException {
        Uid uid = card.getRequiredProperty(PropertyName.UID.toString());

        try (FileWriter writer = new FileWriter(new File(getRoot(), uid.getValue() + ".vcf"))) {
            new VCardOutputter(false).output(card, writer);
        } catch (IOException e) {
            throw new ObjectStoreException("Error writing card file", e);
        }
    }

    public Optional<VCard> get(String uid) {
        File cardFile = new File(getRoot(), uid + ".vcf");
        if (!cardFile.exists()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new VCardBuilder(Files.newInputStream(cardFile.toPath())).build());
        } catch (IOException | ParserException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<VCard> removeAll(String... uid) throws FailedOperationException {
        List<VCard> removed = new ArrayList<>();
        for (String u : uid) {
            File cardFile = new File(getRoot(), u + ".vcf");
            if (cardFile.exists()) {
                Optional<VCard> card = get(u);
                if (card.isPresent()) {
                    if (!cardFile.delete()) {
                        throw new FailedOperationException("Unable to delete card: " + u);
                    }
                    removed.add(card.get());
                    fireOnRemoveEvent(this, card.get());
                }
            }
        }
        return removed;
    }

//    @Override
//    public Iterable<VCard> getAll() throws ObjectStoreException {
//        List<VCard> cards = new ArrayList<>();
//
//        File[] componentFiles = getObjectFiles();
//        if (componentFiles != null) {
//            try {
//                for (File file : componentFiles) {
//                    VCardBuilder builder = new VCardBuilder(Files.newInputStream(file.toPath()));
//                    cards.add(builder.build());
//                }
//            } catch (IOException | ParserException e) {
//                throw new ObjectStoreException(e);
//            }
//        }
//        return cards;
//    }

    @Override
    public VCard[] export() {
        List<VCard> export = new ArrayList<>(getAll());
        return export.toArray(new VCard[0]);
    }

    private File[] getObjectFiles() {
        return getRoot().listFiles(pathname ->
                !pathname.isDirectory() && pathname.getName().endsWith(".vcf"));
    }
}

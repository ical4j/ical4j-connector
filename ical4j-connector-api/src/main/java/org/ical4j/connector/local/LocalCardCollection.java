package org.ical4j.connector.local;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ConstraintViolationException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LocalCardCollection extends AbstractLocalObjectCollection<VCard> implements CardCollection {

    private static final MediaType[] SUPPORTED_MEDIA_TYPES = new MediaType[1];
    static {
        SUPPORTED_MEDIA_TYPES[0] = MediaType.VCARD_4_0;
    }

    private static final String FILE_EXTENSION = ".vcf";

    public LocalCardCollection(File root) throws IOException {
        super(root);
    }

    @Override
    public List<String> listObjectUIDs() {
        return Arrays.stream(getObjectFiles()).map(file -> file.getName().split(FILE_EXTENSION)[0])
                .collect(Collectors.toList());
    }

    @Override
    public String add(VCard card) throws ObjectStoreException, ConstraintViolationException {
        var uid = card.getUid();
        Optional<VCard> existing = get(uid.getValue());

        try (var writer = new FileWriter(new File(getRoot(), uid.getValue() + FILE_EXTENSION))) {
            if (existing.isPresent()) {
                new VCardOutputter(false).output(existing.get().merge(card), writer);
            } else {
                new VCardOutputter(false).output(card, writer);
            }
        } catch (IOException e) {
            throw new ObjectStoreException("Error writing card file", e);
        }

        // notify listeners..
        fireOnAddEvent(this, card);

        return uid.getValue();
    }

    @Override
    public Optional<VCard> get(String uid) {
        var cardFile = new File(getRoot(), uid + FILE_EXTENSION);
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
        for (var u : uid) {
            var cardFile = new File(getRoot(), u + FILE_EXTENSION);
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

    @Override
    public Uid[] merge(VCard card) throws ObjectStoreException, ConstraintViolationException {
        var uidCards = card.split();
        for (var c : uidCards) {
            var uid = c.getUid();
            Optional<VCard> existing = get(uid.getValue());

            if (existing.isPresent()) {
                // TODO: potentially merge/replace existing..
                throw new ObjectStoreException("Card already exists");
            }

            try (var writer = new FileWriter(new File(getRoot(), uid.getValue() + FILE_EXTENSION))) {
                new VCardOutputter(false).output(c, writer);
            } catch (IOException e) {
                throw new ObjectStoreException("Error writing card file", e);
            }
        }

        // notify listeners..
        fireOnMergeEvent(this, card);

        return Arrays.stream(uidCards).map(VCard::getUid).toArray(Uid[]::new);
    }

    private void save(VCard card) throws ObjectStoreException {
        var uid = card.getUid();

        try (var writer = new FileWriter(new File(getRoot(), uid.getValue() + FILE_EXTENSION))) {
            new VCardOutputter(false).output(card, writer);
        } catch (IOException e) {
            throw new ObjectStoreException("Error writing card file", e);
        }
    }

    @Override
    public VCard export() {
        var export = new VCard();
        for (var object : getObjectFiles()) {
            try {
                export = export.merge(new VCardBuilder(Files.newInputStream(object.toPath())).build());
            } catch (IOException | ParserException e) {
                throw new RuntimeException(e);
            }
        }
        return export;
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

    private File[] getObjectFiles() {
        return getRoot().listFiles(pathname ->
                !pathname.isDirectory() && pathname.getName().endsWith(FILE_EXTENSION));
    }
}

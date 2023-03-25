package org.ical4j.connector.command;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.ObjectStore;
import picocli.CommandLine;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public abstract class AbstractCardCommand<T> extends AbstractCollectionCommand<CardCollection, T> {

    @CommandLine.ArgGroup(multiplicity = "1")
    protected Input input;

    static class Input {
        @CommandLine.Option(names = {"-U", "--url"}, required = true)
        protected URL url;

        @CommandLine.Option(names = {"-F", "--file"}, required = true)
        protected String filename;

        @CommandLine.Option(names = {"--stdin"}, required = true)
        protected boolean stdin;
    }

    private VCard card;

    public AbstractCardCommand(String collectionName) {
        super(collectionName);
    }

    public AbstractCardCommand(String collectionName, Consumer<T> consumer) {
        super(collectionName, consumer);
    }

    public AbstractCardCommand(String collectionName, ObjectStore<CardCollection> store) {
        super(collectionName, store);
    }

    public AbstractCardCommand<T> withCard(VCard card) {
        this.card = card;
        return this;
    }

    public VCard getCard() throws ParserException, IOException {
        if (card == null) {
            if (input.filename != null) {
                card = new VCardBuilder(new FileReader(input.filename)).build();
            } else if (input.url != null) {
                card = new VCardBuilder(input.url.openStream()).build();
            } else if (input.stdin) {
                card = new VCardBuilder(System.in).build();
            }
        }
        return card;
    }
}

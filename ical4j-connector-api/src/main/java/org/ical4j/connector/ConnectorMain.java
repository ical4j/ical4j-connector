package org.ical4j.connector;

import org.ical4j.connector.command.*;
import picocli.CommandLine;

@CommandLine.Command(name = "connector", description = "iCal4j Connector",
        subcommands = {CollectionCommand.class, CalendarCommand.class, CardCommand.class, JettyRun.class},
        mixinStandardHelpOptions = true, versionProvider = VersionProvider.class)
public class ConnectorMain {

    public static void main(String[] args) {
        new CommandLine(new ConnectorMain()).execute(args);
    }
}

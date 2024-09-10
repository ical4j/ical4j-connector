package org.ical4j.connector.local;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.util.Calendars;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Properties;

public class LocalCollectionConfiguration {

    public static final String DEFAULT_CONFIG_DIR = ".ical4j";

    private static final String PROPERTIES_FILE_NAME = "config";

    private static final String TIMEZONE_FILE_NAME = "timezone";

    private static final Logger LOG = LoggerFactory.getLogger(LocalCollectionConfiguration.class);

    private final File root;

    private final Properties properties;

    public LocalCollectionConfiguration() {
        this(new File(DEFAULT_CONFIG_DIR));
    }

    public LocalCollectionConfiguration(File root) {
        if (!root.isDirectory()) {
            throw new IllegalArgumentException("Root must be a directory");
        }
        this.root = root;
        this.properties = new Properties();
        try (var in = Files.newInputStream(new File(root, PROPERTIES_FILE_NAME).toPath())) {
            properties.load(in);
        } catch (IOException | NullPointerException e) {
            LOG.info("ical4j config not found.");
        }
    }

    public String getDisplayName() {
        return properties.getProperty("DisplayName");
    }

    public String getDescription() {
        return properties.getProperty("Description");
    }

    public String[] getSupportedComponentTypes() {
        return properties.getProperty("SupportedComponents", "").split(",");
    }

    public Calendar getTimeZone() {
        try {
            return Calendars.load(new File(root, TIMEZONE_FILE_NAME).getAbsolutePath());
        } catch (IOException | ParserException e) {
            LOG.error("Unable to retrieve timezone");
        }
        return null;
    }

    public void setDisplayName(String displayName) throws IOException {
        properties.setProperty("DisplayName", displayName);
        saveProperties();
    }

    public void setDescription(String description) throws IOException {
        properties.setProperty("Description", description);
        saveProperties();
    }

    public void setSupportedComponents(String[] supportedComponents) throws IOException {
        properties.setProperty("SupportedComponents", StringUtils.join(supportedComponents, ","));
        saveProperties();
    }

    public void setTimeZone(Calendar timezone) throws IOException {
        new CalendarOutputter(false).output(timezone, new FileWriter(new File(root, TIMEZONE_FILE_NAME)));
    }

    public boolean delete() {
        new File(root, PROPERTIES_FILE_NAME).delete();
        new File(root, TIMEZONE_FILE_NAME).delete();
        return root.delete();
    }

    private void saveProperties() throws IOException {
//        if (!root.exists() && !root.mkdirs()) {
//            throw new IOException("Unable to create config directory");
//        }
        try (var fw = new FileWriter(new File(root, PROPERTIES_FILE_NAME))) {
            properties.store(fw, String.format("%s", new Date()));
        }
    }
}

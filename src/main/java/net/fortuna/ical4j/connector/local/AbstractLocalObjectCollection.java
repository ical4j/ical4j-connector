package net.fortuna.ical4j.connector.local;

import net.fortuna.ical4j.connector.ObjectCollection;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.util.Calendars;
import net.fortuna.ical4j.util.ResourceLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

public abstract class AbstractLocalObjectCollection<T> implements ObjectCollection<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractLocalObjectCollection.class);

    private static final String PROPERTIES_FILE_NAME = ".config";

    private static final String TIMEZONE_FILE_NAME = ".timezone";

    private final File root;

    private final Properties properties;

    public AbstractLocalObjectCollection(File root) {
        if (!root.isDirectory()) {
            throw new IllegalArgumentException("Root must be a directory");
        }
        this.root = root;
        this.properties = new Properties();
        try (InputStream in = ResourceLoader.getResourceAsStream(PROPERTIES_FILE_NAME)) {
            properties.load(in);
        } catch (IOException | NullPointerException e) {
            LOG.info("ical4j.properties not found.");
        }
    }

    protected File getRoot() {
        return root;
    }

    @Override
    public String getDisplayName() {
        return properties.getProperty("DisplayName");
    }

    @Override
    public String getDescription() {
        return properties.getProperty("Description");
    }

    public String[] getSupportedComponentTypes() {
        return properties.getProperty("SupportedComponents").split(",");
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

    private void saveProperties() throws IOException {
        properties.store(new FileWriter(new File(root, PROPERTIES_FILE_NAME)), String.format("%s", new Date()));
    }

}

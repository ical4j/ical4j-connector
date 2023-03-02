package org.ical4j.connector.local;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.ObjectCollection;

import java.io.File;
import java.io.IOException;

public abstract class AbstractLocalObjectCollection<T> implements ObjectCollection<T> {

    private final File root;

    private final LocalCollectionConfiguration configuration;

    public AbstractLocalObjectCollection(File root) throws IOException {
        if (!root.isDirectory()) {
            throw new IllegalArgumentException("Root must be a directory");
        }
        this.root = root;
        File configRoot = new File(root, LocalCollectionConfiguration.DEFAULT_CONFIG_DIR);
        if ((configRoot.exists() && !configRoot.isDirectory()) ||
                (!configRoot.exists() && !configRoot.mkdirs())) {
            throw new IOException("Unable to initialise collection config");
        }
        this.configuration = new LocalCollectionConfiguration(configRoot);
    }

    protected File getRoot() {
        return root;
    }

    @Override
    public String getDisplayName() {
        return configuration.getDisplayName();
    }

    @Override
    public String getDescription() {
        return configuration.getDescription();
    }

    public String[] getSupportedComponentTypes() {
        return configuration.getSupportedComponentTypes();
    }

    public Calendar getTimeZone() {
        return configuration.getTimeZone();
    }

    public void setDisplayName(String displayName) throws IOException {
        configuration.setDisplayName(displayName);
    }

    public void setDescription(String description) throws IOException {
        configuration.setDescription(description);
    }

    public void setSupportedComponents(String[] supportedComponents) throws IOException {
        configuration.setSupportedComponents(supportedComponents);
    }

    public void setTimeZone(Calendar timezone) throws IOException {
        configuration.setTimeZone(timezone);
    }

    @Override
    public String toString() {
        return String.format("LocalCollection[%s]", getDisplayName());
    }
}

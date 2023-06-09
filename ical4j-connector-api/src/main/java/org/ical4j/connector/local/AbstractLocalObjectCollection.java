package org.ical4j.connector.local;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStoreException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public abstract class AbstractLocalObjectCollection<T> implements ObjectCollection<T> {

    private final File root;

    private final LocalCollectionConfiguration configuration;

    public AbstractLocalObjectCollection(File root) throws IOException {
        this.root = Objects.requireNonNull(root);
        if (!root.isDirectory()) {
            throw new IllegalArgumentException("Root must be a directory");
        }
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
    public void delete() throws ObjectStoreException {
        if (Objects.requireNonNull(
                root.list((root, name) -> !name.equals(LocalCollectionConfiguration.DEFAULT_CONFIG_DIR))).length > 0) {
            throw new ObjectStoreException("Collection is not empty. Remove all contents before deleting.");
        }
        if (!configuration.delete() || !root.delete()) {
            throw new ObjectStoreException("Unable to delete collection");
        }
    }

    @Override
    public String toString() {
        return String.format("LocalCollection[%s]", getDisplayName());
    }
}

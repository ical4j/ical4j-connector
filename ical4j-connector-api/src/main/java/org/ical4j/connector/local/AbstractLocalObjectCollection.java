package org.ical4j.connector.local;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.AbstractObjectCollection;
import org.ical4j.connector.ObjectStoreException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Abstract base class for local object collections.
 * Provides common functionality for managing local collections, including configuration handling.
 *
 * @param <T> the type of objects in the collection
 */
abstract class AbstractLocalObjectCollection<T> extends AbstractObjectCollection<T> {

    private final File root;

    private final LocalCollectionConfiguration configuration;

    public AbstractLocalObjectCollection(File root) throws IOException {
        this.root = Objects.requireNonNull(root);
//        if (!root.isDirectory()) {
//            throw new IllegalArgumentException("Root must be a directory");
//        }
        var configRoot = new File(root, LocalCollectionConfiguration.DEFAULT_CONFIG_DIR);
        var legacyConfigRoot = new File(root, LocalCollectionConfiguration.LEGACY_CONFIG_DIR);
        if (!configRoot.exists() && legacyConfigRoot.isDirectory() && !legacyConfigRoot.renameTo(configRoot)) {
            // migration failed, continue using the legacy config..
            configRoot = legacyConfigRoot;
        }
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
        return Optional.ofNullable(configuration.getDisplayName()).orElse(getRoot().getName());
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
                root.list((root, name) -> !name.equals(LocalCollectionConfiguration.DEFAULT_CONFIG_DIR)
                        && !name.equals(LocalCollectionConfiguration.LEGACY_CONFIG_DIR))).length > 0) {
            throw new ObjectStoreException("Collection is not empty. Remove all contents before deleting.");
        }
        var legacyConfigRoot = new File(root, LocalCollectionConfiguration.LEGACY_CONFIG_DIR);
        if (!configuration.delete()
                || (legacyConfigRoot.isDirectory() && !new LocalCollectionConfiguration(legacyConfigRoot).delete())
                || !root.delete()) {
            throw new ObjectStoreException("Unable to delete collection");
        }
    }

    @Override
    public String toString() {
        return String.format("LocalCollection[%s]", getDisplayName());
    }
}

package org.ical4j.connector.dav;

import org.apache.http.HttpStatus;
import org.apache.jackrabbit.webdav.*;
import org.apache.jackrabbit.webdav.lock.*;
import org.apache.jackrabbit.webdav.property.*;
import org.ical4j.connector.dav.response.GetPropertyValue;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.jackrabbit.webdav.property.DavPropertyName.DISPLAYNAME;
import static org.apache.jackrabbit.webdav.property.DavPropertyName.GETLASTMODIFIED;

public abstract class AbstractDavResource<T extends WebDavSupport> implements DavResource {

    private final DavResourceFactory factory;

    private final DavResourceLocator locator;

    private final DavResource parent;

    private final List<DavResource> children;

    protected final DavPropertySet properties;

    protected final T client;

    private boolean exists;

    public AbstractDavResource(DavResourceFactory factory, DavResourceLocator locator,
                               DavPropertySet properties, T client, DavResource parent) {
        this.factory = factory;
        this.locator = locator;
        this.properties = properties;
        this.client = client;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    @Override
    public String getComplianceClass() {
        return "";
    }

    @Override
    public String getSupportedMethods() {
        return METHODS;
    }

    @Override
    public String getHref() {
        return locator.getHref(isCollection());
    }

    @Override
    public boolean isCollection() {
        return "collection".equals(getPropertyValue(DavPropertyName.RESOURCETYPE));
    }

    @Override
    public String getDisplayName() {
        return getPropertyValue(DISPLAYNAME);
    }

    @Override
    public String getResourcePath() {
        return locator.getResourcePath();
    }

    @Override
    public boolean exists() {
        if (!exists) {
            try {
                exists = client.head(getResourcePath(), response ->
                        response.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_FOUND);
            } catch (IOException e) {
                LoggerFactory.getLogger(AbstractDavResource.class).error("Server request failed", e);
            }
        }
        return exists;
    }

    @Override
    public long getModificationTime() {
        Long lastModified = getPropertyValue(GETLASTMODIFIED);
        return lastModified != null ? lastModified : -1;
    }

    @Override
    public DavPropertySet getProperties() {
        DavPropertySet copy = new DavPropertySet();
        copy.addAll(properties);
        return copy;
    }

    @Override
    public DavPropertyName[] getPropertyNames() {
        try {
            return this.client.propFindAll(getResourcePath()).get(0).getProperties().getPropertyNames();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks for cached property value, and if not found uses PROPFIND query to DAV server.
     * @param name a property name
     * @return a property value, or null if property doesn't exist
     */
    @Override
    public DavProperty<?> getProperty(DavPropertyName name) {
        DavProperty<?> property = properties.get(name);
        if (property == null) {
            try {
                DavPropertySet result = client.propFind(getResourcePath(), name).get(0).getProperties();
                this.properties.addAll(result);
                property = properties.get(name);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return property;
    }

    private <P> P getPropertyValue(DavPropertyName name) {
        DavPropertyNameSet nameSet = new DavPropertyNameSet();
        nameSet.add(name);
        try {
            return client.propFind(getResourcePath(), nameSet, new GetPropertyValue<>());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DavResourceLocator getLocator() {
        return locator;
    }

    @Override
    public DavResourceFactory getFactory() {
        return factory;
    }

    @Override
    public void setProperty(DavProperty<?> property) throws DavException {
        try {
            this.client.propPatchSet(getResourcePath(), property);
            this.properties.add(property);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeProperty(DavPropertyName propertyName) throws DavException {
        try {
            this.properties.remove(propertyName);
            this.client.propPatchRemove(getResourcePath(), propertyName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MultiStatusResponse alterProperties(List<? extends PropEntry> changeList) throws DavException {
        try {
            return this.client.propPatch(getResourcePath(), changeList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DavResource getCollection() {
        return parent;
    }

    @Override
    public DavResourceIterator getMembers() {
        return new DavResourceIteratorImpl(children);
    }

    @Override
    public void move(DavResource destination) throws DavException {
        try {
            client.move(getResourcePath(), destination.getResourcePath());
            this.exists = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void copy(DavResource destination, boolean shallow) throws DavException {
        try {
            client.copy(getResourcePath(), destination.getResourcePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isLockable(Type type, Scope scope) {
        return false;
    }

    @Override
    public boolean hasLock(Type type, Scope scope) {
        return false;
    }

    @Override
    public ActiveLock getLock(Type type, Scope scope) {
        return null;
    }

    @Override
    public ActiveLock[] getLocks() {
        return new ActiveLock[0];
    }

    @Override
    public ActiveLock lock(LockInfo reqLockInfo) throws DavException {
        return null;
    }

    @Override
    public ActiveLock refreshLock(LockInfo reqLockInfo, String lockToken) throws DavException {
        return null;
    }

    @Override
    public void unlock(String lockToken) throws DavException {

    }

    @Override
    public void addLockManager(LockManager lockmgr) {

    }

    @Override
    public DavSession getSession() {
        return null;
    }
}

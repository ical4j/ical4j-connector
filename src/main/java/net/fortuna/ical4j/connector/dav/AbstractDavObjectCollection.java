package net.fortuna.ical4j.connector.dav;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import net.fortuna.ical4j.connector.ObjectCollection;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.method.GetMethod;

import org.apache.commons.httpclient.HttpException;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

/**
 * Created: [20/11/2008]
 *
 * @author fortuna
 */
public abstract class AbstractDavObjectCollection implements ObjectCollection {

    private AbstractDavObjectStore<?> store;

    private String id;

    /**
     * @param id
     */
    public AbstractDavObjectCollection(AbstractDavObjectStore<?> store, String id) {
        this.store = store;
        this.id = id;
    }

    /**
     * @return the store
     */
    public final AbstractDavObjectStore<?> getStore() {
        return store;
    }

    /**
     * @return the id
     */
    public final String getId() {
        return id;
    }

    /**
     * @return
     */
    public final String getPath() {
        return getStore().getPath() + getId();
    }

    /**
     * @param propertyName
     * @return
     * @throws IOException
     * @throws ObjectStoreException
     * @throws DavException
     */
    public final <T> T getProperty(DavPropertyName propertyName, Class<T> type) throws IOException, ObjectStoreException, DavException {
        DavPropertyNameSet set = new DavPropertyNameSet();
        set.add(propertyName);

        PropFindMethod propFindMethod = new PropFindMethod(store.getPath() + id, set, DavConstants.DEPTH_0);
        store.execute(propFindMethod);

        if (!propFindMethod.succeeded()) {
            throw new ObjectStoreException(propFindMethod.getStatusLine().toString());
        }

        MultiStatus multi = propFindMethod.getResponseBodyAsMultiStatus();
        DavPropertySet props = multi.getResponses()[0].getProperties(200);
        if (props.get(propertyName) != null) {
            Object value = props.get(propertyName).getValue();
            try {
                if (Collection.class.isAssignableFrom(type)) {
                    T result = type.newInstance();
                    if (value instanceof Collection<?>) {
                        ((Collection<?>) result).addAll((Collection) value);
                    }
                    return result;
                }
                else {
                    Constructor<T> constructor = type.getConstructor(value.getClass());
                    return constructor.newInstance(value);
                }
            }
            catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return (T) props.get(propertyName).getValue();
        }
        return null;
    }

    /**
     * @throws HttpException
     * @throws IOException
     * @throws ObjectStoreException
     */
    public final void delete() throws HttpException, IOException, ObjectStoreException {
        DeleteMethod deleteMethod = new DeleteMethod(getPath());
        getStore().execute(deleteMethod);
        if (!deleteMethod.succeeded()) {
            throw new ObjectStoreException(deleteMethod.getStatusCode() + ": "
                    + deleteMethod.getStatusText());
        }
    }

    /**
     * @return
     * @throws HttpException
     * @throws IOException
     * @throws ObjectStoreException
     */
    public final boolean exists() throws HttpException, IOException, ObjectStoreException {
        GetMethod getMethod = new GetMethod(getPath());
        getStore().execute(getMethod);
        if (getMethod.getStatusCode() == DavServletResponse.SC_OK) {
            return true;
        }
        else if (getMethod.getStatusCode() == DavServletResponse.SC_NOT_FOUND) {
            return false;
        }
        else {
            throw new ObjectStoreException(getMethod.getStatusLine().toString());
        }
    }
}

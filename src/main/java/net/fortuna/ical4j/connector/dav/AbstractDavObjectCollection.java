/**
 * Copyright (c) 2010, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
 * @param <T> the supported collection object type
 * 
 * Created: [20/11/2008]
 *
 * @author fortuna
 */
public abstract class AbstractDavObjectCollection<T> implements ObjectCollection<T> {

    private final AbstractDavObjectStore<?> store;

    private final String id;

    /**
     * @param store the container store for the collection
     * @param id collection identifier
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
     * @return the absolute collection path
     */
    public final String getPath() {
        return getId();
    }

    /**
     * @param <T> the property type
     * @param propertyName a property name
     * @param type the class for the property type returned (HACK!!)
     * @return the value for the specified property name
     * @throws IOException if there is a communication error
     * @throws ObjectStoreException where an unexpected error occurs
     * @throws DavException where an error occurs calling the DAV method
     */
    @SuppressWarnings("unchecked")
	public final <T> T getProperty(DavPropertyName propertyName, Class<T> type)
        throws IOException, ObjectStoreException, DavException {
        
        DavPropertyNameSet set = new DavPropertyNameSet();
        set.add(propertyName);

        PropFindMethod propFindMethod = new PropFindMethod(getPath(), set, DavConstants.DEPTH_0);
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
     * @throws HttpException where an error occurs calling the HTTP method
     * @throws IOException if there is a communication error
     * @throws ObjectStoreException where an unexpected error occurs
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
     * @return true if the collection exists, otherwise false
     * @throws HttpException where an error occurs calling the HTTP method
     * @throws IOException if there is a communication error
     * @throws ObjectStoreException where an unexpected error occurs
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

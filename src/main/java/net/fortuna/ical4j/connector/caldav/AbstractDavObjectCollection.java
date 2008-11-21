/*
 * This file is part of Touchbase.
 *
 * Created: [20/11/2008]
 *
 * Copyright (c) 2008, Ben Fortuna
 *
 * Touchbase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Touchbase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Touchbase.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.fortuna.ical4j.connector.caldav;

import java.io.IOException;

import net.fortuna.ical4j.connector.AbstractCalendarCollection;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.caldav.method.GetMethod;

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
 * @author fortuna
 *
 */
public abstract class AbstractDavObjectCollection extends AbstractCalendarCollection {

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
        return (T) props.get(propertyName).getValue();
    }
    
    /**
     * @throws HttpException
     * @throws IOException
     * @throws ObjectStoreException
     */
    public final void delete() throws HttpException, IOException, ObjectStoreException {
        DeleteMethod deleteMethod = new DeleteMethod(getPath());
        getStore().execute(deleteMethod);
        if (!deleteMethod.succeeded()){
            throw new ObjectStoreException(deleteMethod.getStatusCode() + ": " + deleteMethod.getStatusText());
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

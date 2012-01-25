/**
 * Copyright (c) 2012, Ben Fortuna
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.fortuna.ical4j.connector.ObjectCollection;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.enums.MediaType;
import net.fortuna.ical4j.connector.dav.enums.ResourceType;
import net.fortuna.ical4j.connector.dav.property.BaseDavPropertyName;
import net.fortuna.ical4j.connector.dav.property.CalDavPropertyName;

import org.apache.commons.httpclient.HttpException;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
    
    protected DavPropertySet properties;
    
    private String _ownerName = null;

    /**
     * @param store the container store for the collection
     * @param id collection identifier
     */
    public AbstractDavObjectCollection(AbstractDavObjectStore<?> store, String id) {
        this.store = store;
        this.id = id;
        this.properties = new DavPropertySet();
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
// FIXME fix for CGP...
        if (!getId().endsWith("/")) {
            return getId() + "/";
        }
        return getId();
    }
    
    /**
     * Returns a list of the kinds of resource type for this collection. For example, for a collection that supports
     * iCalendar object, "calendar" will be one of the resource types.
     */
    public ResourceType[] getResourceTypes() {
        List<ResourceType> resourceTypes = new ArrayList<ResourceType>();

        try {
            ArrayList<Node> resourceTypeProp;
            resourceTypeProp = getProperty(BaseDavPropertyName.RESOURCETYPE, ArrayList.class);
            if (resourceTypeProp != null) {
                for (Node child : resourceTypeProp) {
                    if (child instanceof Element) {
                        String nameNode = child.getNodeName();
                        if (nameNode != null) {
                            ResourceType type = ResourceType.findByDescription(nameNode);
                            if (type != null) {
                                resourceTypes.add(type);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ObjectStoreException e) {
            e.printStackTrace();
        } catch (DavException e) {
            e.printStackTrace();
        }

        return resourceTypes.toArray(new ResourceType[resourceTypes.size()]);
    }
    
    /**
     * Returns a list of supported media types. For example, a CalDAV server will probably return 2.0 as the supported
     * version and text/calendar as the content-type.
     */
    public MediaType[] getSupportedMediaTypes() {
        List<MediaType> mediaTypes = new ArrayList<MediaType>();

        try {
            ArrayList<Node> mediaTypeProp;
            mediaTypeProp = getProperty(CalDavPropertyName.SUPPORTED_CALENDAR_DATA, ArrayList.class);
            if (mediaTypeProp != null) {
                for (Node child : mediaTypeProp) {
                    if (child instanceof Element) {
                        String nameNode = child.getNodeName();
                        if ((nameNode != null) && ("calendar-data".equals(nameNode))) {
                            String contentType = ((Element) child).getAttribute("content-type");
                            String version = ((Element) child).getAttribute("version");
                            MediaType type = MediaType.findByContentTypeAndVersion(contentType, version);
                            if (type != null) {
                                mediaTypes.add(type);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ObjectStoreException e) {
            e.printStackTrace();
        } catch (DavException e) {
            e.printStackTrace();
        }
        return mediaTypes.toArray(new MediaType[mediaTypes.size()]);
    }
    
    /**
     * Indicates the maximum amount of additional storage available to be allocated to a resource.
     */
    public Long getQuotaAvailableBytes() {
        try {
            Long calTimezoneProp = getProperty(BaseDavPropertyName.QUOTA_AVAILABLE_BYTES, Long.class);
            if (calTimezoneProp != null) {
                return calTimezoneProp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ObjectStoreException e) {
            e.printStackTrace();
        } catch (DavException e) {
            e.printStackTrace();
        }
        return new Long(0);
    }
    
    /**
     * Contains the amount of storage counted against the quota on a resource.
     */
    public Long getQuotaUsedBytes() {
        try {
            Long calTimezoneProp = getProperty(BaseDavPropertyName.QUOTA_USED_BYTES, Long.class);
            if (calTimezoneProp != null) {
                return calTimezoneProp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ObjectStoreException e) {
            e.printStackTrace();
        } catch (DavException e) {
            e.printStackTrace();
        }
        return new Long(0);
    }
    
    /**
     * Href (link) to the owner of this collection
     */
    public String getOwnerHref() {
        String ownerHref = null;
        try {
            ArrayList<Node> ownerProp;
            ownerProp = getProperty(SecurityConstants.OWNER, ArrayList.class);
            if (ownerProp != null) {
                for (Node child : ownerProp) {
                    if (child instanceof Element) {
                        String nameNode = child.getNodeName();
                        if ((nameNode != null) && ("href".equals(nameNode))) {
                            ownerHref = child.getTextContent();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ObjectStoreException e) {
            e.printStackTrace();
        } catch (DavException e) {
            e.printStackTrace();
        }
        return ownerHref;
    }

    /**
     * Name of the owner of this collection. Will be retrieved by the owner href
     */
    public String getOwnerName() {
        if ((_ownerName == null) && (getOwnerHref() != null)) {
            try {
                DavPropertyNameSet nameSet = new DavPropertyNameSet();
                nameSet.add(DavPropertyName.DISPLAYNAME);
                PropFindMethod aGet = new PropFindMethod(getOwnerHref(), nameSet, 0);
                aGet.setDoAuthentication(true);
                
                getStore().getClient().execute(aGet);

                if (aGet.getStatusCode() == DavServletResponse.SC_MULTI_STATUS) {
                    MultiStatus multiStatus = aGet.getResponseBodyAsMultiStatus();
                    MultiStatusResponse[] responses = multiStatus.getResponses();
                    
                    for (int i = 0; i < responses.length; i++) {
                        MultiStatusResponse msResponse = responses[i];
                        DavPropertySet foundProperties = msResponse.getProperties(200);
                        DavProperty displayNameProp = foundProperties.get(DavPropertyName.DISPLAYNAME);
                        if (displayNameProp != null) {
                            _ownerName = (String)displayNameProp.getValue();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DavException e) {
                e.printStackTrace();
            }
        }
        return _ownerName;
    }

    /**
     * @param <P> the property type
     * @param propertyName a property name
     * @param type the class for the property type returned (HACK!!)
     * @return the value for the specified property name
     * @throws IOException if there is a communication error
     * @throws ObjectStoreException where an unexpected error occurs
     * @throws DavException where an error occurs calling the DAV method
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public final <P> P getProperty(DavPropertyName propertyName, Class<P> type)
        throws IOException, ObjectStoreException, DavException {
        
        DavPropertySet props = properties;
        if (props.get(propertyName) != null) {
            Object value = props.get(propertyName).getValue();
            try {
                if (Collection.class.isAssignableFrom(type)) {
                    P result = type.newInstance();
                    if (value instanceof Collection<?>) {
                        ((Collection<?>) result).addAll((Collection) value);
                    }
                    return result;
                }
                else {
                    Constructor<P> constructor = type.getConstructor(value.getClass());
                    return constructor.newInstance(value);
                }
            }
            catch (SecurityException e) {
                e.printStackTrace();
            }
            catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            catch (InstantiationException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return (P) props.get(propertyName).getValue();
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
        getStore().getClient().execute(deleteMethod);
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
        DavPropertyNameSet principalsProps = CalDavCalendarCollection.propertiesForFetch();
        PropFindMethod getMethod = new PropFindMethod(getPath(), principalsProps, PropFindMethod.DEPTH_0);
        
        getStore().getClient().execute(getMethod);

        if (getMethod.getStatusCode() == DavServletResponse.SC_MULTI_STATUS) {
            return true;
        }
        else if (getMethod.getStatusCode() == DavServletResponse.SC_OK) {
            return true;
        }
        else if (getMethod.getStatusCode() == DavServletResponse.SC_NOT_FOUND) {
            return false;
        }
        else {
            throw new ObjectStoreException(getMethod.getStatusLine().toString());
        }
    }
    
    /**
     * Get the list of collections from a MultiStatus (HTTP 207 status code) response and populate the list of
     * properties of each collection.
     */
    protected static List<CalDavCalendarCollection> collectionsFromResponse(CalDavCalendarStore store,
            MultiStatusResponse[] responses) {
        List<CalDavCalendarCollection> collections = new ArrayList<CalDavCalendarCollection>();

        for (int i = 0; i < responses.length; i++) {
            MultiStatusResponse msResponse = responses[i];
            DavPropertySet foundProperties = msResponse.getProperties(200);
            String collectionUri = msResponse.getHref();

            for (int j = 0; j < responses[i].getStatus().length; j++) {
                DavPropertySet _properties = new DavPropertySet();
                for (DavPropertyIterator iNames = foundProperties.iterator(); iNames.hasNext();) {
                    DavProperty property = iNames.nextProperty();
                    if (property != null) {
                        _properties.add(property);
                    }
                }
                collections.add(new CalDavCalendarCollection(store, collectionUri, _properties));
            }
        }

        return collections;
    }
}

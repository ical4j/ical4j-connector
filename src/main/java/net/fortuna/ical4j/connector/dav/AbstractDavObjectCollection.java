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

import net.fortuna.ical4j.connector.ObjectCollection;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.enums.MediaType;
import net.fortuna.ical4j.connector.dav.enums.ResourceType;
import net.fortuna.ical4j.connector.dav.property.BaseDavPropertyName;
import net.fortuna.ical4j.connector.dav.property.CalDavPropertyName;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.HttpDelete;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.property.*;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    
    private boolean _isReadOnly;

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
    @SuppressWarnings("unchecked")
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
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }

        return resourceTypes.toArray(new ResourceType[resourceTypes.size()]);
    }
    
    /**
     * Returns a list of supported media types. For example, a CalDAV server will probably return 2.0 as the supported
     * version and text/calendar as the content-type.
     */
    @SuppressWarnings("unchecked")
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
            return mediaTypes.toArray(new MediaType[mediaTypes.size()]);
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
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
            return new Long(0);
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
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
            return new Long(0);
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
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
            return ownerHref;
        } catch (ObjectStoreException | IOException | DavException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Name of the owner of this collection. Will be retrieved by the owner href
     */
    public String getOwnerName() {
        if ((_ownerName == null) && (getOwnerHref() != null)) {
            try {
                DavPropertyNameSet nameSet = new DavPropertyNameSet();
                nameSet.add(DavPropertyName.DISPLAYNAME);
                HttpPropfind aGet = new HttpPropfind(getOwnerHref(), nameSet, 0);

                RequestConfig config = RequestConfig.copy(aGet.getConfig()).setAuthenticationEnabled(true).build();
                aGet.setConfig(config);

                HttpResponse httpResponse = getStore().getClient().execute(aGet);

                if (httpResponse.getStatusLine().getStatusCode() == DavServletResponse.SC_MULTI_STATUS) {
                    MultiStatus multiStatus = aGet.getResponseBodyAsMultiStatus(httpResponse);
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
            } catch (IOException | DavException e) {
                throw new RuntimeException(e);
            }
        }
        return _ownerName;
    }

    public boolean isReadOnly() {
        return _isReadOnly;
    }

    public void setReadOnly(boolean isReadOnly) {
        _isReadOnly = isReadOnly;
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
            } catch (IllegalAccessException
                    | InvocationTargetException
                    | InstantiationException
                    | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * @throws HttpResponseException where an error occurs calling the HTTP method
     * @throws IOException if there is a communication error
     * @throws ObjectStoreException where an unexpected error occurs
     */
    public final void delete() throws HttpResponseException, IOException, ObjectStoreException {
        HttpDelete deleteMethod = new HttpDelete(getPath());
        HttpResponse httpResponse = getStore().getClient().execute(deleteMethod);
        if (!deleteMethod.succeeded(httpResponse)) {
            throw new ObjectStoreException(httpResponse.getStatusLine().getStatusCode() + ": "
                    + httpResponse.getStatusLine().getReasonPhrase());
        }
    }

    /**
     * @return true if the collection exists, otherwise false
     * @throws HttpResponseException where an error occurs calling the HTTP method
     * @throws IOException if there is a communication error
     * @throws ObjectStoreException where an unexpected error occurs
     */
    public final boolean exists() throws HttpResponseException, IOException, ObjectStoreException {
        DavPropertyNameSet principalsProps = CalDavCalendarCollection.propertiesForFetch();
        HttpPropfind getMethod = new HttpPropfind(getPath(), principalsProps, 0);
        
        HttpResponse httpResponse = getStore().getClient().execute(getMethod);

        if (httpResponse.getStatusLine().getStatusCode() == DavServletResponse.SC_MULTI_STATUS) {
            return true;
        }
        else if (httpResponse.getStatusLine().getStatusCode() == DavServletResponse.SC_OK) {
            return true;
        }
        else if (httpResponse.getStatusLine().getStatusCode() == DavServletResponse.SC_NOT_FOUND) {
            return false;
        }
        else {
            throw new ObjectStoreException(httpResponse.getStatusLine().toString());
        }
    }
    
    /**
     * Get the list of collections from a MultiStatus (HTTP 207 status code) response and populate the list of
     * properties of each collection.
     */
    protected static List<CalDavCalendarCollection> collectionsFromResponse(CalDavCalendarStore store,
            MultiStatusResponse[] responses) {
        
        /*
         * TODO: supported features can be different on collections than the store, we should
         * check the headers and store the supported features per collection when we fetch them
         */
        
        List<CalDavCalendarCollection> collections = new ArrayList<CalDavCalendarCollection>();

        for (int i = 0; i < responses.length; i++) {
            MultiStatusResponse msResponse = responses[i];
            DavPropertySet foundProperties = msResponse.getProperties(200);
            String collectionUri = msResponse.getHref();

            for (int j = 0; j < responses[i].getStatus().length; j++) {
                if (responses[i].getStatus()[j].getStatusCode() == 200) {
                    boolean isCalendarCollection = false;
                    DavPropertySet _properties = new DavPropertySet();
                    for (DavPropertyIterator iNames = foundProperties.iterator(); iNames.hasNext();) {
                        DavProperty property = iNames.nextProperty();
                        if (property != null) {
                            _properties.add(property);
                            if ((DavConstants.PROPERTY_RESOURCETYPE.equals(property.getName().getName())) && (DavConstants.NAMESPACE.equals(property.getName().getNamespace()))) {
                                Object value = property.getValue();
                                if (value instanceof java.util.ArrayList) {
                                    for (Node child: (java.util.ArrayList<Node>)value) {
                                        if (child instanceof Element) {
                                            String nameNode = child.getLocalName();
                                            if (nameNode != null) {
                                                ResourceType type = ResourceType.findByDescription(nameNode);
                                                if (type != null) {
                                                    if (type.equals(ResourceType.CALENDAR)) {
                                                        isCalendarCollection = true;
                                                    }
                                                    if (type.equals(ResourceType.CALENDAR_PROXY_READ)) {
                                                        isCalendarCollection = true;
                                                    }
                                                    if (type.equals(ResourceType.CALENDAR_PROXY_WRITE)) {
                                                        isCalendarCollection = true;
                                                    }
                                                }
                                            }
                                        }                                
                                    }
                                }
                            }
                        }
                    }
                    if (isCalendarCollection) {
                        collections.add(new CalDavCalendarCollection(store, collectionUri, _properties));
                    }
                }
            }
        }

        return collections;
    }
}

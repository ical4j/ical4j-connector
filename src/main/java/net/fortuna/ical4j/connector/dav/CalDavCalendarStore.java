/**
 * Copyright (c) 2009, Ben Fortuna
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.fortuna.ical4j.connector.CalendarCollection;
import net.fortuna.ical4j.connector.CalendarStore;
import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.xerces.dom.DeferredElementNSImpl;
import org.w3c.dom.Node;

/**
 * $Id$
 *
 * Created on 24/02/2008
 *
 * @author Ben
 *
 */
public class CalDavCalendarStore extends AbstractDavObjectStore<CalDavCalendarCollection> implements CalendarStore<CalDavCalendarCollection> {

    private String prodId;
    
    /**
     * @param host
     * @param port
     */
    public CalDavCalendarStore(String prodId, String host, int port, Protocol protocol, String path) {
    	super(host, port, protocol, path);
        this.prodId = prodId;
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#add(java.lang.String)
     */
    public CalDavCalendarCollection addCollection(String id) throws ObjectStoreException {
        CalDavCalendarCollection collection = new CalDavCalendarCollection(this, id);
        try {
            collection.create();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return collection;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#addCollection(java.lang.String, java.lang.String, java.lang.String, java.lang.String[], net.fortuna.ical4j.model.Calendar)
     */
    public CalDavCalendarCollection addCollection(String id, String displayName,
            String description, String[] supportedComponents, Calendar timezone)
            throws ObjectStoreException {
        
        CalDavCalendarCollection collection = new CalDavCalendarCollection(this, id, displayName, description);
        try {
            collection.create();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return collection;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#get(java.lang.String)
     */
    public CalDavCalendarCollection getCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        CalDavCalendarCollection collection = new CalDavCalendarCollection(this, id);
        try {
            if (collection.exists()) {
                return collection;
            }
        }
        catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        throw new ObjectNotFoundException("Collection with id: [" + id + "] not found");
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#merge(java.lang.String, net.fortuna.ical4j.connector.CalendarCollection)
     */
    public CalendarCollection merge(String id, CalendarCollection calendar) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * This method try to find the calendar-home-set attribute in the user's DAV principals. The calendar-home-set
     * attribute is the URI of the main collection of calendars for the user.
     * @return the URI for the main calendar collection
     * @author Pascal Robert
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws DavException
     */
	private String findCalendarHomeSet() throws ParserConfigurationException, IOException, DavException {
	    /* 
	     * Principals in Zimbra (5.0.x) and Calendar Server are stored in /principal/users/USER/
	     * The calendar-home-set attribute is part of OpenDirectory schema with iCal Server, need a ICalServerCalendarStore class
	     * The principals in Kerio Mail Server are stored in http://server/caldav, , need a KMSCalendarStore class
	     * The principals in CommuniGate Pro are stored in http://server/CalDAV, , need a CGPCalendarStore class
	     */
		String propfindUri = hostConfiguration.getHostURL() + "/principals/users/" + getUserName() + "/";

		DavPropertyNameSet principalsProps = new DavPropertyNameSet();
		principalsProps.add(DavPropertyName.create("calendar-home-set", CalDavConstants.NAMESPACE));
		principalsProps.add(DavPropertyName.DISPLAYNAME);

		PropFindMethod method = new PropFindMethod(propfindUri, principalsProps, PropFindMethod.DEPTH_0);
		httpClient.executeMethod(hostConfiguration,method);
    
	    MultiStatus multiStatus = method.getResponseBodyAsMultiStatus();
	    MultiStatusResponse[] responses = multiStatus.getResponses();
	    for (int i = 0; i < responses.length; i++) {
	    	for (int j = 0; j < responses[i].getStatus().length; j++) {
	    		Status status = responses[i].getStatus()[j];
	    		for (DavPropertyIterator iNames = responses[i].getProperties(status.getStatusCode()).iterator(); iNames.hasNext();) {
	    			DavProperty name = iNames.nextProperty();
	    	
	    			if ((name.getName().getName().equals("calendar-home-set"))  && (CalDavConstants.NAMESPACE.isSame(name.getName().getNamespace().getURI()))) {
		    			if (name.getValue() instanceof ArrayList) {
		    				for (Iterator<?> iter = ((ArrayList<?>)name.getValue()).iterator(); iter.hasNext();) {
		    					Object child = iter.next();
		    					if (child instanceof DeferredElementNSImpl) {
		    						String calendarHomeSetUri = ((DeferredElementNSImpl)child).getTextContent();
		    						/*
		    						 * If the trailing slash is not there, CalendarServer will return a 301 status code
		    						 * and we will get a nice DavException with "Moved Permanently" as the error
		    						 */
		    						if (!(calendarHomeSetUri.endsWith("/"))) {
		    							calendarHomeSetUri += "/"; 
		    						}
		    						return calendarHomeSetUri;
		    					}
		    				}
		    			}
	    			}
	    		}
	    	}
	    }		
	    return null;
	}
	
	/**
	 * This method will try to find all calendar collections available at the 
	 * calendar-home-set URI of the user.
	 * @return An array of all available calendar collections
	 * @author Pascal Robert
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws DavException
	 */
	public List<CalDavCalendarCollection> getCollections() throws ParserConfigurationException, IOException, DavException {
		ArrayList<CalDavCalendarCollection> collections = new ArrayList<CalDavCalendarCollection>();
		
		DavPropertyNameSet principalsProps = new DavPropertyNameSet();
		principalsProps.add(DavPropertyName.DISPLAYNAME);
		principalsProps.add(DavPropertyName.RESOURCETYPE);
		principalsProps.add(DavPropertyName.create("getctag", CalDavConstants.CS_NAMESPACE));
		principalsProps.add(DavPropertyName.create("calendar-description", CalDavConstants.NAMESPACE));
		principalsProps.add(DavPropertyName.create("calendar-color", CalDavConstants.ICAL_NAMESPACE));
		principalsProps.add(DavPropertyName.create("calendar-order", CalDavConstants.ICAL_NAMESPACE));
		principalsProps.add(DavPropertyName.create("calendar-free-busy-set", CalDavConstants.NAMESPACE));

		String urlForcalendarHomeSet = hostConfiguration.getHostURL() + findCalendarHomeSet();
		PropFindMethod method = new PropFindMethod(urlForcalendarHomeSet, principalsProps, PropFindMethod.DEPTH_1);
		httpClient.executeMethod(hostConfiguration,method);
		
	    MultiStatus multiStatus = method.getResponseBodyAsMultiStatus();
	    MultiStatusResponse[] responses = multiStatus.getResponses();
	    for (int i = 0; i < responses.length; i++) {
	    	String collectionUri = responses[i].getHref();
	    	for (int j = 0; j < responses[i].getStatus().length; j++) {
	    		Status status = responses[i].getStatus()[j];
	    		for (DavPropertyIterator iNames = responses[i].getProperties(status.getStatusCode()).iterator(); iNames.hasNext();) {
	    			DavProperty name = iNames.nextProperty();
	    			if (name.getName().getName().equals("resourcetype") && (DavConstants.NAMESPACE.isSame(name.getName().getNamespace().getURI()))) {
	    				if (name.getValue() instanceof ArrayList) {
		    				if (((ArrayList<?>)name.getValue()).size() == 5) {
		    					for (Iterator<?> iter = ((ArrayList<?>)name.getValue()).iterator(); iter.hasNext();) {
		    						Object child = iter.next();
		    						if (child instanceof DeferredElementNSImpl) {
		    							Node node = ((DeferredElementNSImpl)child);
		    							if ((node.getLocalName().equals("calendar")) && (node.getNamespaceURI().equals(CalDavConstants.NAMESPACE.getURI()))) {
		    								collections.add(new CalDavCalendarCollection(this, collectionUri));
		    							}
		    						}
		    					}
		    				}
		    			}
	    			}
	    		}
	    	}
	    }	
		return collections;
	}
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#remove(java.lang.String)
     */
    public CalDavCalendarCollection removeCollection(String id) throws ObjectStoreException, ObjectNotFoundException {
        CalDavCalendarCollection collection = getCollection(id);
        try {
            collection.delete();
        }
        catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return collection;
    }

    /* (non-Javadoc)
     * @see net.fortuna.ical4j.connector.CalendarStore#replace(java.lang.String, net.fortuna.ical4j.connector.CalendarCollection)
     */
//    public CalendarCollection replace(String id, CalendarCollection calendar) {
//        // TODO Auto-generated method stub
//        return null;
//    }

    /**
     * @return the prodId
     */
    final String getProdId() {
        return prodId;
    }

}

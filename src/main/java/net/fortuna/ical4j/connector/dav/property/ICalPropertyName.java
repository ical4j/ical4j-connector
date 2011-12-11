package net.fortuna.ical4j.connector.dav.property;

import net.fortuna.ical4j.connector.dav.CalDavConstants;

import org.apache.jackrabbit.webdav.property.DavPropertyName;

/**
 * This class regroups all properties that are specific to the iCal (OS X client) namespace.
 * 
 * @author probert
 * 
 */
public class ICalPropertyName {

    public static final DavPropertyName CALENDAR_COLOR = DavPropertyName.create(
            CalDavConstants.PROPERTY_CALENDAR_COLOR, CalDavConstants.ICAL_NAMESPACE);

    public static final DavPropertyName CALENDAR_ORDER = DavPropertyName.create(
            CalDavConstants.PROPERTY_CALENDAR_ORDER, CalDavConstants.ICAL_NAMESPACE);

}

package net.fortuna.ical4j.connector.dav.property;

import net.fortuna.ical4j.connector.dav.CalDavConstants;
import net.fortuna.ical4j.connector.dav.DavConstants;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.security.SecurityConstants;

/**
 * Collection of properties related to the DAV namespace
 * 
 * @author probert
 * 
 */
public class BaseDavPropertyName {

    /**
   * 
   */
    public static final DavPropertyName CURRENT_USER_PRIVILEGE_SET = SecurityConstants.CURRENT_USER_PRIVILEGE_SET;

    /**
     * &lt;prop&gt; element
     */
    public static final DavPropertyName PROP = DavPropertyName.create(DavConstants.XML_PROP, DavConstants.NAMESPACE);

    /**
     * Contains the amount of storage counted against the quota on a resource. rfc4331
     */
    public static final DavPropertyName QUOTA_USED_BYTES = DavPropertyName.create(
            CalDavConstants.PROPERTY_QUOTA_USED_BYTES, CalDavConstants.NAMESPACE);

    /**
   * 
   */
    public static final DavPropertyName RESOURCETYPE = DavPropertyName.create(DavConstants.PROPERTY_RESOURCETYPE,
            DavConstants.NAMESPACE);

    /**
     * Indicates the maximum amount of additional storage available to be allocated to a resource rfc4331
     */
    public static final DavPropertyName QUOTA_AVAILABLE_BYTES = DavPropertyName.create(
            CalDavConstants.PROPERTY_QUOTA_AVAILABLE_BYTES, CalDavConstants.NAMESPACE);

}

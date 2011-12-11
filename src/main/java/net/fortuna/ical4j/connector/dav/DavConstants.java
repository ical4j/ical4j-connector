package net.fortuna.ical4j.connector.dav;

public interface DavConstants extends org.apache.jackrabbit.webdav.DavConstants {

    /**
     * Indicates the maximum amount of additional storage available to be allocated to a resource. RFC 4331
     */
    public static final String PROPERTY_QUOTA_AVAILABLE_BYTES = "quota-available-bytes";

    /**
     * Contains the amount of storage counted against the quota on a resource. RFC 4331
     */
    public static final String PROPERTY_QUOTA_USED_BYTES = "quota-used-bytes";

}

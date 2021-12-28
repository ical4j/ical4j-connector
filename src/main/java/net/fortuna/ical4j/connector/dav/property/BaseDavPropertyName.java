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
package net.fortuna.ical4j.connector.dav.property;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.Namespace;

/**
 * Collection of properties related to the DAV namespace
 * 
 * @author probert
 * 
 */
public class BaseDavPropertyName {

    /**
     * Indicates the maximum amount of additional storage available to be allocated to a resource. RFC 4331
     */
    private static final String PROPERTY_QUOTA_AVAILABLE_BYTES = "quota-available-bytes";

    /**
     * Contains the amount of storage counted against the quota on a resource. RFC 4331
     */
    private static final String PROPERTY_QUOTA_USED_BYTES = "quota-used-bytes";

    /**
     * The DAV:resource-id property is a REQUIRED property that enables clients to determine whether two bindings
     * are to the same resource. rfc5842
     */
    private static final String PROPERTY_RESOURCE_ID = "resource-id";

    /**
     * This property identifies the reports that are supported by the resource. RFC 3253
     */
    private static final String PROPERTY_SUPPORTED_REPORT_SET = "supported-report-set";

    /**
     * Contains the value of the synchronization token as it would be returned by a
     * DAV:sync-collection report RFC 6578
     */
    private static final String PROPERTY_SYNC_TOKEN = "sync-token";

    /**
     * DAV:add-member is a protected property (see [RFC4918], Section 15) defined on WebDAV collections,
     * and contains the "Add-Member" URI for that collection. RFC 5995
     */
    private static final String PROPERTY_ADD_MEMBER = "add-member";

    private static final Namespace NAMESPACE = org.apache.jackrabbit.webdav.DavConstants.NAMESPACE;

    /**
     * &lt;prop&gt; element
     */
    public static final DavPropertyName PROP = DavPropertyName.create(DavConstants.XML_PROP, DavConstants.NAMESPACE);

    /**
     * Contains the amount of storage counted against the quota on a resource. rfc4331
     */
    public static final DavPropertyName QUOTA_USED_BYTES = DavPropertyName.create(
            PROPERTY_QUOTA_USED_BYTES, NAMESPACE);

    /**
     * Indicates the maximum amount of additional storage available to be allocated to a resource rfc4331
     */
    public static final DavPropertyName QUOTA_AVAILABLE_BYTES = DavPropertyName.create(
            PROPERTY_QUOTA_AVAILABLE_BYTES, NAMESPACE);

    /**
     * The DAV:resource-id property is a REQUIRED property that enables clients to determine whether two bindings 
     * are to the same resource. rfc5842
     */
    public static final DavPropertyName RESOURCE_ID = DavPropertyName.create(
            PROPERTY_RESOURCE_ID, NAMESPACE);
    
    /**
     * This property identifies the reports that are supported by the resource. RFC 3253
     */
    public static final DavPropertyName SUPPORTED_REPORT_SET = DavPropertyName.create(
            PROPERTY_SUPPORTED_REPORT_SET, NAMESPACE);
    
    /**
     * Contains the value of the synchronization token as it would be returned by a 
     * DAV:sync-collection report RFC 6578
     */    
    public static final DavPropertyName SYNC_TOKEN = DavPropertyName.create(
            PROPERTY_SYNC_TOKEN, NAMESPACE);
    
    /**
     * DAV:add-member is a protected property (see [RFC4918], Section 15) defined on WebDAV collections, 
     * and contains the "Add-Member" URI for that collection. RFC 5995
     */   
    public static final DavPropertyName ADD_MEMBER = DavPropertyName.create(
            PROPERTY_ADD_MEMBER, NAMESPACE);

    public static final DavPropertyName CURRENT_USER_PRINCIPAL = DavPropertyName.create(
            "current-user-principal", NAMESPACE);
}

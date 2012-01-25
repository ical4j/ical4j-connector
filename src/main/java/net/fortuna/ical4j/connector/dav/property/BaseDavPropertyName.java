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

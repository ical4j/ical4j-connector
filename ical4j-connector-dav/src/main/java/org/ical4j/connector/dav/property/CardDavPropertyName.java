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
package org.ical4j.connector.dav.property;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.Namespace;

/**
 * $Id$
 * 
 * Created on 19/11/2008
 * 
 * @author Ben
 * 
 */
public interface CardDavPropertyName {

    /**
     * Identifies the URL of any WebDAV collections that contain address book collections
     * owned by the associated principal resource. rfc6352
     */
    String PROPERTY_ADDRESSBOOK_HOME_SET = "addressbook-home-set";

    /**
     * Specifies what media types are allowed for address object resources in an address
     * book collection. rfc6352
     */
    String PROPERTY_SUPPORTED_ADDRESS_DATA = "supported-address-data";

    /**
     * for carddav
     */
    String PROPERTY_MAX_IMAGE_SIZE = "max-image-size";

    /**
     * Specifies one of the following:
     *
     * 1.  The parts of an address object resource that should be
     *     returned by a given address book REPORT request, and the media
     *     type and version for the returned data; or
     *
     * 2.  The content of an address object resource in a response to an
     *     address book REPORT request.
     *
     * RFC 6352
     */
    String PROPERTY_ADDRESS_DATA = "address-data";

    /**
     *
     */
    String PROPERTY_MAX_RESOURCE_SIZE = "max-resource-size";

    /**
     * CardDAV namespace
     */
    Namespace NAMESPACE = Namespace.getNamespace("C", "urn:ietf:params:xml:ns:carddav");

    ReportType ADDRESSBOOK_QUERY = ReportType.register("addressbook-query", NAMESPACE,
            PrincipalMatchReport.class);
    /**
     * 
     */
    DavPropertyName MAX_RESOURCE_SIZE = DavPropertyName.create(
            PROPERTY_MAX_RESOURCE_SIZE, NAMESPACE);

    /**
     * Purpose: Identifies the URL of any WebDAV collections that contain address book collections owned by the associated
     * principal resource. RFC : rfc6352
     */
    DavPropertyName ADDRESSBOOK_HOME_SET = DavPropertyName.create(PROPERTY_ADDRESSBOOK_HOME_SET,
            NAMESPACE);
    
    /**
     * 
     */
    DavPropertyName SUPPORTED_ADDRESS_DATA = DavPropertyName.create(
            PROPERTY_SUPPORTED_ADDRESS_DATA, NAMESPACE);

    /**
     * 
     */
    DavPropertyName MAX_IMAGE_SIZE = DavPropertyName.create(
            PROPERTY_MAX_IMAGE_SIZE, NAMESPACE);
    
    /**
     * 
     */
    DavPropertyName ADDRESS_DATA = DavPropertyName.create(
            PROPERTY_ADDRESS_DATA, NAMESPACE);

    DavPropertyName ADDRESSBOOK_DESCRIPTION = DavPropertyName.create("addressbook-description", NAMESPACE);
}

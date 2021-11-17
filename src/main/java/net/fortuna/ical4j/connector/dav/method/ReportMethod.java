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
package net.fortuna.ical4j.connector.dav.method;

import net.fortuna.ical4j.connector.dav.CalDavConstants;
import net.fortuna.ical4j.connector.dav.response.ReportResponseHandler;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.vcard.VCard;
import org.apache.http.HttpResponse;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.client.methods.HttpReport;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.w3c.dom.DOMException;

import java.io.IOException;

/**
 * $Id$
 *
 * Created on: 07/01/2009
 *
 * @author Ben
 *
 * @deprecated use {@link net.fortuna.ical4j.connector.dav.response.ReportResponseHandler}
 */
@Deprecated
public class ReportMethod extends HttpReport {

    /**
     * 
     */
    public static final ReportType CALENDAR_QUERY = ReportType.register("calendar-query", CalDavConstants.CALDAV_NAMESPACE,
            PrincipalMatchReport.class);
    public static final ReportType FREEBUSY_QUERY = ReportType.register("free-busy-query", CalDavConstants.CALDAV_NAMESPACE,
            PrincipalMatchReport.class);
    public static final ReportType ADDRESSBOOK_QUERY = ReportType.register("addressbook-query", CalDavConstants.CARDDAV_NAMESPACE,
            PrincipalMatchReport.class);

    /**
     * @param uri a calendar collection URI
     * @param reportInfo report configuration
     * @throws IOException where communication fails
     */
    public ReportMethod(String uri, ReportInfo reportInfo) throws IOException {
        super(uri, reportInfo);
    }

    /**
     * @return an array of calendar objects
     * @throws IOException where communication fails
     * @throws DavException where the DAV method fails
     * @throws DOMException where XML parsing fails
     * @throws ParserException where calendar parsing fails
     */
    public Calendar[] getCalendars(HttpResponse httpResponse) throws IOException, DavException, DOMException, ParserException {
        ReportResponseHandler responseHandler = new ReportResponseHandler(this);
        responseHandler.accept(httpResponse);
        return responseHandler.getCalendars();
    }
    
    public VCard[] getVCards(HttpResponse httpResponse) throws IOException, DavException, DOMException {
        ReportResponseHandler responseHandler = new ReportResponseHandler(this);
        responseHandler.accept(httpResponse);
        return responseHandler.getVCards();
    }
}

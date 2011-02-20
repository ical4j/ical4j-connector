/**
 * Copyright (c) 2011, Ben Fortuna
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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.connector.dav.CalDavConstants;
import net.fortuna.ical4j.connector.dav.property.CalDavPropertyName;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.w3c.dom.DOMException;

/**
 * $Id$
 *
 * Created on: 07/01/2009
 *
 * @author Ben
 *
 */
public class ReportMethod extends org.apache.jackrabbit.webdav.client.methods.ReportMethod {

    /**
     * 
     */
    public static final ReportType CALENDAR_QUERY = ReportType.register("calendar-query",
            CalDavConstants.NAMESPACE, PrincipalMatchReport.class);
    
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
    public Calendar[] getCalendars() throws IOException, DavException, DOMException, ParserException {
        List<Calendar> calendars = new ArrayList<Calendar>();
        MultiStatus multi = getResponseBodyAsMultiStatus();
        for (MultiStatusResponse response : multi.getResponses()) {
            DavPropertySet props = response.getProperties(200);
            if (props.get(CalDavPropertyName.CALENDAR_DATA) != null) {
                String value = (String) props.get(CalDavPropertyName.CALENDAR_DATA).getValue();
                CalendarBuilder builder = new CalendarBuilder();
                calendars.add(builder.build(new StringReader(value)));
            }
        }
        return calendars.toArray(new Calendar[calendars.size()]);
    }
}

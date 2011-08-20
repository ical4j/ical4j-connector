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

package net.fortuna.ical4j.connector.dav;

import java.io.IOException;
import java.io.StringReader;

import net.fortuna.ical4j.connector.dav.property.CalDavPropertyName;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * 
 * 
 * @author probert
 * 
 *         Created on: 14 avr. 2009
 * 
 *         $Id$
 */
public class ScheduleResponse {

    /*
     * http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-06#section-11.3
     */
    private String recipient;
    /*
     * http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-06#section-3.5.4
     */
    private float requestStatusCode;
    private String requestStatusMessage;
    /*
     * http://tools.ietf.org/html/rfc4791#section-9.6
     */
    private Calendar calendarData;

    /*
     * <schedule-response xmlns='urn:ietf:params:xml:ns:caldav'> <response> <recipient> <href
     * xmlns='DAV:'>mailto:probert@macti.ca</href> </recipient> <request-status>3.7;Invalid Calendar
     * User</request-status> <error xmlns='DAV:'> <recipient-exists xmlns='urn:ietf:params:xml:ns:caldav'/> </error>
     * </response> <response> <recipient> <href xmlns='DAV:'>urn:uuid:admin</href> </recipient>
     * <request-status>2.0;Success</request-status> <calendar-data><![CDATA[BEGIN:VCALENDAR ... END:VCALENDAR
     * ]]></calendar-data> <responsedescription xmlns='DAV:'>OK</responsedescription> </response> </schedule-response>
     */
    public ScheduleResponse(Element responseNode) throws IOException, ParserException {
        NodeList recipients = responseNode.getElementsByTagNameNS(CalDavConstants.NAMESPACE.getURI(),
                CalDavPropertyName.RECIPIENT.getName());
        NodeList status = responseNode.getElementsByTagNameNS(CalDavConstants.NAMESPACE.getURI(),
                CalDavPropertyName.REQUEST_STATUS.getName());
        NodeList calendars = responseNode.getElementsByTagNameNS(CalDavConstants.NAMESPACE.getURI(),
                CalDavPropertyName.CALENDAR_DATA.getName());

        for (int nodesIndex = 0; nodesIndex < calendars.getLength(); nodesIndex++) {
            Element node = (Element) calendars.item(nodesIndex);
            if (node.getFirstChild() != null) {
                CalendarBuilder builder = new CalendarBuilder();
                if (node.getFirstChild() instanceof CDATASection) {
                    CDATASection calData = (CDATASection) node.getFirstChild();
                    StringReader sin = new StringReader(calData.getData());
                    this.calendarData = builder.build(sin);
                }
                // KMS don't return CDATA, go figure
                if (node.getFirstChild() instanceof Text) {
                    StringReader sin = new StringReader(((Text) node.getFirstChild()).getTextContent());
                    this.calendarData = builder.build(sin);
                }
            }
        }

        for (int nodesIndex = 0; nodesIndex < status.getLength(); nodesIndex++) {
            Element node = (Element) status.item(nodesIndex);
            String fullStatus = ((Text) node.getFirstChild()).getTextContent();
            String[] split = fullStatus.split(";");
            if (split.length == 2) {
                this.requestStatusCode = new Float(split[0]).floatValue();
                this.requestStatusMessage = split[1];
            }
        }

        for (int nodesIndex = 0; nodesIndex < recipients.getLength(); nodesIndex++) {
            Element node = (Element) recipients.item(nodesIndex);
            NodeList childs = node.getElementsByTagNameNS(DavConstants.NAMESPACE.getURI(), DavPropertyName.XML_HREF);
            if ((childs != null) && (childs.item(0) != null) && (childs.item(0).getFirstChild() != null)) {
                this.recipient = childs.item(0).getFirstChild().getTextContent();
            }
        }
    }

    /**
     * @return the recipient
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * @param recipient
     *            the recipient to set
     */
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /**
     * @return the requestStatusCode
     */
    public float getRequestStatusCode() {
        return requestStatusCode;
    }

    /**
     * @param requestStatusCode
     *            the requestStatusCode to set
     */
    public void setRequestStatusCode(float requestStatusCode) {
        this.requestStatusCode = requestStatusCode;
    }

    /**
     * @return the requestStatusMessage
     */
    public String getRequestStatusMessage() {
        return requestStatusMessage;
    }

    /**
     * @param requestStatusMessage
     *            the requestStatusMessage to set
     */
    public void setRequestStatusMessage(String requestStatusMessage) {
        this.requestStatusMessage = requestStatusMessage;
    }

    /**
     * @return the calendarData
     */
    public Calendar getCalendarData() {
        return calendarData;
    }

    /**
     * @param calendarData
     *            the calendarData to set
     */
    public void setCalendarData(Calendar calendarData) {
        this.calendarData = calendarData;
    }

    public boolean isSuccess() {
        if (requestStatusCode < 3.0) {
            return true;
        }
        return false;
    }

}

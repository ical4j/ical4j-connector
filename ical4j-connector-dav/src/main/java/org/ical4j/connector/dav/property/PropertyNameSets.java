package org.ical4j.connector.dav.property;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.security.SecurityConstants;

import static org.apache.jackrabbit.webdav.property.DavPropertyName.DISPLAYNAME;

public abstract class PropertyNameSets {
    
    public static final DavPropertyNameSet REPORT_CALENDAR = new DavPropertyNameSet();
    
    public static final DavPropertyNameSet PROPFIND_CALENDAR = new DavPropertyNameSet();
    
    public static final DavPropertyNameSet PROPFIND_CALENDAR_HOME = new DavPropertyNameSet();
    
    public static final DavPropertyNameSet PROPFIND_CARD = new DavPropertyNameSet();

    public static final DavPropertyNameSet PROPFIND_CARD_HOME = new DavPropertyNameSet();

    public static final DavPropertyNameSet REPORT_CARD = new DavPropertyNameSet();

    public static final DavPropertyNameSet PROPFIND_SUPPORTED_FEATURES = new DavPropertyNameSet();

    static {
        REPORT_CALENDAR.add(DavPropertyName.GETETAG);
        REPORT_CALENDAR.add(CalDavPropertyName.CALENDAR_DATA);

        PROPFIND_CALENDAR.add(BaseDavPropertyName.QUOTA_AVAILABLE_BYTES);
        PROPFIND_CALENDAR.add(BaseDavPropertyName.QUOTA_USED_BYTES);
        PROPFIND_CALENDAR.add(SecurityConstants.CURRENT_USER_PRIVILEGE_SET);
        PROPFIND_CALENDAR.add(BaseDavPropertyName.PROP);
        PROPFIND_CALENDAR.add(DavPropertyName.RESOURCETYPE);
        PROPFIND_CALENDAR.add(DISPLAYNAME);
        PROPFIND_CALENDAR.add(SecurityConstants.OWNER);

        PROPFIND_CALENDAR.add(CalDavPropertyName.CALENDAR_DESCRIPTION);
        PROPFIND_CALENDAR.add(CalDavPropertyName.SUPPORTED_CALENDAR_COMPONENT_SET);
        PROPFIND_CALENDAR.add(CalDavPropertyName.FREE_BUSY_SET);
        PROPFIND_CALENDAR.add(CalDavPropertyName.SCHEDULE_CALENDAR_TRANSP);
        PROPFIND_CALENDAR.add(CalDavPropertyName.SCHEDULE_DEFAULT_CALENDAR_URL);
        PROPFIND_CALENDAR.add(CalDavPropertyName.CALENDAR_TIMEZONE);
        PROPFIND_CALENDAR.add(CalDavPropertyName.SUPPORTED_CALENDAR_DATA);
        PROPFIND_CALENDAR.add(CalDavPropertyName.MAX_ATTENDEES_PER_INSTANCE);
        PROPFIND_CALENDAR.add(CalDavPropertyName.MAX_DATE_TIME);
        PROPFIND_CALENDAR.add(CalDavPropertyName.MIN_DATE_TIME);
        PROPFIND_CALENDAR.add(CalDavPropertyName.MAX_INSTANCES);
        PROPFIND_CALENDAR.add(CalDavPropertyName.MAX_RESOURCE_SIZE);

        PROPFIND_CALENDAR.add(CSDavPropertyName.XMPP_SERVER);
        PROPFIND_CALENDAR.add(CSDavPropertyName.XMPP_URI);
        PROPFIND_CALENDAR.add(CSDavPropertyName.CTAG);
        PROPFIND_CALENDAR.add(CSDavPropertyName.SOURCE);
        PROPFIND_CALENDAR.add(CSDavPropertyName.SUBSCRIBED_STRIP_ALARMS);
        PROPFIND_CALENDAR.add(CSDavPropertyName.SUBSCRIBED_STRIP_ATTACHMENTS);
        PROPFIND_CALENDAR.add(CSDavPropertyName.SUBSCRIBED_STRIP_TODOS);
        PROPFIND_CALENDAR.add(CSDavPropertyName.REFRESHRATE);
        PROPFIND_CALENDAR.add(CSDavPropertyName.PUSH_TRANSPORTS);
        PROPFIND_CALENDAR.add(CSDavPropertyName.PUSHKEY);

        PROPFIND_CALENDAR.add(ICalPropertyName.CALENDAR_COLOR);
        PROPFIND_CALENDAR.add(ICalPropertyName.CALENDAR_ORDER);

        PROPFIND_CALENDAR_HOME.add(CalDavPropertyName.CALENDAR_HOME_SET);
        PROPFIND_CALENDAR_HOME.add(DavPropertyName.DISPLAYNAME);

        
                /*
         * TODO : to add the following properties
            <C:me-card xmlns:C="http://calendarserver.org/ns/" /> 
            <C:push-transports xmlns:C="http://calendarserver.org/ns/" /> 
            <C:pushkey xmlns:C="http://calendarserver.org/ns/" /> 
            <D:bulk-requests xmlns:D="http://me.com/_namespace/" /> 
       */

        PROPFIND_CARD.add(DISPLAYNAME);


        PROPFIND_CARD.add(SecurityConstants.CURRENT_USER_PRIVILEGE_SET);
        PROPFIND_CARD.add(DavPropertyName.RESOURCETYPE);
        PROPFIND_CARD.add(SecurityConstants.OWNER);
        PROPFIND_CARD.add(CardDavPropertyName.MAX_RESOURCE_SIZE);
        PROPFIND_CARD.add(BaseDavPropertyName.RESOURCE_ID);
        PROPFIND_CARD.add(BaseDavPropertyName.SUPPORTED_REPORT_SET);
        PROPFIND_CARD.add(BaseDavPropertyName.SYNC_TOKEN);
        PROPFIND_CARD.add(BaseDavPropertyName.ADD_MEMBER);
        PROPFIND_CARD.add(CardDavPropertyName.MAX_IMAGE_SIZE);

        /**
         * FIXME jackrabbit generates an error when quota-used-bytes is sent.
         * I suspect the problem is that the response have this attribute: e:dt="int"
         */
        //PROPFIND_CARD.add(BaseDavPropertyName.QUOTA_USED_BYTES);
        //PROPFIND_CARD.add(BaseDavPropertyName.QUOTA_AVAILABLE_BYTES);

        /* In the absence of this property, the server MUST only accept data with the media type
         * "text/vcard" and vCard version 3.0, and clients can assume that is
         * all the server will accept.
         */
        PROPFIND_CARD.add(CardDavPropertyName.SUPPORTED_ADDRESS_DATA);

        REPORT_CARD.add(DavPropertyName.GETETAG);
        REPORT_CARD.add(CardDavPropertyName.ADDRESS_DATA);

        PROPFIND_CARD_HOME.add(CardDavPropertyName.ADDRESSBOOK_HOME_SET);
        PROPFIND_CARD_HOME.add(DavPropertyName.DISPLAYNAME);

        PROPFIND_SUPPORTED_FEATURES.add(DavPropertyName.RESOURCETYPE);
        PROPFIND_SUPPORTED_FEATURES.add(CSDavPropertyName.CTAG);
        var owner = DavPropertyName.create(DavPropertyName.XML_OWNER, DavConstants.NAMESPACE);
        PROPFIND_SUPPORTED_FEATURES.add(owner);

    }
}

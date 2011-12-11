package net.fortuna.ical4j.connector.dav.enums;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public enum SupportedFeature {

  ACCESS_CONTROL("access-control"),
  CALENDAR_ACCESS("calendar-access"),
  CALENDAR_SCHEDULE("calendar-schedule"),
  CALENDAR_AUTO_SCHEDULE("calendar-auto-schedule"),
  CALENDAR_AVAILABILITY("calendar-availability"),
  INBOX_AVAILABILITY("inbox-availability"),
  CALENDAR_PROXY("calendar-proxy"),
  CALENDARSERVER_PRIVATE_EVENTS("calendarserver-private-events"),
  CALENDARSERVER_PRIVATE_COMMENTS("calendarserver-private-comments"),
  CALENDARSERVER_SHARING("calendarserver-sharing"),
  CALENDARSERVER_SHARING_NO_SCHEDULING("calendarserver-sharing-no-scheduling"),
  CALENDAR_QUERY_EXTENDED("calendar-query-extended"),
  CALENDAR_DEFAULT_ALARMS("calendar-default-alarms"),
  ADDRESSBOOK("addressbook"),
  EXTENDED_MKCOL("extended-mkcol"),
  CALENDARSERVER_PRINCIPAL_PROPERTY_SEARCH("calendarserver-principal-property-search");

    private String description;

    private static Set<String> index = new HashSet<String>();

    static {
        for (SupportedFeature supportedFeature : SupportedFeature.values()) {
            index.add(supportedFeature.description());
        }
    }

    private SupportedFeature(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    public static ArrayList<String> descriptions() {
        return new ArrayList<String>(index);
    }

    public static SupportedFeature findByDescription(String value) {
        for (SupportedFeature feature : values()) {
            if (feature.description().equals(value)) {
                return feature;
            }
        }
        return null;
    }

}
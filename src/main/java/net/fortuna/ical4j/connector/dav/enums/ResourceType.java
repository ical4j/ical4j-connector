package net.fortuna.ical4j.connector.dav.enums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public enum ResourceType {

  CALENDAR("calendar"),
  FREE_BUSY_URL("free-busy-url"),
  SCHEDULE_OUTBOX("schedule-outbox"),
  NOTIFICATION("notification"),
  DROPBOX_HOME("dropbox-home"),
  CALENDAR_PROXY_WRITE("calendar-proxy-write"),
  CALENDAR_PROXY_READ("calendar-proxy-read"),
  SHARE_OWNER("shared-owner"),
  ADRESSBOOK("addressbook"),
  SUBSCRIBED("subscribed"),
  COLLECTION("collection"),
  SCHEDULE_INBOX("schedule-inbox");
  
    private String description;

    private static Set<String> index = new HashSet<String>();

    static {
        for (ResourceType supportedFeature : ResourceType.values()) {
            index.add(supportedFeature.description());
        }
    }

    private ResourceType(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    public static ArrayList<String> descriptions() {
        return new ArrayList<String>(index);
    }

    public static ResourceType findByDescription(String value) {
        for (ResourceType feature : values()) {
            if (feature.description().equals(value)) {
                return feature;
            }
        }
        return null;
    }

}
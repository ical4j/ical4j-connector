module ical4j.connector.api {
    requires java.base;

    requires transitive ical4j.core;
    requires transitive ical4j.vcard;

    requires org.slf4j;
    requires org.apache.commons.lang3;

    exports org.ical4j.connector;
    exports org.ical4j.connector.local;
    exports org.ical4j.connector.event;
}
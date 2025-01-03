module ical4j.connector.dav {
    requires java.base;
    requires java.xml;

    requires transitive ical4j.connector.api;

    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires jackrabbit.webdav;

    requires transitive org.slf4j;
    requires static transitive org.jetbrains.annotations;
    requires static org.hamcrest;
    requires org.apache.commons.io;

    exports org.ical4j.connector.dav;
    exports org.ical4j.connector.dav.method;
    exports org.ical4j.connector.dav.property;
    exports org.ical4j.connector.dav.request;
    exports org.ical4j.connector.dav.response;
}

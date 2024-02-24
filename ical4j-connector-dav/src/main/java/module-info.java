module ical4j.connector.dav {
    requires java.base;
    requires java.xml;

    requires ical4j.connector.api;

    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires jackrabbit.webdav;

    requires org.slf4j;
    requires static org.jetbrains.annotations;
    requires static org.hamcrest;

    exports org.ical4j.connector.dav;
    exports org.ical4j.connector.dav.method;
    exports org.ical4j.connector.dav.property;
    exports org.ical4j.connector.dav.request;
    exports org.ical4j.connector.dav.response;
}

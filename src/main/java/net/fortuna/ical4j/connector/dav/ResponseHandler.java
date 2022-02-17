package net.fortuna.ical4j.connector.dav;

import org.apache.http.HttpResponse;

import java.util.function.Consumer;

public interface ResponseHandler extends Consumer<HttpResponse> {
}

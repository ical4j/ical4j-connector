package org.ical4j.connector.api.controller;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;
import org.ical4j.connector.api.ICalendarResponseVariants;

import java.util.function.Supplier;

public abstract class AbstractController implements ICalendarResponseVariants {

    protected Response response(Supplier<Object> result, Request request) {
        Variant variant = request.selectVariant(VARIANT_LIST);
        if (variant != null) {
            if (variant.getMediaType().isCompatible(APPLICATION_ICALENDAR_TYPE)) {
                return Response.ok(result.get(), APPLICATION_ICALENDAR_TYPE).build();
            } else if (variant.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                return Response.ok(result.get(), MediaType.APPLICATION_JSON_TYPE).build();
            } else if (variant.getMediaType().isCompatible(MediaType.APPLICATION_XML_TYPE)) {
                return Response.ok(result.get(), MediaType.APPLICATION_XML_TYPE).build();
            }
        }
        return Response.notAcceptable(VARIANT_LIST).build();
    }
}

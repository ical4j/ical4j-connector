package org.ical4j.connector.api;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Variant;

import java.util.List;

public interface ICalendarResponseVariants {

    MediaType APPLICATION_ICALENDAR_TYPE = MediaType.valueOf("application/icalendar");

    List<Variant> VARIANT_LIST = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE,
            MediaType.APPLICATION_XML_TYPE,
            APPLICATION_ICALENDAR_TYPE).build();
}

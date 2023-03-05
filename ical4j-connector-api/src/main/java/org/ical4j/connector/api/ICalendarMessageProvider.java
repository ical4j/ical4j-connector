package org.ical4j.connector.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

@Produces({"application/icalendar"})
@Consumes({"application/icalendar"})
//@Singleton
@Provider
public class ICalendarMessageProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == Calendar.class || List.class.isAssignableFrom(type);
    }

    @Override
    public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                             MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {

        if (type.isAssignableFrom(Calendar.class)) {
            CalendarBuilder builder = new CalendarBuilder();
            try {
                return (T) builder.build(entityStream);
            } catch (ParserException e) {
                throw new WebApplicationException(e);
            }
        }
        throw new IllegalArgumentException("Unsupported type");
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == Calendar.class || List.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(T s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {

        CalendarOutputter outputter = new CalendarOutputter();
        if (type.isAssignableFrom(Calendar.class)) {
            outputter.output((Calendar) s, entityStream);
        } else if (List.class.isAssignableFrom(type)) {
            for (Calendar c : (List<Calendar>) s) {
                outputter.output(c, entityStream);
            }
        }
    }
}

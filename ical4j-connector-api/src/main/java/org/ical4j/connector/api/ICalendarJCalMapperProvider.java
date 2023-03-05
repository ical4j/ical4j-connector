package org.ical4j.connector.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import net.fortuna.ical4j.model.Calendar;
import org.mnode.ical4j.serializer.JCalSerializer;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ICalendarJCalMapperProvider implements ContextResolver<ObjectMapper> {

    private ObjectMapper mapper;

    public ICalendarJCalMapperProvider() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Calendar.class, new JCalSerializer(null));

        mapper = new ObjectMapper();
        mapper.registerModule(module);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}

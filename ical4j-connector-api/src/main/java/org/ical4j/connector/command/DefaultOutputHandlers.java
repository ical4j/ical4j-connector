package org.ical4j.connector.command;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import net.fortuna.ical4j.model.Calendar;
import org.mnode.ical4j.serializer.JCalSerializer;
import org.mnode.ical4j.serializer.XCalSerializer;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public interface DefaultOutputHandlers {

    static <T> Consumer<T> STDOUT_PRINTER() {
        return System.out::println;
    }

    static <T extends List<?>> Consumer<T> STDOUT_LIST_PRINTER() {
        return (t) -> {
            t.forEach(System.out::println);
        };
    }

    static <T> Consumer<T> STDOUT_JCAL_PRINTER() {
        return (t) -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Calendar.class, new JCalSerializer(null));

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(module);

            try {
                mapper.writeValue(System.out, t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    static <T> Consumer<T> STDOUT_XCAL_PRINTER() {
        return (t) -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Calendar.class, new XCalSerializer(null));

            XmlMapper mapper = XmlMapper.builder().defaultUseWrapper(true).build();
            mapper.setConfig(mapper.getSerializationConfig().withRootName(
                            PropertyName.construct("icalendar", "urn:ietf:params:xml:ns:icalendar-2.0"))
                    .with(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME));

            mapper.registerModule(module);

            try {
                mapper.writeValue(System.out, t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}

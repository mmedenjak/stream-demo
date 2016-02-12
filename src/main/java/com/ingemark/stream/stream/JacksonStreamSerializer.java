package com.ingemark.stream.stream;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.stream.Stream;

public class JacksonStreamSerializer extends AsArraySerializerBase<Stream<?>> {
    public JacksonStreamSerializer() {
        this(TypeFactory.unknownType(), false, null, null);
    }

    public JacksonStreamSerializer(
            JavaType elementType, boolean staticTyping, TypeSerializer vts, BeanProperty property) {
        super(Stream.class, elementType, staticTyping, vts, property, null);
    }

    public JacksonStreamSerializer(
            JacksonStreamSerializer src, BeanProperty property, TypeSerializer vts, JsonSerializer<?> elementSerializer) {
        super(src, property, vts, elementSerializer);
    }


    private static <T> T sneakyThrow(Throwable e) {
        return JacksonStreamSerializer.<RuntimeException, T>sneakyThrow0(e);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable, T> T sneakyThrow0(Throwable t) throws E {
        throw (E) t;
    }

    @Override
    public boolean hasSingleElement(Stream<?> value) {
        return false;
    }

    @Override
    protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
        return new JacksonStreamSerializer(_elementType, _staticTyping, vts, _property);
    }

    @Override
    public JacksonStreamSerializer withResolved(
            BeanProperty property, TypeSerializer vts, JsonSerializer<?> elementSerializer, Boolean unwrapSingle) {
        return new JacksonStreamSerializer(this, property, vts, elementSerializer);
    }

    @Override
    protected void serializeContents(Stream<?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        final TypeSerializer typeSer = _valueTypeSerializer;
        final PrevStateHolder prev = new PrevStateHolder();
        value.forEachOrdered(elem -> {
            try {
                if (elem == null) {
                    provider.defaultSerializeNull(jgen);
                    return;
                }
                JsonSerializer<Object> currSerializer = _elementSerializer;
                if (currSerializer == null) {
                    final Class<?> cc = elem.getClass();
                    if (cc == prev.cc) {
                        currSerializer = prev.serializer;
                    } else {
                        currSerializer = provider.findValueSerializer(cc, _property);
                        prev.serializer = currSerializer;
                        prev.cc = cc;
                    }
                }
                if (typeSer == null) {
                    currSerializer.serialize(elem, jgen, provider);
                } else {
                    currSerializer.serializeWithType(elem, jgen, provider, typeSer);
                }
                jgen.writeRaw('\n');
            } catch (IOException e) {
                // achieve exception transparency by propagating checked exceptions
                // over the lambda invocation boundary. Note that the enclosing method
                // safely declares the same checked exception(s).
                sneakyThrow(e);
            }
        });
    }

    private static class PrevStateHolder {
        JsonSerializer<Object> serializer;
        Class<?> cc;
    }
}

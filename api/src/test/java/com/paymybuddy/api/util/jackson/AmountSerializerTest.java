package com.paymybuddy.api.util.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AmountSerializerTest {
    @Test
    void serialize() throws IOException {
        Writer jsonWriter = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(jsonWriter);
        SerializerProvider provider = new ObjectMapper().getSerializerProvider();
        AmountSerializer serializer = new AmountSerializer();
        serializer.serialize(new BigDecimal("012.34000"), gen, provider);
        gen.flush();
        assertEquals("\"12.34\"", jsonWriter.toString());
    }

    @Test
    void testToString() {
        assertEquals("12.34001", AmountSerializer.toString(new BigDecimal("012.34001")));
        assertEquals("12.34", AmountSerializer.toString(new BigDecimal("012.34000")));
        assertNull(AmountSerializer.toString(null));
    }
}
package com.paymybuddy.api.util.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Jackson serializer which encode BigDecimal as JSON string.
 * This ensures that all systems will interpret these values the same way, without omitting decimals, etc (specifically
 * for extra-large or extra-small values).
 */
public class AmountSerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal amount, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(toString(amount));
    }

    public static String toString(BigDecimal amount) {
        return amount == null ? null : amount.stripTrailingZeros().toPlainString();
    }
}

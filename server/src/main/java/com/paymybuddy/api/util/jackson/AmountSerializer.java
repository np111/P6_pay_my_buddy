package com.paymybuddy.api.util.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.math.BigDecimal;

public class AmountSerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal amount, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (amount == null) {
            gen.writeNull();
        } else {
            gen.writeString(amount.stripTrailingZeros().toPlainString());
        }
    }
}

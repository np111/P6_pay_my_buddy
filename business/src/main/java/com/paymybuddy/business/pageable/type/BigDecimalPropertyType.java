package com.paymybuddy.business.pageable.type;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class BigDecimalPropertyType implements PropertyType<BigDecimal> {
    @Override
    public byte[] serialize(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString().getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public BigDecimal deserialize(byte[] bytes) {
        return new BigDecimal(new String(bytes, StandardCharsets.US_ASCII));
    }
}

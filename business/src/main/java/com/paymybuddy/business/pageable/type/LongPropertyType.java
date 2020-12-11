package com.paymybuddy.business.pageable.type;

import com.google.common.primitives.Longs;

public class LongPropertyType implements PropertyType<Long> {
    @Override
    public byte[] serialize(Long value) {
        return Longs.toByteArray(value);
    }

    @Override
    public Long deserialize(byte[] bytes) {
        return Longs.fromByteArray(bytes);
    }
}

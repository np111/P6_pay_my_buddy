package com.paymybuddy.business.pageable.type;

public interface PropertyType<T> {
    byte[] serialize(T value);

    T deserialize(byte[] str);
}

package com.paymybuddy.api.model.collection;

import java.util.Collection;

public interface CollectionResponse<T> {
    Collection<T> getRecords();
}

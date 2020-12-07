package com.paymybuddy.api.model.collection;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class IteratorResponse<T> implements CollectionResponse<T> {
    private String prevCursor;
    private String nextCursor;
    private List<T> records;
}

package com.paymybuddy.api.model.collection;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class CursorResponse<T> implements CollectionResponse<T> {
    private String prevCursor;
    private Boolean hasPrev;
    private String nextCursor;
    private Boolean hasNext;
    @Singular("record")
    private List<T> records;
}

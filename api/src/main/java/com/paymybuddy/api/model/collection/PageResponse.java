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
public class PageResponse<T> implements CollectionResponse<T> {
    private Integer page;
    private Integer pageSize;
    private Integer pageCount;
    private Integer totalCount;
    @Singular("record")
    private List<T> records;
}

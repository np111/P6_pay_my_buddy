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
public class PageableResponse<T> implements CollectionResponse<T> {
    private Integer page;
    private Integer perPage;
    private Integer pageCount;
    private Integer totalCount;
    private List<T> records;
}

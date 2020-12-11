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
public class ListResponse<T> implements CollectionResponse<T> {
    public static <T> ListResponse<T> of(List<T> records) {
        return new ListResponse<>(records);
    }

    private List<T> records;
}

package com.paymybuddy.business.pageable;

import com.google.common.base.Preconditions;
import com.paymybuddy.api.model.collection.PageResponse;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Accessors(fluent = true)
public class PageFetcher<T, E> {
    public static <T, E> PageFetcher<T, E> create() {
        return new PageFetcher<>();
    }

    private Function<Pageable, Page<E>> recordsQuery;
    private Function<E, T> recordMapper;
    private Function<String, String> sortPropertyTransformer;

    public PageResponse<T> fetch(com.paymybuddy.business.pageable.PageRequest request) {
        Preconditions.checkNotNull(recordsQuery, "recordsQuery cannot be null");
        Preconditions.checkNotNull(recordMapper, "recordMapper cannot be null");

        int page = request.getPage();
        int pageSize = request.getPageSize();
        Sort sort = Sort.by(PageableUtil.parseSortInstructions(request.getPageSort(), sortPropertyTransformer));

        Page<E> entities = recordsQuery.apply(PageRequest.of(page, pageSize, sort));
        int totalCount = Math.toIntExact(entities.getTotalElements());
        List<T> records = entities.stream().map(recordMapper).collect(Collectors.toList());
        return PageResponse.<T>builder()
                .page(page)
                .pageSize(pageSize)
                .pageCount((totalCount + (pageSize - 1)) / pageSize)
                .totalCount(totalCount)
                .records(records)
                .build();
    }
}

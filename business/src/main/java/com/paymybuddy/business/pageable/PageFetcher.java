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

/**
 * An utility to retrieve paginated results, using an OFFSET/LIMIT logic.
 *
 * @param <Model>  model
 * @param <Entity> database entity
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Accessors(fluent = true)
public class PageFetcher<Model, Entity> {
    /**
     * Create a new {@link PageFetcher} instance.
     * <p>
     * You must now set {@link #recordsQuery(Function)} and {@link #recordMapper(Function)} before calling
     * {@link #fetch(com.paymybuddy.business.pageable.PageRequest)}.
     */
    public static <Model, Entity> PageFetcher<Model, Entity> create() {
        return new PageFetcher<>();
    }

    /**
     * Query function, to retrieve {@linkplain #<Entity> entities} records.
     */
    private Function<Pageable, Page<Entity>> recordsQuery;

    /**
     * Mapping function, to transform the fetched {@linkplain #<Entity> entities} records to {@linkplain #<Model> models}.
     */
    private Function<Entity, Model> recordMapper;

    private Function<String, String> sortPropertyTransformer;

    /**
     * Perform a request and returns paginated results.
     *
     * @param request request parameters
     * @return the paginated results
     */
    public PageResponse<Model> fetch(com.paymybuddy.business.pageable.PageRequest request) {
        Preconditions.checkNotNull(recordsQuery, "recordsQuery cannot be null");
        Preconditions.checkNotNull(recordMapper, "recordMapper cannot be null");

        int page = request.getPage();
        int pageSize = request.getPageSize();
        Sort sort = Sort.by(PageableUtil.parseSortInstructions(request.getPageSort(), sortPropertyTransformer));

        Page<Entity> entities = recordsQuery.apply(PageRequest.of(page, pageSize, sort));
        int totalCount = Math.toIntExact(entities.getTotalElements());
        List<Model> records = entities.stream().map(recordMapper).collect(Collectors.toList());
        return PageResponse.<Model>builder()
                .page(page)
                .pageSize(pageSize)
                .pageCount((totalCount + (pageSize - 1)) / pageSize)
                .totalCount(totalCount)
                .records(records)
                .build();
    }
}

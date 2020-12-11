package com.paymybuddy.business.fetcher;

import com.google.common.base.Preconditions;
import com.paymybuddy.api.model.collection.PageResponse;
import com.paymybuddy.business.util.PageableUtil;
import com.paymybuddy.business.util.ValidationUtil;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
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

    public PageResponse<T> fetch(Request request) {
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

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = false)
    public static class Request extends AbstractRequest {
        private int page;
    }

    public static class RequestParser extends AbstractRequestParser<Request> {
        private static final String PAGE_PARAM_NAME = "page";

        @lombok.Builder(builderClassName = "Builder")
        private RequestParser(
                @NonNull Integer defaultPageSize, @NonNull Integer minPageSize, @NonNull Integer maxPageSize,
                @Singular("defaultSort") List<String> defaultSorts, @Singular("sortableProperty") Set<String> sortableProperties
        ) {
            super(Request::new, defaultPageSize, minPageSize, maxPageSize, defaultSorts, sortableProperties);
        }

        public Request of(Function<String, String> getParameterFn) {
            int page = ValidationUtil.getIntParameter(PAGE_PARAM_NAME, getParameterFn.apply(PAGE_PARAM_NAME), 0);
            ValidationUtil.min(PAGE_PARAM_NAME, page, 0);

            Request ret = super.of(getParameterFn);
            ret.setPage(page);
            return ret;
        }
    }
}

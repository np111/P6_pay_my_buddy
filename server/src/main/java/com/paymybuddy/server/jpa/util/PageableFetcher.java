package com.paymybuddy.server.jpa.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.paymybuddy.api.model.collection.PageableResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongSupplier;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.context.request.WebRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Accessors(fluent = true)
public class PageableFetcher<T> {
    public static <T> PageableFetcher<T> create() {
        return new PageableFetcher<>();
    }

    private Function<Pageable, List<T>> recordsFinder;
    private LongSupplier recordsCounter;

    private Sort defaultSort;
    private final Set<String> sortableProperties = new HashSet<>();
    private Function<String, String> sortablePropertyTransformer;

    private int minPerPage = 10;
    private int maxPerPage = 100;
    private int defaultPerPage = 20;

    public PageableFetcher<T> sortableProperty(String property) {
        sortableProperties.add(property);
        return this;
    }

    public PageableResponse<T> fetch(Params params) {
        Preconditions.checkNotNull(recordsFinder, "recordsFinder cannot be null");
        Preconditions.checkNotNull(recordsCounter, "recordsCounter cannot be null");
        Preconditions.checkNotNull(defaultSort, "defaultSort cannot be null");
        Preconditions.checkState(minPerPage > 0, "minPerPage must be positive");
        Preconditions.checkState(maxPerPage >= minPerPage, "maxPerPage must be greater than minPerPage");

        int page = getPageParam(params);
        int perPage = getPerPageParam(params);
        Sort sort = getSortParam(params);

        List<T> records = recordsFinder.apply(PageRequest.of(page, perPage, sort));
        int totalCount = Math.toIntExact(recordsCounter.getAsLong());
        return PageableResponse.<T>builder()
                .page(page)
                .perPage(perPage)
                .pageCount((totalCount + (perPage - 1)) / perPage)
                .totalCount(totalCount)
                .records(records)
                .build();
    }

    private int getPageParam(Params params) {
        Integer pageParam = params.getPage();
        int page = pageParam == null ? 0 : pageParam;
        if (page < 0) {
            // TODO: error
            page = 0;
        }
        return page;
    }

    private int getPerPageParam(Params params) {
        Integer perPageParam = params.getPerPage();
        int perPage = perPageParam == null ? defaultPerPage : perPageParam;
        if (perPage < minPerPage) {
            // TODO: error
            perPage = minPerPage;
        }
        if (perPage > maxPerPage) {
            // TODO: error
            perPage = maxPerPage;
        }
        return perPage;
    }

    private Sort getSortParam(Params params) {
        List<String> sortParams = params.getSorts();
        List<Sort.Order> orders = null;
        if (sortParams != null) {
            orders = new ArrayList<>();
            for (String sortParam : sortParams) {
                boolean asc = true;
                if (sortParam.startsWith("-")) {
                    asc = false;
                    sortParam = sortParam.substring(1);
                }
                if (!sortableProperties.contains(sortParam)) {
                    // TODO: error
                    continue;
                }
                if (sortablePropertyTransformer != null) {
                    sortParam = sortablePropertyTransformer.apply(sortParam);
                }
                orders.add(asc ? Sort.Order.asc(sortParam) : Sort.Order.desc(sortParam));
            }
        }
        return orders == null ? defaultSort : Sort.by(orders);
    }

    public interface Params {
        Integer getPage();

        Integer getPerPage();

        List<String> getSorts();
    }

    @RequiredArgsConstructor
    public static class WebParams implements Params {
        private static final Splitter SORT_SPLITTER = Splitter.on(',').limit(20);

        private final @NonNull WebRequest webRequest;

        @Override
        public Integer getPage() {
            String pageParam = webRequest.getParameter("page");
            if (pageParam != null) {
                try {
                    return Integer.parseInt(pageParam);
                } catch (NumberFormatException ignored) {
                    // TODO: error?
                }
            }
            return null;
        }

        @Override
        public Integer getPerPage() {
            String perPageParam = webRequest.getParameter("perPage");
            if (perPageParam != null) {
                try {
                    return Integer.parseInt(perPageParam);
                } catch (NumberFormatException ignored) {
                    // TODO: error?
                }
            }
            return null;
        }

        @Override
        public List<String> getSorts() {
            String sortParam = webRequest.getParameter("sort");
            if (sortParam != null && !sortParam.isEmpty()) {
                return SORT_SPLITTER.splitToList(sortParam);
            }
            return null;
        }
    }
}

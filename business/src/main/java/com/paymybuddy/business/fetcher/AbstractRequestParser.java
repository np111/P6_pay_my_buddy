package com.paymybuddy.business.fetcher;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.paymybuddy.business.util.PageableUtil;
import com.paymybuddy.business.util.ValidationUtil;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.Singular;

public abstract class AbstractRequestParser<T extends AbstractRequest> {
    protected static final String PAGE_SIZE_PARAM_NAME = "pageSize";
    protected static final String PAGE_SORT_PARAM_NAME = "pageSort";
    protected static final Splitter PAGE_SORT_SPLITTER = Splitter.on(',').limit(20);

    protected Supplier<T> requestConstructor;
    protected int defaultPageSize;
    protected int minPageSize;
    protected int maxPageSize;
    protected List<String> defaultSort;
    protected Set<String> sortableProperties;

    protected AbstractRequestParser(
            Supplier<T> requestConstructor,
            @NonNull Integer defaultPageSize, @NonNull Integer minPageSize, @NonNull Integer maxPageSize,
            @Singular("defaultSort") List<String> defaultSorts, @Singular("sortableProperty") Set<String> sortableProperties
    ) {
        this.requestConstructor = requestConstructor;

        this.minPageSize = minPageSize;
        Preconditions.checkState(this.minPageSize > 0, "minPageSize must be positive");

        this.maxPageSize = maxPageSize;
        Preconditions.checkState(this.maxPageSize >= this.minPageSize, "maxPageSize must be greater than minPageSize");

        this.defaultPageSize = defaultPageSize;
        Preconditions.checkState(this.defaultPageSize >= this.minPageSize, "defaultPageSize must be greater than minPageSize");
        Preconditions.checkState(this.defaultPageSize <= this.maxPageSize, "defaultPageSize must be less than maxPageSize");

        this.defaultSort = defaultSorts;
        Preconditions.checkArgument(!this.defaultSort.isEmpty(), "defaultSort cannot be empty");

        this.sortableProperties = sortableProperties;
    }

    protected T of(Function<String, String> getParameterFn) {
        int pageSize = ValidationUtil.getIntParameter(PAGE_SIZE_PARAM_NAME, getParameterFn.apply(PAGE_SIZE_PARAM_NAME), defaultPageSize);
        ValidationUtil.min(PAGE_SIZE_PARAM_NAME, pageSize, minPageSize);
        ValidationUtil.max(PAGE_SIZE_PARAM_NAME, pageSize, maxPageSize);

        String pageSortValue = getParameterFn.apply(PAGE_SORT_PARAM_NAME);
        Collection<String> pageSort;
        if (pageSortValue != null && !pageSortValue.isEmpty()) {
            pageSort = new LinkedHashSet<>();
            for (String sort : PAGE_SORT_SPLITTER.split(pageSortValue)) {
                ValidationUtil.isIn(PAGE_SORT_PARAM_NAME, PageableUtil.getSortProperty(sort), sortableProperties);
                pageSort.add(sort);
            }
        } else {
            pageSort = defaultSort;
        }

        T ret = requestConstructor.get();
        ret.setPageSize(pageSize);
        ret.setPageSort(pageSort);
        return ret;
    }
}

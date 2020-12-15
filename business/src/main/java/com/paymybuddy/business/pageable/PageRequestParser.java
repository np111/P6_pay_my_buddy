package com.paymybuddy.business.pageable;

import com.paymybuddy.business.util.ValidationUtil;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.NonNull;
import lombok.Singular;

/**
 * Parser utility to generate a {@link PageRequest} from users inputs.
 */
public class PageRequestParser extends AbstractRequestParser<PageRequest> {
    public static final String PAGE_PARAM_NAME = "page";

    @lombok.Builder(builderClassName = "Builder")
    private PageRequestParser(
            @NonNull Integer defaultPageSize, @NonNull Integer minPageSize, @NonNull Integer maxPageSize,
            @Singular("defaultSort") List<String> defaultSorts, @Singular("sortableProperty") Set<String> sortableProperties
    ) {
        super(PageRequest::new, defaultPageSize, minPageSize, maxPageSize, defaultSorts, sortableProperties);
    }

    public PageRequest of(Function<String, String> getParameterFn) {
        int page = ValidationUtil.getIntParameter(PAGE_PARAM_NAME, getParameterFn.apply(PAGE_PARAM_NAME), 0);
        ValidationUtil.min(PAGE_PARAM_NAME, page, 0);

        PageRequest ret = super.of(getParameterFn);
        ret.setPage(page);
        return ret;
    }
}

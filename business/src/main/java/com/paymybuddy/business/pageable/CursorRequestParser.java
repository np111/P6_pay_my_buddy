package com.paymybuddy.business.pageable;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.NonNull;
import lombok.Singular;

/**
 * Parser utility to generate a {@link CursorRequest} from users inputs.
 */
public class CursorRequestParser extends AbstractRequestParser<CursorRequest> {
    public static final String CURSOR_PARAM_NAME = "cursor";

    @lombok.Builder(builderClassName = "Builder")
    private CursorRequestParser(
            @NonNull Integer defaultPageSize, @NonNull Integer minPageSize, @NonNull Integer maxPageSize,
            @Singular("defaultSort") List<String> defaultSorts, @Singular("sortableProperty") Set<String> sortableProperties
    ) {
        super(CursorRequest::new, defaultPageSize, minPageSize, maxPageSize, defaultSorts, sortableProperties);
    }

    public CursorRequest of(Function<String, String> getParameterFn) {
        String cursor = getParameterFn.apply(CURSOR_PARAM_NAME);
        // cursor is decoded and validated when fetching

        CursorRequest ret = super.of(getParameterFn);
        ret.setCursor(cursor);
        return ret;
    }
}

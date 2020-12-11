package com.paymybuddy.business.fetcher;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Longs;
import com.paymybuddy.api.model.collection.CursorResponse;
import com.paymybuddy.business.util.PageableUtil;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Accessors(fluent = true)
public class CursorFetcher<T, E> {
    private static final BaseEncoding BASE64_URL = BaseEncoding.base64Url().omitPadding();
    private static final Splitter DOT_SPLITTER = Splitter.on('.');

    public static <T, E> CursorFetcher<T, E> create() {
        return new CursorFetcher<>();
    }

    private Function<Query<E>, Page<E>> recordsQuery;
    private Function<E, T> recordMapper;
    private String uniquePropertyName;
    private final Map<String, Property<?, E>> properties = new HashMap<>();
    private Function<String, String> propertyTransformer;

    public <V> CursorFetcher<T, E> property(String propertyName, PropertyType<V> type, Function<E, V> accessor) {
        return property(propertyName, type, accessor, false);
    }

    public <V> CursorFetcher<T, E> property(String propertyName, PropertyType<V> type, Function<E, V> accessor, boolean unique) {
        if (uniquePropertyName == null && unique) {
            uniquePropertyName = propertyName;
        }
        properties.put(propertyName, new Property<>(type, accessor, unique));
        return this;
    }

    public CursorResponse<T> fetch(Request request) {
        Preconditions.checkNotNull(recordsQuery, "recordsQuery cannot be null");
        Preconditions.checkNotNull(recordMapper, "recordMapper cannot be null");
        Preconditions.checkNotNull(uniquePropertyName, "require at least one unique property");

        CursorResponse<T> res = CursorResponse.<T>builder().hasNext(false).hasPrev(false).build();
        String cursor = request.getCursor();
        int pageSize = request.getPageSize();
        List<Sort.Order> sorts = PageableUtil.parseSortInstructions(request.getPageSort(), propertyTransformer);
        boolean hasUnique = false;
        for (Sort.Order sort : sorts) {
            Property<?, E> property = properties.get(sort.getProperty());
            if (property == null) {
                throw new IllegalArgumentException(sort.getProperty() + " is not a supported property");
            }
            if (property.isUnique()) {
                hasUnique = true;
            }
        }
        if (!hasUnique) {
            sorts.add(Sort.Order.asc(uniquePropertyName));
        }

        Cursor c = decodeCursor(cursor, sorts); // TODO: wrap exceptions
        boolean reverse = false;
        Specification<E> specification = Specification.where(null);
        if (c != null) {
            if (c.getType().isBefore()) {
                reverse = true;
                if (!c.getType().isInclude()) {
                    res.setPrevCursor(cursor);
                    res.setNextCursor(CursorType.AFTER_INCLUDE.getSymbol() + cursor.substring(1));
                    res.setHasNext(true);
                }
            } else if (!c.getType().isInclude()) {
                res.setNextCursor(cursor);
                res.setPrevCursor(CursorType.BEFORE_INCLUDE.getSymbol() + cursor.substring(1));
                res.setHasPrev(true);
            }

            Specification<E> prev = null;
            int valueIndex = 0;
            for (Sort.Order sort : sorts) {
                String property = sort.getProperty();
                CursorComparator comparator;
                if (sort.getDirection() == Direction.ASC) {
                    comparator = c.getType().getComparator();
                } else {
                    comparator = c.getType().inverse().getComparator();
                }

                Comparable value = (Comparable) c.getValues().get(valueIndex);
                if (prev == null) {
                    specification = specification.or((root, query, builder) -> comparator.compare(builder, root.get(property), value));
                    prev = (root, query, builder) -> builder.equal(root.get(property), value);
                } else {
                    Specification<E> prevF = prev;
                    specification = specification.or((root, query, builder) -> builder.and(prevF.toPredicate(root, query, builder), comparator.compare(builder, root.get(property), value)));
                    prev = (root, query, builder) -> builder.and(prevF.toPredicate(root, query, builder), builder.equal(root.get(property), value));
                }
                ++valueIndex;
            }
        }

        Sort sort;
        if (reverse) {
            sort = Sort.by(sorts.stream()
                    .map(o -> new Sort.Order(o.getDirection() == Direction.ASC ? Direction.DESC : Direction.ASC, o.getProperty()))
                    .collect(Collectors.toList()));
        } else {
            sort = Sort.by(sorts);
        }

        Iterator<E> it = recordsQuery.apply(new Query<>(specification, PageRequest.of(0, pageSize + 1, sort))).iterator();
        List<E> entities = new ArrayList<>(pageSize);
        int entitiesSize = 0;
        for (; it.hasNext() && entitiesSize < pageSize; ++entitiesSize) {
            entities.add(it.next());
        }

        if (!entities.isEmpty()) {
            if (!reverse) {
                res.setPrevCursor(encodeCursor(CursorType.BEFORE, entities.get(0), sorts));
                res.setNextCursor(encodeCursor(CursorType.AFTER, entities.get(entitiesSize - 1), sorts));
                res.setHasNext(it.hasNext());
            } else {
                res.setNextCursor(encodeCursor(CursorType.AFTER, entities.get(0), sorts));
                res.setPrevCursor(encodeCursor(CursorType.BEFORE, entities.get(entitiesSize - 1), sorts));
                res.setHasPrev(it.hasNext());
            }
        }

        List<T> records = entities.stream().map(recordMapper).collect(Collectors.toList());
        res.setRecords(reverse ? Lists.reverse(records) : records);
        return res;
    }

    private Cursor decodeCursor(String cursor, List<Sort.Order> sorts) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }

        CursorType type = CursorType.bySymbol(cursor.charAt(0));
        if (type == null) {
            // TODO: Exception
            return null;
        }

        List<Object> values = new ArrayList<>(sorts.size());
        int valuesIndex = 0;
        for (String str : DOT_SPLITTER.split(cursor.substring(1))) {
            if (valuesIndex >= sorts.size()) {
                // TODO: Exception
                return null;
            }

            if (str.equals("$")) {
                values.add(null);
            } else {
                byte[] bytes;
                try {
                    bytes = BASE64_URL.decode(str);
                } catch (IllegalArgumentException e) {
                    // TODO: Exception
                    return null;
                }
                Property<?, E> property = properties.get(sorts.get(valuesIndex).getProperty());
                values.add(property.getType().deserialize(bytes));
            }
            ++valuesIndex;
        }
        if (valuesIndex != sorts.size()) {
            // TODO: Exception
            return null;
        }
        return new Cursor(type, values);
    }

    @SuppressWarnings("unchecked")
    private String encodeCursor(CursorType type, E entity, List<Sort.Order> sorts) {
        return type.getSymbol() + sorts.stream()
                .map(sort -> {
                    Property<Object, E> property = (Property<Object, E>) properties.get(sort.getProperty());
                    Object value = property.getAccessor().apply(entity);
                    if (value == null) {
                        return "$";
                    }
                    return BASE64_URL.encode(property.getType().serialize(value));
                })
                .collect(Collectors.joining("."));
    }

    @RequiredArgsConstructor
    @Data
    @Accessors(fluent = false)
    public static class Query<E> {
        private final Specification<E> specification;
        private final Pageable pageable;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = false)
    public static class Request extends AbstractRequest {
        private String cursor;
    }

    public static class RequestParser extends AbstractRequestParser<Request> {
        private static final String CURSOR_PARAM_NAME = "cursor";

        @lombok.Builder(builderClassName = "Builder")
        private RequestParser(
                @NonNull Integer defaultPageSize, @NonNull Integer minPageSize, @NonNull Integer maxPageSize,
                @Singular("defaultSort") List<String> defaultSorts, @Singular("sortableProperty") Set<String> sortableProperties
        ) {
            super(Request::new, defaultPageSize, minPageSize, maxPageSize, defaultSorts, sortableProperties);
        }

        public Request of(Function<String, String> getParameterFn) {
            String cursor = getParameterFn.apply(CURSOR_PARAM_NAME);
            // TODO: validate cursor

            Request ret = super.of(getParameterFn);
            ret.setCursor(cursor);
            return ret;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Accessors(fluent = false)
    private static class Cursor {
        private CursorType type;
        private List<Object> values;
    }

    @RequiredArgsConstructor
    @Getter
    @Accessors(fluent = false)
    private enum CursorType {
        AFTER('a', CriteriaBuilder::greaterThan, false, false) {
            @Override
            public CursorType inverse() {
                return BEFORE;
            }
        },
        AFTER_INCLUDE('A', CriteriaBuilder::greaterThanOrEqualTo, false, true) {
            @Override
            public CursorType inverse() {
                return BEFORE_INCLUDE;
            }
        },
        BEFORE('b', CriteriaBuilder::lessThan, true, false) {
            @Override
            public CursorType inverse() {
                return AFTER;
            }
        },
        BEFORE_INCLUDE('B', CriteriaBuilder::lessThanOrEqualTo, true, true) {
            @Override
            public CursorType inverse() {
                return AFTER_INCLUDE;
            }
        };

        private static final Map<Character, CursorType> BY_SYMBOL;

        static {
            Map<Character, CursorType> bySymbol = new HashMap<>(values().length, 0.25F);
            for (CursorType cursorType : values()) {
                bySymbol.put(cursorType.symbol, cursorType);
            }
            BY_SYMBOL = Collections.unmodifiableMap(bySymbol);
        }

        public static CursorType bySymbol(char symbol) {
            return BY_SYMBOL.get(symbol);
        }

        private final char symbol;
        private final CursorComparator comparator;
        private final boolean before;
        private final boolean include;

        public abstract CursorType inverse();
    }

    @FunctionalInterface
    private interface CursorComparator<Y extends Comparable<? super Y>> {
        Predicate compare(CriteriaBuilder builder, Expression<? extends Y> x, Y y);
    }

    @RequiredArgsConstructor
    @Data
    @Accessors(fluent = false)
    private static class Property<T, E> {
        private final PropertyType<T> type;
        private final Function<E, T> accessor;
        private final boolean unique;
    }

    public interface PropertyType<T> {
        byte[] serialize(T value);

        T deserialize(byte[] str);
    }

    public static class LongPropertyType implements PropertyType<Long> {
        @Override
        public byte[] serialize(Long value) {
            return Longs.toByteArray(value);
        }

        @Override
        public Long deserialize(byte[] bytes) {
            return Longs.fromByteArray(bytes);
        }
    }

    public static class BigDecimalPropertyType implements PropertyType<BigDecimal> {
        @Override
        public byte[] serialize(BigDecimal value) {
            return value.stripTrailingZeros().toPlainString().getBytes(StandardCharsets.US_ASCII);
        }

        @Override
        public BigDecimal deserialize(byte[] bytes) {
            return new BigDecimal(new String(bytes, StandardCharsets.US_ASCII));
        }
    }
}

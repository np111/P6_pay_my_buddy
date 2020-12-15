package com.paymybuddy.business.pageable;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.paymybuddy.api.model.collection.CursorResponse;
import com.paymybuddy.business.exception.PreconditionException;
import com.paymybuddy.business.pageable.type.PropertyType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

/**
 * An utility to retrieve paginated results, using an CURSOR/COMPARISON logic.
 * <p>
 * See: https://medium.com/swlh/why-you-shouldnt-use-offset-and-limit-for-your-pagination-4440e421ba87
 *
 * @param <Model>  model
 * @param <Entity> database entity
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Accessors(fluent = true)
public class CursorFetcher<Model, Entity> {
    private static final BaseEncoding BASE64_URL = BaseEncoding.base64Url().omitPadding();
    private static final Splitter DOT_SPLITTER = Splitter.on('.');

    /**
     * Create a new {@link CursorFetcher} instance.
     * <p>
     * You must now set {@link #recordsQuery(Function)}, {@link #recordMapper(Function)} and
     * at least one {@link #property(String, PropertyType, Function, boolean)} before calling {@link #fetch(CursorRequest)}.
     */
    public static <Model, Entity> CursorFetcher<Model, Entity> create() {
        return new CursorFetcher<>();
    }

    /**
     * Query function, to retrieve {@linkplain #<Entity> entities} records.
     */
    private Function<Query<Entity>, Page<Entity>> recordsQuery;

    /**
     * Mapping function, to transform the fetched {@linkplain #<Entity> entities} records to {@linkplain #<Model> models}.
     */
    private Function<Entity, Model> recordMapper;

    /**
     * Sortable properties, used by the cursor for comparisons.
     */
    private final Map<String, Property<?, Entity>> properties = new HashMap<>();
    private String uniquePropertyName;

    private Function<String, String> propertyTransformer;

    /**
     * Register a non-unique property.
     *
     * @return this
     * @see #property(String, PropertyType, Function, boolean)
     */
    public <V> CursorFetcher<Model, Entity> property(String propertyName, PropertyType<V> type, Function<Entity, V> accessor) {
        return property(propertyName, type, accessor, false);
    }

    /**
     * Register a property. A property is a sortable entity column (used for the cursor comparisons).
     * At least one unique property is required to maintains the cursor consistency.
     *
     * @param propertyName name of the property, must be the same as the entity column name (eg. "id")
     * @param type         type of the column (to serialize/deserialize the cursor)
     * @param accessor     column getter (eg. {@code <code>Entity::getId</code>})
     * @param unique       whether this column is unique or not
     * @return this
     */
    public <V> CursorFetcher<Model, Entity> property(String propertyName, PropertyType<V> type, Function<Entity, V> accessor, boolean unique) {
        if (uniquePropertyName == null && unique) {
            uniquePropertyName = propertyName;
        }
        properties.put(propertyName, new Property<>(type, accessor, unique));
        return this;
    }

    /**
     * Perform a request and returns paginated results.
     *
     * @param request request parameters
     * @return the paginated results
     */
    public CursorResponse<Model> fetch(CursorRequest request) {
        Preconditions.checkNotNull(recordsQuery, "recordsQuery cannot be null");
        Preconditions.checkNotNull(recordMapper, "recordMapper cannot be null");
        Preconditions.checkNotNull(uniquePropertyName, "require at least one unique property");

        CursorResponse<Model> res = CursorResponse.<Model>builder().hasNext(false).hasPrev(false).build();
        String cursor = request.getCursor();
        int pageSize = request.getPageSize();

        // Parse pageSort, and validate that they belong to a registered property.
        // Also check that at least one unique property is used, else add the first unique property at end.
        List<Sort.Order> sorts = PageableUtil.parseSortInstructions(request.getPageSort(), propertyTransformer);
        boolean hasUnique = false;
        for (Sort.Order sort : sorts) {
            Property<?, Entity> property = properties.get(sort.getProperty());
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

        // Decode/deserialize the cursor
        Cursor c;
        try {
            c = decodeCursor(cursor, sorts);
        } catch (IllegalArgumentException ignored) {
            throw new PreconditionException("cursor", "IsCursor", "must be a valid cursor", Collections.emptyMap());
        }

        // Generate the query specification
        // TODO: Explain the algorithm (reverse, pre-set cursors, specification comparisons & chaining)
        boolean reverse = false;
        Specification<Entity> specification = Specification.where(null);
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

            Specification<Entity> prev = null;
            int valueIndex = 0;
            for (Sort.Order sort : sorts) {
                String property = sort.getProperty();
                CursorComparator comparator;
                if (sort.getDirection() == Direction.ASC) {
                    comparator = c.getType().getComparator();
                } else {
                    comparator = c.getType().inverse().getComparator();
                }

                @SuppressWarnings("unchecked") // values are validated in decodeCursor
                Comparable<Object> value = (Comparable<Object>) c.getValues().get(valueIndex);
                if (prev == null) {
                    specification = specification.or((root, query, builder) -> comparator.compare(builder, root.get(property), value));
                    prev = (root, query, builder) -> builder.equal(root.get(property), value);
                } else {
                    Specification<Entity> prevF = prev;
                    specification = specification.or((root, query, builder) -> builder.and(prevF.toPredicate(root, query, builder), comparator.compare(builder, root.get(property), value)));
                    prev = (root, query, builder) -> builder.and(prevF.toPredicate(root, query, builder), builder.equal(root.get(property), value));
                }
                ++valueIndex;
            }
        }

        // Generate the query sort (reversing it if needed - see the explanation above)
        Sort sort;
        if (reverse) {
            sort = Sort.by(sorts.stream()
                    .map(o -> new Sort.Order(o.getDirection() == Direction.ASC ? Direction.DESC : Direction.ASC, o.getProperty()))
                    .collect(Collectors.toList()));
        } else {
            sort = Sort.by(sorts);
        }

        // Perform the query (to retrieve entities)
        // TODO: Explain the 'pageSize + 1' limit
        // TODO: Find a way to prevent spring from fetching totalElements (since we are using PageRequest)...
        //       This is not needed here and costly.
        Iterator<Entity> it = recordsQuery.apply(new Query<>(specification, PageRequest.of(0, pageSize + 1, sort))).iterator();
        List<Entity> entities = new ArrayList<>(pageSize);
        int entitiesSize = 0;
        for (; it.hasNext() && entitiesSize < pageSize; ++entitiesSize) {
            entities.add(it.next());
        }

        // Set the future cursor values (reversing them if needed - see the explanation above)
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

        // And finally, map the entities to their models (and reverse the result if needed - see the explanation above)
        List<Model> records = entities.stream().map(recordMapper).collect(Collectors.toList());
        res.setRecords(reverse ? Lists.reverse(records) : records);
        return res;
    }

    /**
     * Deserialize the cursor from it's string representation.
     */
    private Cursor decodeCursor(String cursor, List<Sort.Order> sorts) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }

        CursorType type = CursorType.bySymbol(cursor.charAt(0));
        if (type == null) {
            throw new IllegalArgumentException("Unknown cursor type");
        }

        List<Object> values = new ArrayList<>(sorts.size());
        int valuesIndex = 0;
        for (String str : DOT_SPLITTER.split(cursor.substring(1))) {
            if (valuesIndex >= sorts.size()) {
                throw new IllegalArgumentException("Cursor overflow");
            }

            if (str.equals("$")) {
                values.add(null);
            } else {
                try {
                    byte[] bytes = BASE64_URL.decode(str);
                    Property<?, Entity> property = properties.get(sorts.get(valuesIndex).getProperty());
                    values.add(property.getType().deserialize(bytes));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unreadable cursor property", e);
                }
            }
            ++valuesIndex;
        }
        if (valuesIndex != sorts.size()) {
            throw new IllegalArgumentException("Incomplete cursor");
        }
        return new Cursor(type, values);
    }

    /**
     * Serialize a cursor, to a string representation.
     */
    @SuppressWarnings("unchecked")
    private String encodeCursor(CursorType type, Entity entity, List<Sort.Order> sorts) {
        return type.getSymbol() + sorts.stream()
                .map(sort -> {
                    Property<Object, Entity> property = (Property<Object, Entity>) properties.get(sort.getProperty());
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
    private interface CursorComparator {
        <Y extends Comparable<? super Y>> Predicate compare(CriteriaBuilder builder, Expression<? extends Y> x, Y y);
    }

    @RequiredArgsConstructor
    @Data
    @Accessors(fluent = false)
    private static class Property<T, E> {
        private final PropertyType<T> type;
        private final Function<E, T> accessor;
        private final boolean unique;
    }
}

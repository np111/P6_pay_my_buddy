package com.paymybuddy.server.util;

import com.google.common.collect.ImmutableMap;
import com.paymybuddy.server.util.exception.PreconditionException;
import java.util.Collection;

public class ValidationUtil {
    public static void min(String parameterName, long parameter, long value) {
        if (parameter < value) {
            throw new PreconditionException(parameterName, "Min", "must be greater than or equal to " + value, ImmutableMap.of("parameter", parameter, "value", value));
        }
    }

    public static void max(String parameterName, long parameter, long value) {
        if (parameter > value) {
            throw new PreconditionException(parameterName, "Max", "must be less than or equal to " + value, ImmutableMap.of("parameter", parameter, "value", value));
        }
    }

    public static <T> void isIn(String parameterName, T parameter, Collection<T> values) {
        if (!values.contains(parameter)) {
            throw new PreconditionException(parameterName, "IsIn", "does not match an allowed value", ImmutableMap.of("parameter", parameter, "values", values));
        }
    }

    public static Integer getIntParameter(String parameterName, String parameter, Integer def) {
        if (parameter != null) {
            try {
                return Integer.parseInt(parameter);
            } catch (NumberFormatException ex) {
                throw new PreconditionException(parameterName, null, "must be an Integer", ImmutableMap.of("parameter", parameter));
            }
        }
        return def;
    }
}

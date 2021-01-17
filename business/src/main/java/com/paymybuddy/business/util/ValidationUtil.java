package com.paymybuddy.business.util;

import com.google.common.collect.ImmutableMap;
import com.paymybuddy.business.exception.PreconditionException;
import java.util.Collection;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtil {
    /**
     * Perform a {@link javax.validation.constraints.Min} validation.
     *
     * @param parameterName name of the parameter to validate
     * @param parameter     value of the parameter to validate
     * @param value         {@link javax.validation.constraints.Min#value()}
     * @throws PreconditionException on failure
     */
    public static void min(String parameterName, long parameter, long value) {
        if (parameter < value) {
            throw new PreconditionException(parameterName, "Min", "must be greater than or equal to " + value, ImmutableMap.of("parameter", parameter, "value", value));
        }
    }

    /**
     * Perform a {@link javax.validation.constraints.Max} validation.
     *
     * @param parameterName name of the parameter to validate
     * @param parameter     value of the parameter to validate
     * @param value         {@link javax.validation.constraints.Max#value()}
     * @throws PreconditionException on failure
     */
    public static void max(String parameterName, long parameter, long value) {
        if (parameter > value) {
            throw new PreconditionException(parameterName, "Max", "must be less than or equal to " + value, ImmutableMap.of("parameter", parameter, "value", value));
        }
    }

    /**
     * Checks if a parameter is contained in the given collection.
     *
     * @param parameterName name of the parameter to validate
     * @param parameter     value of the parameter to validate
     * @param values        the collection
     * @throws PreconditionException on failure
     */
    public static <T> void isIn(String parameterName, T parameter, Collection<T> values) {
        if (!values.contains(parameter)) {
            throw new PreconditionException(parameterName, "IsIn", "does not match an allowed value", ImmutableMap.of("parameter", parameter, "values", values));
        }
    }

    /**
     * Returns a string parameter as an integer.
     *
     * @param parameterName name of the parameter
     * @param parameter     value of the parameter
     * @param def           default value to returns if the parameter is null
     * @return the parameter parsed as an integer
     * @throws PreconditionException on parsing failure
     */
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

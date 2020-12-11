package com.paymybuddy.business.exception;

import java.util.Map;
import lombok.Getter;

@Getter
public class PreconditionException extends FastRuntimeException {
    private final String parameter;
    private final String constraint;
    private final Map<String, Object> attributes;

    @lombok.Builder(builderClassName = "Builder")
    public PreconditionException(String parameter, String constraint, String message, Map<String, Object> attributes) {
        super(message);
        this.parameter = parameter;
        this.constraint = constraint;
        this.attributes = attributes;
    }
}

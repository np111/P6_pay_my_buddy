package com.paymybuddy.server.util.exception;

public class NotHimselfException extends PreconditionException {
    public NotHimselfException(String parameter) {
        this(parameter, "cannot be equal to your userId");
    }

    public NotHimselfException(String parameter, String message) {
        super(parameter, "NotHimself", message, null);
    }
}

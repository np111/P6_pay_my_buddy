package com.paymybuddy.server.util.exception;

public class FastException extends Exception {
    public FastException() {
    }

    public FastException(String message) {
        super(message);
    }

    public FastException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastException(Throwable cause) {
        super(cause);
    }

    @Override
    public Throwable initCause(Throwable throwable) {
        return this;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}

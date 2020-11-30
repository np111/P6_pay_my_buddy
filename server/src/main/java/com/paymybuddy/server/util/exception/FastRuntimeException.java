package com.paymybuddy.server.util.exception;

public class FastRuntimeException extends RuntimeException {
    public FastRuntimeException() {
    }

    public FastRuntimeException(String message) {
        super(message);
    }

    public FastRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FastRuntimeException(Throwable cause) {
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

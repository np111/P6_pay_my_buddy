package com.paymybuddy.business.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Thrown when the given password does not contain enough characters.
 */
@RequiredArgsConstructor
@Getter
public class TooShortPasswordException extends FastRuntimeException {
    private final int length;

    public TooShortPasswordException() {
        this(0);
    }
}

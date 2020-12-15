package com.paymybuddy.business.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Thrown when the given password contains too many characters.
 */
@RequiredArgsConstructor
@Getter
public class TooLongPasswordException extends FastRuntimeException {
    private final int length;
}

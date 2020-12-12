package com.paymybuddy.business.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TooLongPasswordException extends FastRuntimeException {
    private final int length;
}

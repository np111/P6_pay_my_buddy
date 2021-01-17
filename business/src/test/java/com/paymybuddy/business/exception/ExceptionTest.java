package com.paymybuddy.business.exception;

import com.paymybuddy.api.model.Currency;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {
    @Test
    void defaultTooShortPasswordException() {
        TooShortPasswordException ex = new TooShortPasswordException();
        assertEquals(0, ex.getLength());
    }

    @Test
    void defaultTooLongPasswordException() {
        TooLongPasswordException ex = new TooLongPasswordException();
        assertEquals(0, ex.getLength());
    }

    @Test
    void defaultNotEnoughFundsException() {
        NotEnoughFundsException ex = new NotEnoughFundsException();
        assertEquals(Currency.USD, ex.getCurrency());
        assertEquals(BigDecimal.ZERO, ex.getMissingAmount());
    }
}
package com.paymybuddy.business.util;

import com.paymybuddy.business.exception.PreconditionException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {
    @Test
    void min() {
        ValidationUtil.min("param", 5, 5);
        ValidationUtil.min("param", 6, 5);
        assertThrows(PreconditionException.class, () -> ValidationUtil.min("param", 4, 5));
    }

    @Test
    void max() {
        ValidationUtil.max("param", 5, 5);
        ValidationUtil.max("param", 4, 5);
        assertThrows(PreconditionException.class, () -> ValidationUtil.max("param", 6, 5));
    }

    @Test
    void isIn() {
        ValidationUtil.isIn("param", "AA", Arrays.asList("AA"));
        ValidationUtil.isIn("param", "AA", Arrays.asList("BB", "DD", "AA", "CC"));
        assertThrows(PreconditionException.class, () -> ValidationUtil.isIn("param", "AA", Arrays.asList("BB", "DD")));
    }

    @Test
    void getIntParameter() {
        assertEquals(17, ValidationUtil.getIntParameter("param", null, 17));
        assertEquals(18, ValidationUtil.getIntParameter("param", "18", 17));
        assertThrows(PreconditionException.class, () -> ValidationUtil.getIntParameter("param", "18x", 17));
    }
}
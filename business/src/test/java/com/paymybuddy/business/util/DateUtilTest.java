package com.paymybuddy.business.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {
    @Test
    void now() {
        ZonedDateTime exceptedNow = ZonedDateTime.now();
        ZonedDateTime now = DateUtil.now();
        assertEquals(0, now.getNano());
        assertEquals(ZoneOffset.UTC, now.getOffset());
        assertEquals(0, ChronoUnit.SECONDS.between(exceptedNow, now));
    }
}

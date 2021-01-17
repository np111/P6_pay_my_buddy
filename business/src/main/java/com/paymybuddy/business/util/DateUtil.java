package com.paymybuddy.business.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateUtil {
    /**
     * Returns the current date-time (in UTC, without nanoseconds).
     */
    public static ZonedDateTime now() {
        return ZonedDateTime.now(ZoneOffset.UTC).withNano(0);
    }
}

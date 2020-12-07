package com.paymybuddy.server.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DateUtil {
    public static ZonedDateTime now() {
        return ZonedDateTime.now(ZoneOffset.UTC).withNano(0);
    }
}

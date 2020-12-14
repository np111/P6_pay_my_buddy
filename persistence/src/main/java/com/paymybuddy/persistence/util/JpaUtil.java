package com.paymybuddy.persistence.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JpaUtil {
    public static String escapeLikeParam(String param) {
        return param.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}

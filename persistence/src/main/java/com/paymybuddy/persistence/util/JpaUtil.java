package com.paymybuddy.persistence.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JpaUtil {
    /**
     * Escape a user input to be used safely as JPA/SQL LIKE parameter.
     *
     * @param param input to escape
     * @return the escaped value
     */
    public static String escapeLikeParam(String param) {
        return param.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}

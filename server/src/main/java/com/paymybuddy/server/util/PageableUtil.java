package com.paymybuddy.server.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

@UtilityClass
public class PageableUtil {
    public static List<Sort.Order> parseSortInstructions(Collection<String> instructions, Function<String, String> propertyTransformer) {
        if (instructions.isEmpty()) {
            throw new IllegalArgumentException("empty sort instructions");
        }
        return instructions.stream()
                .map(instruction -> parseSortInstruction(instruction, propertyTransformer))
                .collect(Collectors.toList());
    }

    private static Sort.Order parseSortInstruction(String instruction, Function<String, String> propertyTransformer) {
        Sort.Direction direction;
        String property;
        if (instruction.startsWith("-")) {
            direction = Sort.Direction.DESC;
            property = instruction.substring(1);
        } else {
            direction = Sort.Direction.ASC;
            property = instruction;
        }
        return new Sort.Order(direction, propertyTransformer == null ? property : propertyTransformer.apply(property));
    }

    public static String getSortProperty(String instruction) {
        if (instruction.startsWith("-")) {
            return instruction.substring(1);
        }
        return instruction;
    }
}

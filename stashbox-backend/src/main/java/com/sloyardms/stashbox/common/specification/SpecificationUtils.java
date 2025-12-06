package com.sloyardms.stashbox.common.specification;

public abstract class SpecificationUtils {

    protected static String escapeLikePattern(String input) {
        String escaped = input.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

}

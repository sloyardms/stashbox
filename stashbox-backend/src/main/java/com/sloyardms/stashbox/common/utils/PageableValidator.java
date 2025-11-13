package com.sloyardms.stashbox.common.utils;

import com.sloyardms.stashbox.common.error.exception.InvalidSortFieldException;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public final class PageableValidator {

    private PageableValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates that all sort fields in the pageable are in the allowed fields set
     *
     * @param pageable      the pageable to validate
     * @param allowedFields the set of allowed sort field names
     * @throws InvalidSortFieldException if any sort field is not allowed
     */
    public static void validateSortFields(Pageable pageable, Set<String> allowedFields) {
        if (pageable == null || allowedFields == null) {
            return;
        }

        pageable.getSort().forEach(order -> {
            String property = order.getProperty();
            if (!allowedFields.contains(property)) {
                throw new InvalidSortFieldException(property, allowedFields);
            }
        });
    }

}

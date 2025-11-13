package com.sloyardms.stashbox.common.error.exception;

import lombok.Getter;

import java.util.Set;

@Getter
public class InvalidSortFieldException extends RuntimeException {

    private final String invalidField;
    private final Set<String> allowedFields;

    public InvalidSortFieldException(String invalidField, Set<String> allowedFields) {
        super(String.format("Invalid sort field '%s'. Allowed fields: %s",
                invalidField, allowedFields));
        this.invalidField = invalidField;
        this.allowedFields = allowedFields;
    }

}

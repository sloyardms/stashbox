package com.sloyardms.stashbox.config.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessageKey {

    VALIDATION_TITLE("error.validation.title"),
    VALIDATION_DETAIL("error.validation.detail"),
    CONSTRAINT_VIOLATION_TITLE("error.constraint-violation.title"),
    CONSTRAINT_VIOLATION_DETAIL("error.constraint-violation.detail"),
    BUSINESS_TITLE("error.business.title"),
    BUSINESS_DETAIL("error.business.detail"),
    RESOURCE_NOT_FOUND_TITLE("error.resource-not-found.title"),
    RESOURCE_NOT_FOUND_DETAIL("error.resource-not-found.detail"),
    RESOURCE_ALREADY_EXISTS_TITLE("error.resource-already-exists.title"),
    RESOURCE_ALREADY_EXISTS_DETAIL("error.resource-already-exists.detail"),
    INTERNAL_SERVER_TITLE("error.internal-server.title"),
    INTERNAL_SERVER_DETAIL("error.internal-server.detail"),
    AUTHENTICATION_TITLE("error.authentication.title"),
    AUTHENTICATION_DETAIL("error.authentication.detail"),
    ACCESS_DENIED_TITLE("error.access-denied.title"),
    ACCESS_DENIED_DETAIL("error.access-denied.detail"),
    MALFORMED_REQUEST_TITLE("error.malformed-request.title"),
    MALFORMED_REQUEST_DETAIL("error.malformed-request.detail"),
    DATA_INTEGRITY_VIOLATION_TITLE("error.data-integrity.title"),
    DATA_INTEGRITY_VIOLATION_DETAIL("error.data-integrity.detail"),
    HTTP_METHOD_NOT_ALLOWED_TITLE("error.method-not-allowed.title"),
    HTTP_METHOD_NOT_ALLOWED_DETAIL("error.method-not-allowed.detail"),
    UNSUPPORTED_MEDIA_TYPE_TITLE("error.unsupported-media-type.title"),
    UNSUPPORTED_MEDIA_TYPE_DETAIL("error.unsupported-media-type.detail"),
    MISSING_PARAMETER_TITLE("error.missing-parameter.title"),
    MISSING_PARAMETER_DETAIL("error.missing-parameter.detail"),
    TYPE_MISMATCH_TITLE("error.type-mismatch.title"),
    TYPE_MISMATCH_DETAIL("error.type-mismatch.detail"),
    FILE_TOO_LARGE_TITLE("error.file-too-large.title"),
    FILE_TOO_LARGE_DETAIL("error.file-too-large.detail"),
    NO_HANDLER_FOUND_TITLE("error.endpoint-not-found.title"),
    NO_HANDLER_FOUND_DETAIL("error.endpoint-not-found.detail"),
    DATABASE_ERROR_TITLE("error.database.title"),
    DATABASE_ERROR_DETAIL("error.database.detail"),
    INVALID_SORT_FIELD_TITLE("error.invalid-sort-field.title"),
    INVALID_SORT_FIELD_DETAIL("error.invalid-sort-field.detail");

    private final String key;

}

package com.sloyardms.backend.common.error;

import lombok.Getter;

/**
 * Custom exception for business logic errors
 */
@Getter
public class BusinessException extends RuntimeException{

    private final String errorCode;
    private final int statusCode;

    public BusinessException(String errorCode, String message, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

}

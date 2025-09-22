package com.sloyardms.backend.common.error;

import lombok.Getter;

/**
 * Custom exception for resource not found scenarios
 */
@Getter
public class ResourceNotFoundException extends RuntimeException{

    private final String resourceType;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s not found with ID: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

}

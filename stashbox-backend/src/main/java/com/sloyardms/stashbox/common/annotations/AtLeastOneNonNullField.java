package com.sloyardms.stashbox.common.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates if at least one field of the entity is non-null
 * Used for partial update requests
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneNonNullFieldValidator.class)
@Documented
public @interface AtLeastOneNonNullField {

    String message() default "{update.request.atLeastOneFieldProvided}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

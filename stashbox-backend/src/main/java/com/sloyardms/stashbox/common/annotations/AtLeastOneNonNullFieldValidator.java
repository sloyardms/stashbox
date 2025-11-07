package com.sloyardms.stashbox.common.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class AtLeastOneNonNullFieldValidator implements ConstraintValidator<AtLeastOneNonNullField, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        // At least one field must be non-null
        return Arrays.stream(value.getClass().getDeclaredFields())
                .filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                .anyMatch(field -> {
                    field.setAccessible(true);
                    try {
                        return field.get(value) != null;
                    } catch (IllegalAccessException e) {
                        return false;
                    }
                });
    }
}

package com.sloyardms.backend.common.entity;

import jakarta.validation.constraints.AssertTrue;

public abstract class UpdatableDto {

    @AssertTrue(message = "{common.update.atLeastOneFieldProvided}")
    public abstract boolean hasAtLeastOneField();

}

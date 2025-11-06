package com.sloyardms.stashbox.user.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UpdateUserSettingsRequest {

    private Boolean darkMode;

    @AssertTrue(message = "{update.request.atLeastOneFieldProvided}")
    public boolean hasAtLeastOneField() {
        return darkMode != null;
    }

}

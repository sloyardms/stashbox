package com.sloyardms.backend.user.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsUpdateDto {

    private Boolean darkMode;

    @AssertTrue(message = "{user.update.atLeastOneFieldProvided}")
    public boolean hasAtLeastOneField() {
        return darkMode != null;
    }

}

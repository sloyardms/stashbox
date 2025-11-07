package com.sloyardms.stashbox.user.dto;

import com.sloyardms.stashbox.common.annotations.AtLeastOneNonNullField;
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
@AtLeastOneNonNullField
public class UpdateUserSettingsRequest {

    private Boolean darkMode;

}

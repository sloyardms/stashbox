package com.sloyardms.backend.user.entity;

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
public class UserSettings {

    private Boolean darkMode = false;

    //other settings

    /**
     * Sets default values for the settings if they are not set
     * @param settings the settings to set defaults for
     * @return the settings with defaults set
     */
    public static UserSettings withDefaults(UserSettings settings){
        if(settings == null) return new UserSettings();
        if(settings.getDarkMode() == null) settings.darkMode = false;
        return settings;
    }

}

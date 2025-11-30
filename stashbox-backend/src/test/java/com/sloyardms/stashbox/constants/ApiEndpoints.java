package com.sloyardms.stashbox.constants;

public final class ApiEndpoints {

    private ApiEndpoints() {}

    // User
    public static final String USER_PROFILE = "/api/v1/users/me";
    public static final String USER_SETTINGS = "/api/v1/users/me/settings";
    public static final String ADMIN_USERS_LIST = "/api/v1/admin/users";
    public static final String ADMIN_USERS_BY_ID = "/api/v1/admin/users/{id}";

}

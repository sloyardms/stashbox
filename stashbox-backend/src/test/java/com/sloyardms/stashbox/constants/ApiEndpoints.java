package com.sloyardms.stashbox.constants;

public final class ApiEndpoints {

    private ApiEndpoints() {}

    // User
    public static final String USER_PROFILE = "/api/v1/users/me";
    public static final String USER_SETTINGS = "/api/v1/users/me/settings";
    public static final String ADMIN_USERS_LIST = "/api/v1/admin/users";
    public static final String ADMIN_USERS_BY_ID = "/api/v1/admin/users/{id}";

    // UserFilter
    public static final String USER_FILTER_BY_ID = "/api/v1/filters/{id}";
    public static final String USER_FILTERS = "/api/v1/filters";
    public static final String USER_FILTER_RECORD_MATCH = "/api/v1/filters/{id}/match";
    public static final String USER_FILTERS_DOMAIN_LIST = "/api/v1/filters/domains";

}

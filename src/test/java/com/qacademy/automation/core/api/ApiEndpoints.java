package com.qacademy.automation.core.api;

/**
 * Every REST endpoint path used by the API clients, in one place, so a path never gets
 * hand-typed - and mistyped - more than once.
 */
public final class ApiEndpoints {

    public static final String LOGIN = "/api/auth/login";
    public static final String REGISTER = "/api/auth/register";
    public static final String STUDENT = "/api/student";
    public static final String COURSE = "/api/course";
    public static final String ENROLLMENT = "/api/enrollment";

    private ApiEndpoints() {
        // constants holder - never instantiated
    }
}

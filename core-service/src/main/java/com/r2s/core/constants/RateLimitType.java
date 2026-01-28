package com.r2s.core.constants;

public final class RateLimitType {
    private RateLimitType() {}

    public static final String LOGIN = "login";
    public static final String GLOBAL = "global";
    public static final String INTROSPECT = "introspect";
    public static final String GET_ME = "get_me";

    public static final int ATTEMPTS = 5;
    public static final int TIME_TO_LIVE = 1;

}

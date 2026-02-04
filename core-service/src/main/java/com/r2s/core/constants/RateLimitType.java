package com.r2s.core.constants;

public final class RateLimitType {
    private RateLimitType() {}

    public static final int MAX_ATTEMPTS  = 5;
    public static final int TTL_SPAM_REQUEST = 1;
    public static final int TTL_BLOCK_REQUEST = 2;

}

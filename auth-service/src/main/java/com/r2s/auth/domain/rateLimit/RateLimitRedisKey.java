package com.r2s.auth.domain.rateLimit;

import org.springframework.stereotype.Component;

@Component
public class RateLimitRedisKey {

    private static final int SUFFIX_LENGTH = 5;

    public String build(String type, String value) {
        return "rate_limit:" + type + ":" + normalize(value);
    }

    private String normalize(String value) {
        if (value == null) {
            return "null";
        }
        if (value.length() <= SUFFIX_LENGTH) {
            return value;
        }
        return value.substring(value.length() - SUFFIX_LENGTH);
    }
}

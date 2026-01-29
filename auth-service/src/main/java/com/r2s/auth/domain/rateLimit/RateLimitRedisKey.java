package com.r2s.auth.domain.rateLimit;

import org.springframework.stereotype.Component;

@Component
public class RateLimitRedisKey {
    public String base(String method, String path, String clientKey) {
        return "rate_limit:" + method + ":" + path + ":" + normalize(clientKey);
    }

    public String blocked(String baseKey) {
        return baseKey + ":blocked";
    }

    private String normalize(String value) {
        if (value == null) return "null";
        if (value.length() <= 5) return value;
        return value.substring(value.length() - 5);
    }
}


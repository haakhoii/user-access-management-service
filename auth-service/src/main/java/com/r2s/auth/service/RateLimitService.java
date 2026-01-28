package com.r2s.auth.service;

import java.time.Duration;

public interface RateLimitService {
    boolean isBlocked(String baseKey);

    void onFailure(String baseKey, int maxAttempts, Duration ttl);

    void onSuccess(String baseKey);
}

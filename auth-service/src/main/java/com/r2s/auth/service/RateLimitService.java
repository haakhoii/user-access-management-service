package com.r2s.auth.service;

import java.time.Duration;

public interface RateLimitService {
    void checkAndConsume(String baseKey, int max, Duration ttl);
}

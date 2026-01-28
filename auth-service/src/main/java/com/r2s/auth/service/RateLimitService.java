package com.r2s.auth.service;

import java.time.Duration;

public interface RateLimitService {
    void check(String type, String value, int maxAttempts, Duration blockDuration);
    void reset(String type, String value);
}
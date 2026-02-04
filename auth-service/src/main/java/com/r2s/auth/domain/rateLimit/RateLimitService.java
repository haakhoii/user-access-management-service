package com.r2s.auth.domain.rateLimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, Object> redis;
    private final RateLimitRedisKey redisKey;
    private static final String BLOCKED = "1";

    public boolean checkAndConsume(String baseKey, int max, Duration ttl, Duration block) {
        if (Boolean.TRUE.equals(redis.hasKey(redisKey.blocked(baseKey)))) {
            return false;
        }
        Long count = redis.opsForValue().increment(baseKey);
        if (count != null && count == 1) {
            redis.expire(baseKey, ttl);
        }
        if (count != null && count > max) {
            redis.opsForValue().set(
                    redisKey.blocked(baseKey),
                    BLOCKED,
                    block
            );
            return false;
        }
        return true;
    }
}


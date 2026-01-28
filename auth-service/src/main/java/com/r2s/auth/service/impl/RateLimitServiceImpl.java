package com.r2s.auth.service.impl;

import com.r2s.auth.domain.rateLimit.RateLimitRedisKey;
import com.r2s.auth.service.RateLimitService;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final RedisTemplate<String, Object> redis;
    private final RateLimitRedisKey redisKey;

    @Override
    public void checkAndConsume(String baseKey, int max, Duration ttl) {
        if (Boolean.TRUE.equals(redis.hasKey(redisKey.blocked(baseKey)))) {
            throw new AppException(ErrorCode.TOO_MANY_REQUEST);
        }
        Long count = redis.opsForValue().increment(baseKey);

        if (count != null && count == 1) {
            redis.expire(baseKey, ttl);
        }
        if (count != null && count > max) {
            redis.opsForValue().set(
                    redisKey.blocked(baseKey),
                    "1",
                    ttl
            );
            throw new AppException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}

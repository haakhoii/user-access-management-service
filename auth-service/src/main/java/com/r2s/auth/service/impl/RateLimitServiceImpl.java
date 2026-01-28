package com.r2s.auth.service.impl;

import com.r2s.auth.domain.rateLimit.RateLimitRedisKey;
import com.r2s.auth.service.RateLimitService;
import com.r2s.core.constants.RateLimitType;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final RedisTemplate<String, Object> redis;
    private final RateLimitRedisKey redisKey;

    @Override
    public boolean isBlocked(String baseKey) {
        return Boolean.TRUE.equals(redis.hasKey(redisKey.blocked(baseKey)));
    }

    @Override
    public void onFailure(String baseKey, int maxAttempts, Duration ttl) {
        Long attempts = redis.opsForValue().increment(baseKey);

        if (attempts != null && attempts == 1) {
            redis.expire(baseKey, ttl);
        }

        if (attempts != null && attempts >= maxAttempts) {
            redis.opsForValue().set(
                    redisKey.blocked(baseKey),
                    "1",
                    ttl
            );
            throw new AppException(ErrorCode.TOO_MANY_REQUEST);
        }
    }

    @Override
    public void onSuccess(String baseKey) {
        redis.delete(baseKey);
        redis.delete(redisKey.blocked(baseKey));
    }
}

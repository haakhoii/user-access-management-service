package com.r2s.auth.service.impl;

import com.r2s.auth.domain.rateLimit.RateLimitRedisKey;
import com.r2s.auth.service.RateLimitService;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitServiceImpl implements RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RateLimitRedisKey redisKey;

    @Override
    public void check(
            String type,
            String value,
            int maxAttempts,
            Duration duration
    ) {
        String key = redisKey.build(type, value);

        Long attempts = redisTemplate.opsForValue().increment(key);
        log.info("RATE_LIMIT type={}, key={}, attempts={}", type, key, attempts);

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, duration);
        }

        if (attempts != null && attempts > maxAttempts) {
            throw new AppException(ErrorCode.TOO_MANY_REQUEST);
        }
    }

    @Override
    public void reset(String type, String value) {
        redisTemplate.delete(redisKey.build(type, value));
    }
}

package com.r2s.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.auth.domain.rateLimit.ClientKeyResolver;
import com.r2s.auth.domain.rateLimit.RateLimitRedisKey;
import com.r2s.auth.domain.rateLimit.RateLimitService;
import com.r2s.core.constants.RateLimitType;
import com.r2s.core.dto.ApiResponse;
import com.r2s.core.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitBlockFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitRedisKey redisKey;
    private final ClientKeyResolver clientKeyResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        String clientKey = clientKeyResolver.resolve(request);

        String baseKey = redisKey.base(
                request.getMethod(),
                request.getRequestURI(),
                clientKey
        );

        boolean allowed = rateLimitService.checkAndConsume(
                baseKey,
                RateLimitType.ATTEMPTS,
                Duration.ofMinutes(RateLimitType.TIME_TO_LIVE)
        );

        if (!allowed) {
            response.setStatus(429);
            response.setContentType("application/json");

            ApiResponse<?> apiResponse = ApiResponse.builder()
                    .code(ErrorCode.TOO_MANY_REQUEST.getCode())
                    .message(ErrorCode.TOO_MANY_REQUEST.getMessage())
                    .build();

            response.getWriter().write(
                    objectMapper.writeValueAsString(apiResponse)
            );
            response.flushBuffer();
            return;
        }

        chain.doFilter(request, response);
    }
}



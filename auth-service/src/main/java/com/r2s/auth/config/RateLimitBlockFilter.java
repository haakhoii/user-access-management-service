package com.r2s.auth.config;

import com.r2s.auth.domain.rateLimit.ClientKeyResolver;
import com.r2s.auth.domain.rateLimit.RateLimitRedisKey;
import com.r2s.auth.service.RateLimitService;
import com.r2s.core.constants.RateLimitType;
import com.r2s.core.exception.AppException;
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

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        String path = request.getRequestURI();
        String baseKey = redisKey.base(
                request.getMethod(),
                path,
                clientKeyResolver.resolve(request)
        );
        try {
            rateLimitService.checkAndConsume(
                    baseKey,
                    RateLimitType.ATTEMPTS,
                    Duration.ofMinutes(RateLimitType.TIME_TO_LIVE)
            );

            chain.doFilter(request, response);

        } catch (AppException ex) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("code: 429, message: Too many requests. Please try again later.");
        }
    }
}




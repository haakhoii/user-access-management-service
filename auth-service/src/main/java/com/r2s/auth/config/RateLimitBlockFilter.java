package com.r2s.auth.config;

import com.r2s.auth.domain.rateLimit.ClientKeyResolver;
import com.r2s.auth.domain.rateLimit.RateLimitRedisKey;
import com.r2s.auth.service.RateLimitService;
import com.r2s.core.constants.RateLimitType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RateLimitBlockFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ClientKeyResolver clientKeyResolver;
    private final RateLimitRedisKey redisKey;

    private static final Set<String> PUBLIC = Set.of(
            "/auth/login",
            "/auth/register"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        String path = request.getRequestURI();

        if (PUBLIC.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        String baseKey = redisKey.base(
                request.getMethod(),
                path,
                clientKeyResolver.resolve(request)
        );

        if (rateLimitService.isBlocked(baseKey)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("""
                { "code": 429, "message": "Too many requests" }
            """);
            return;
        }

        try {
            chain.doFilter(request, response);
        } catch (Exception ex) {
            rateLimitService.onFailure(
                    baseKey,
                    RateLimitType.ATTEMPTS,
                    Duration.ofMinutes(RateLimitType.TIME_TO_LIVE)
            );
            throw ex;
        }
    }
}

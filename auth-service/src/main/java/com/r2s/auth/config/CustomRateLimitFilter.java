package com.r2s.auth.config;

import com.r2s.auth.service.RateLimitService;
import com.r2s.core.constants.RateLimitType;
import com.r2s.core.dto.response.RateLimitRule;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    private static final Map<String, RateLimitRule> RULES = Map.of(
            "/auth/introspect",
            RateLimitRule.builder()
                    .type(RateLimitType.INTROSPECT)
                    .maxAttempts(RateLimitType.ATTEMPTS)
                    .duration(Duration.ofMinutes(RateLimitType.TIME_TO_LIVE))
                    .build(),

            "/auth/me",
            RateLimitRule.builder()
                    .type(RateLimitType.GET_ME)
                    .maxAttempts(RateLimitType.ATTEMPTS)
                    .duration(Duration.ofMinutes(RateLimitType.TIME_TO_LIVE))
                    .build()
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();
        RateLimitRule rule = RULES.get(uri);
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = resolveToken(request);
        if (token != null) {
            try {
                rateLimitService.check(
                        rule.getType(),
                        token,
                        rule.getMaxAttempts(),
                        rule.getDuration()
                );
            } catch (AppException ex) {
                ErrorCode errorCode = ex.getErrorCode();

                response.setStatus(errorCode.getStatus().value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("""
                        {
                          "code": %d,
                          "message": "%s"
                        }
                        """.formatted(
                        errorCode.getCode(),
                        errorCode.getMessage()
                ));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}

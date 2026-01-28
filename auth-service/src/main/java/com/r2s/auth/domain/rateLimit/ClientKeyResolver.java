package com.r2s.auth.domain.rateLimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientKeyResolver {

    public String resolve(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return "token:" + authHeader.substring(7).hashCode();
        }
        return "ip:" + request.getRemoteAddr();
    }
}

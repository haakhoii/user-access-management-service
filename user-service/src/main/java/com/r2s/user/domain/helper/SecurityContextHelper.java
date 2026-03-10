package com.r2s.user.domain.helper;

import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class SecurityContextHelper {
    private JwtAuthenticationToken getJwtAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwt)) {
            log.error("Authentication is not JWT token");
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return jwt;
    }

    public UUID getCurrentUserId() {
        JwtAuthenticationToken jwt = getJwtAuthentication();
        return UUID.fromString(jwt.getName());
    }

    public String getCurrentUsername() {
        JwtAuthenticationToken jwt = getJwtAuthentication();
        return jwt.getToken().getClaimAsString("username");
    }

    public String getCurrentRole() {
        JwtAuthenticationToken jwt = getJwtAuthentication();
        var roles = jwt.getToken().getClaimAsStringList("role");

        if (roles == null || roles.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return roles.get(0);
    }
}

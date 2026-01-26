package com.r2s.user.domain.helper;

import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
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

    public List<String> getCurrentRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof JwtAuthenticationToken jwt)) {
            return List.of();
        }

        return jwt.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

}

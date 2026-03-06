package com.r2s.auth.domain.role;

import com.r2s.auth.entity.Role;
import com.r2s.auth.repository.UserRoleRepository;
import com.r2s.core.constants.RoleConstants;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserRoleAssigner {

    UserRoleRepository roleRepository;

    public Role assign(RegisterRequest request) {   // 🔥 đổi return type

        String roleName = resolveRole(request.getRole());

        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
    }

    private String resolveRole(String inputRole) {

        if (inputRole == null || inputRole.isBlank()) {
            return RoleConstants.ROLE_USER;
        }

        String role = inputRole.trim().toUpperCase();

        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        return switch (role) {
            case "USER" -> RoleConstants.ROLE_USER;
            case "ADMIN" -> RoleConstants.ROLE_ADMIN;
            case "MODERATOR" -> RoleConstants.ROLE_MODERATOR;
            default -> throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        };
    }
}
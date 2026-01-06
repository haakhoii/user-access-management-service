package com.r2s.auth_service.mapper;

import com.r2s.auth_service.entity.Auth;
import com.r2s.core_service.dto.request.RegisterRequest;
import com.r2s.core_service.dto.response.AccountResponse;

public class AuthMapper {
    public static Auth toAuth(RegisterRequest request) {
        return Auth.builder()
                .username(request.getUsername())
                .build();
    }

    public static AccountResponse toAccountResponse(Auth auth) {
        return AccountResponse.builder()
                .id(auth.getId())
                .username(auth.getUsername())
                .role(auth.getRole().name())
                .build();
    }
}

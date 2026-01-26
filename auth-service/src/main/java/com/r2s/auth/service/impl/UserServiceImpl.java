package com.r2s.auth.service.impl;

import com.r2s.auth.domain.role.RoleNormalizerResolver;
import com.r2s.auth.domain.validation.user.UserValidation;
import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.auth.domain.factory.UserFactory;
import com.r2s.auth.mapper.UserMapper;
import com.r2s.auth.repository.UserRepository;
import com.r2s.auth.repository.UserRoleRepository;
import com.r2s.auth.service.UserService;
import com.r2s.core.constants.RoleConstants;
import com.r2s.core.dto.request.RegisterRequest;
import com.r2s.core.dto.response.UserResponse;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserRoleRepository userRoleRepository;
    PasswordEncoder passwordEncoder;
    UserFactory userFactory;
    RoleNormalizerResolver roleNormalizerResolver;
    UserValidation userValidation;

    private Set<Role> assignRoles(RegisterRequest request) {
        Set<Role> roles = new HashSet<>();
        Role userRole = userRoleRepository.findByName(RoleConstants.ROLE_USER)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        roles.add(userRole);
        String normalizedRole = roleNormalizerResolver.normalize(request.getRole());
        if (normalizedRole != null) {
            Role role = userRoleRepository.findByName(normalizedRole)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            roles.add(role);
        }

        return roles;
    }

    @Override
    public String register(RegisterRequest request) {
        userValidation.validateRegister(request);
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Set<Role> roles = assignRoles(request);
        User user = userFactory.create(request, roles, encodedPassword);
        userRepository.save(user);
        log.info("User created with username: {}", user.getUsername());

        return "User created with id = " + user.getId();
    }

    @Override
    public UserResponse getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(auth.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        log.info("Get user successfully: {}", user);
        return UserMapper.toUserResponse(user);
    }

}
package com.r2s.auth.service.impl;

import com.r2s.auth.domain.helper.SecurityContextHelper;
import com.r2s.auth.domain.role.RoleNormalizerResolver;
import com.r2s.auth.domain.role.UserRoleAssigner;
import com.r2s.auth.domain.validation.user.UserValidation;
import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.auth.domain.factory.UserFactory;
import com.r2s.auth.mapper.UserMapper;
import com.r2s.auth.repository.UserRepository;
import com.r2s.auth.repository.UserRoleRepository;
import com.r2s.auth.service.UserQueryService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserFactory userFactory;
    UserValidation userValidation;
    UserRoleAssigner roleAssigner;
    SecurityContextHelper securityContextHelper;
    UserQueryService userQueryService;

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        userValidation.validateRegister(request);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Set<Role> roles = roleAssigner.assign(request);
        User user = userFactory.create(request, roles, encodedPassword);
        userRepository.save(user);
        log.info("User registered id: {}", user.getId());

        return "User created with id: " + user.getId();
    }

    @Override
    public UserResponse getMe() {
        UUID userId = securityContextHelper.getCurrentUserId();
        User user = userQueryService.getById(userId);
        return UserMapper.toUserResponse(user);
    }
}

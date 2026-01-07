package com.r2s.user_service.service;

import com.r2s.core_service.dto.request.UserCreationRequest;
import com.r2s.core_service.dto.request.UserUpdatedRequest;
import com.r2s.core_service.dto.response.PageResponse;
import com.r2s.core_service.dto.response.UserResponse;
import com.r2s.core_service.exception.AppException;
import com.r2s.core_service.exception.ErrorCode;
import com.r2s.user_service.entity.User;
import com.r2s.user_service.mapper.UserMapper;
import com.r2s.user_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;

    private String getAuthUserId() {
        if (SecurityContextHolder.getContext() == null ||
                SecurityContextHolder.getContext().getAuthentication() == null ||
                SecurityContextHolder.getContext().getAuthentication().getName() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    public UserResponse create(UserCreationRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        String authUserId = getAuthUserId();
        User user = User.builder()
                .authUserId(authUserId)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .build();

        userRepository.save(user);
        return UserMapper.toUserResponse(user);
    }

    public PageResponse<UserResponse> getList(int page, int size) {
        if (page <= 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(UserMapper::toUserResponse)
                .toList();

        return PageResponse.<UserResponse>builder()
                .currentPage(page)
                .totalPages(userPage.getTotalPages())
                .pageSize(size)
                .totalElements(userPage.getTotalElements())
                .data(userResponses)
                .build();
    }

    public UserResponse getMe() {
        String authUserId = getAuthUserId();
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserMapper.toUserResponse(user);
    }

    public UserResponse update(UserUpdatedRequest request) {
        String authUserId = getAuthUserId();
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        userRepository.save(user);

        return UserMapper.toUserResponse(user);
    }

    public String delete(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);

        return "User deleted successfully with userId = " + userId;
    }
}

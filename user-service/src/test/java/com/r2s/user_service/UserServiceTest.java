package com.r2s.user_service;
import com.r2s.core_service.dto.request.UserCreationRequest;
import com.r2s.core_service.dto.request.UserUpdatedRequest;
import com.r2s.core_service.dto.response.PageResponse;
import com.r2s.core_service.dto.response.UserResponse;
import com.r2s.core_service.exception.AppException;
import com.r2s.core_service.exception.ErrorCode;
import com.r2s.user_service.entity.User;
import com.r2s.user_service.repository.UserRepository;
import com.r2s.user_service.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    private void mockAuthenticatedUser(String authUserId) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName())
                .thenReturn(authUserId);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void create_null_request() {
        AppException ex = assertThrows(
                AppException.class,
                () -> userService.create(null)
        );
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_without_auth() {
        SecurityContextHolder.clearContext();

        UserCreationRequest request = UserCreationRequest.builder()
                .fullName("user01")
                .email("user01@gmail.com")
                .build();

        AppException ex = assertThrows(
                AppException.class, 
                () -> userService.create(request)
        );
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    void create_success() {
        mockAuthenticatedUser("auth-user-id");

        UserCreationRequest request = UserCreationRequest.builder()
                .fullName("user01")
                .email("user01@gmail.com")
                .build();

        User savedUser = User.builder()
                .authUserId("auth-user-id")
                .fullName("user01")
                .email("user01@gmail.com")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.create(request);

        assertEquals("user01", response.getFullName());
        assertEquals("user01@gmail.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getList_invalid_page() {
        AppException ex1 = assertThrows(
                AppException.class, 
                () -> userService.getList(0, 1)
        );
        assertEquals(ErrorCode.INVALID_REQUEST, ex1.getErrorCode());

        AppException ex2 = assertThrows(
                AppException.class, 
                () -> userService.getList(1, 0)
        );
        assertEquals(ErrorCode.INVALID_REQUEST, ex2.getErrorCode());
    }

    @Test
    void getList_success() {
        User user1 = User.builder()
                .fullName("A")
                .email("a@gmail.com")
                .build();
        User user2 = User.builder()
                .fullName("B")
                .email("b@gmail.com")
                .build();
        List<User> users = List.of(user1, user2);

        Pageable pageable = PageRequest.of(0, 2);
        Page<User> page = new PageImpl<>(users, pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(page);

        PageResponse<UserResponse> response = userService.getList(1, 2);

        assertEquals(1, response.getCurrentPage());
        assertEquals(2, response.getPageSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(2, response.getData().size());
    }

    @Test
    void getMe_not_found() {
        mockAuthenticatedUser("user-123");
        when(userRepository.findByAuthUserId("user-123")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> userService.getMe());
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getMe_success() {
        mockAuthenticatedUser("user-123");

        User user = User.builder()
                .authUserId("user-123")
                .fullName("user01")
                .email("user01@gmail.com")
                .build();
        
        when(userRepository.findByAuthUserId("user-123")).thenReturn(Optional.of(user));

        UserResponse response = userService.getMe();

        assertEquals("user01", response.getFullName());
        assertEquals("user01@gmail.com", response.getEmail());
    }

    @Test
    void update_user_not_found() {
        mockAuthenticatedUser("user-1");
        when(userRepository.findByAuthUserId("user-1"))
                .thenReturn(Optional.empty());

        UserUpdatedRequest request = new UserUpdatedRequest();
        request.setFullName("Updated");

        AppException ex = assertThrows(
                AppException.class,
                () -> userService.update(request)
        );
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void update_success() {
        mockAuthenticatedUser("user-1");

        UserUpdatedRequest request = new UserUpdatedRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@gmail.com");

        User user = User.builder()
                .authUserId("user-1")
                .fullName("Old")
                .email("old@gmail.com")
                .build();
        when(userRepository.findByAuthUserId("user-1"))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.update(request);

        assertEquals("Updated Name", response.getFullName());
        assertEquals("updated@gmail.com", response.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void delete_user_not_found() {
        when(userRepository.findById("u1"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(
                AppException.class, 
                () -> userService.delete("u1")
        );
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void delete_invalid_id() {
        AppException ex1 = assertThrows(
                AppException.class, 
                () -> userService.delete(null)
        );
        assertEquals(ErrorCode.INVALID_REQUEST, ex1.getErrorCode());

        AppException ex2 = assertThrows(
                AppException.class, 
                () -> userService.delete(" ")
        );
        assertEquals(ErrorCode.INVALID_REQUEST, ex2.getErrorCode());
    }

    @Test
    void delete_success() {
        User user = User.builder()
                .fullName("user01")
                .email("user01@gmail.com")
                .build();
        when(userRepository.findById("u1"))
                .thenReturn(Optional.of(user));

        String result = userService.delete("u1");
        assertTrue(result.contains("User deleted successfully"));
        verify(userRepository).delete(user);
    }
    
}

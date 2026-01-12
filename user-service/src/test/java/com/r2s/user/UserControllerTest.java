package com.r2s.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.core.dto.request.UserUpdatedRequest;
import com.r2s.core.dto.response.PageResponse;
import com.r2s.core.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user")
    void create_success() throws Exception {
        UserCreationRequest request = UserCreationRequest.builder()
                .fullName("user01")
                .email("user01@gmail.com")
                .build();

        UserResponse response = UserResponse.builder()
                .fullName("user01")
                .email("user01@gmail.com")
                .build();

        when(userService.create(any())).thenReturn(response);

        mockMvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.fullName").value("user01"))
                .andExpect(jsonPath("$.result.email").value("user01@gmail.com"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getList_success() throws Exception {
        UserResponse user1 = UserResponse.builder().fullName("A").email("a@gmail.com").build();
        UserResponse user2 = UserResponse.builder().fullName("B").email("b@gmail.com").build();

        PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                .currentPage(1)
                .pageSize(2)
                .totalPages(1)
                .totalElements(2)
                .data(List.of(user1, user2))
                .build();

        when(userService.getList(1, 2)).thenReturn(pageResponse);

        mockMvc.perform(get("/list")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data[0].fullName").value("A"))
                .andExpect(jsonPath("$.result.data[1].fullName").value("B"));
    }

    @Test
    @WithMockUser(username = "user")
    void getMe_success() throws Exception {
        UserResponse response = UserResponse.builder()
                .fullName("John Doe")
                .email("john@gmail.com")
                .build();

        when(userService.getMe()).thenReturn(response);

        mockMvc.perform(get("/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.fullName").value("John Doe"))
                .andExpect(jsonPath("$.result.email").value("john@gmail.com"));
    }

    @Test
    @WithMockUser(username = "user")
    void update_success() throws Exception {
        UserUpdatedRequest request = new UserUpdatedRequest();
        request.setFullName("new full name");
        request.setEmail("newEmail@gmail.com");

        UserResponse response = UserResponse.builder()
                .fullName("new full name")
                .email("newEmail@gmail.com")
                .build();

        when(userService.update(any())).thenReturn(response);

        mockMvc.perform(put("/upd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.fullName").value("new full name"))
                .andExpect(jsonPath("$.result.email").value("newEmail@gmail.com"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void delete_success() throws Exception {
        when(userService.delete("u1")).thenReturn("User deleted successfully");

        mockMvc.perform(delete("/u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("User deleted successfully"));
    }
}

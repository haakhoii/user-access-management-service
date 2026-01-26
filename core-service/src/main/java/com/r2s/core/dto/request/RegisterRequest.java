package com.r2s.core.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @NotBlank(message = "username must not be blank")
    @Size(min = 3, max = 20, message = "username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "password must not be blank")
    @Size(min = 3, max = 20, message = "password must be between 3 and 20 characters")
    private String password;

    private String role;
}

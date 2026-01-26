package com.r2s.core.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdatedRequest {
    @Size(min = 3, max = 30, message = "full name must be between 3 and 20 characters")
    String fullName;

    @Email
    String email;

    @Pattern(regexp = "^[0-9]{9,15}$")
    String phone;

    @Size(min = 3, max = 30, message = "address must be between 3 and 20 characters")
    String address;

    String avatarUrl;
}

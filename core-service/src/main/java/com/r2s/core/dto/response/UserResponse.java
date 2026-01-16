package com.r2s.core.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.management.relation.Role;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String username;

    List<String> role;

    String fullName;

    String email;
}

package com.r2s.core.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Duration;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RateLimitRule {
    String type;
    int maxAttempts;
    Duration duration;
}

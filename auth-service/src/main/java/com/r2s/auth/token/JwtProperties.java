package com.r2s.auth.token;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "jwt")
@Validated
@Data
public class JwtProperties {
    @NotBlank
    private String signerKey;

    @Min(1)
    private long expiry;
}


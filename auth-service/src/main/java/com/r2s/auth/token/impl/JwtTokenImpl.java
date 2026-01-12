package com.r2s.auth.token.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.r2s.auth.token.JwtToken;
import com.r2s.core.dto.response.AuthResponse;
import com.r2s.core.entity.User;
import com.r2s.core.exception.AppException;
import com.r2s.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenImpl implements JwtToken {

    private final JwtDecoder jwtDecoder;

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.expiry}")
    private long EXPIRY;

    @Override
    public AuthResponse generateToken(User user) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getId())
                    .issuer("r2s")
                    .issueTime(Date.from(Instant.now()))
                    .expirationTime(Date.from(Instant.now().plus(EXPIRY, ChronoUnit.MINUTES)))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("username", user.getUsername())
                    .claim("scope", "ROLE_" + user.getRole().name())
                    .build();

            Payload payload = new Payload(claims.toJSONObject());
            JWSObject object = new JWSObject(header, payload);

            object.sign(new MACSigner(SIGNER_KEY.getBytes()));

            return new AuthResponse(
                    object.serialize()
            );
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.TOKEN_GENERATION_FAILED);
        }

    }

    @Override
    public Jwt verify(String token) {
        return jwtDecoder.decode(token);
    }
}
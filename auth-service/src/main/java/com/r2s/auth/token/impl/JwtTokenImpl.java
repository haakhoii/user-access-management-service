package com.r2s.auth.token.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.auth.token.JwtToken;
import com.r2s.core.dto.response.TokenResponse;
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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenImpl implements JwtToken {

    private final JwtDecoder jwtDecoder;

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.expiry}")
    private long EXPIRY;

    @Override
    public TokenResponse generateToken(User user) {
        try {
//            String role = user.getRoles()
//                    .stream()
//                    .map(Role::getName)
//                    .collect(Collectors.joining(" "));

            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getId().toString())
                    .issuer("r2s")
                    .issueTime(Date.from(Instant.now()))
                    .expirationTime(Date.from(Instant.now().plus(EXPIRY, ChronoUnit.MINUTES)))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("username", user.getUsername())
                    .claim("roles",
                            user.getRoles()
                                    .stream()
                                    .map(Role::getName)
                                    .toList()
                    )
                    .build();

            Payload payload = new Payload(claims.toJSONObject());
            JWSObject object = new JWSObject(header, payload);

            object.sign(new MACSigner(SIGNER_KEY.getBytes()));

            return new TokenResponse(
                    object.serialize()
            );
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.TOKEN_GENERATION_FAILED);
        }
    }

}


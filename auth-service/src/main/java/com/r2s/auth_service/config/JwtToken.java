package com.r2s.auth_service.config;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;
import com.r2s.auth_service.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtToken {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.expiry}")
    private long EXPIRY;


    public String generateToken(User user) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getId())
                    .issuer("r2s")
                    .issueTime(Date.from(Instant.now()))
                    .expirationTime(Date.from(Instant.now().plus(EXPIRY, ChronoUnit.MINUTES)))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("username", user.getUsername())
                    .claim("scope", "ROLE_" + user.getRole())
                    .build();

            Payload payload = new Payload(claims.toJSONObject());
            JWSObject object = new JWSObject(header, payload);

            object.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return object.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public Instant generateExpiry() {
        return Instant.now().plus(EXPIRY, ChronoUnit.MINUTES);
    }
}

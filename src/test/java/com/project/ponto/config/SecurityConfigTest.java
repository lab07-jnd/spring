package com.project.ponto.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;


class SecurityConfigTest {

    private static final String SECRET = "test-secret-with-more-than-32-characters";
    private static final String OTHER_SECRET = "a-completely-different-secret-123456";

    private final JwtDecoder decoder = new SecurityConfig().jwtDecoder(SECRET);

    @Test
    void mustAcceptValidTokenAndExposeUserId() throws Exception {
        String token = signed(SECRET, "123", Instant.now().plusSeconds(3600));

        Jwt jwt = decoder.decode(token);

        assertEquals("123", jwt.getClaimAsString("id"));
    }

    @Test
    void mustRejectWhenTokenIsExpired() throws Exception {
        String token = signed(SECRET, "123", Instant.now().minusSeconds(3600));

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void mustRejectWhenSignedWithDifferentSecret() throws Exception {
        String token = signed(OTHER_SECRET, "123", Instant.now().plusSeconds(3600));

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void mustRejectWhenTokenIsUnsignedWithAlgNone() {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("id", "123")
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .build();
        String token = new PlainJWT(claims).serialize();

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void mustRejectWhenIdClaimIsMissing() throws Exception {
        String token = signed(SECRET, null, Instant.now().plusSeconds(3600));

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void mustRejectWhenIdClaimIsNotNumeric() throws Exception {
        String token = signed(SECRET, "abc123", Instant.now().plusSeconds(3600));

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void mustRejectWhenTextIsNotAJwt() {
        assertThrows(JwtException.class, () -> decoder.decode("this-is-not-a-jwt"));
    }

    private String signed(String secret, String id, Instant exp) throws Exception {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .issueTime(new Date())
                .expirationTime(Date.from(exp));
        if (id != null) {
            claims.claim("id", id);
        }
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
        jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }
}
package com.project.ponto.util;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Generates valid, signed JWT tokens for use in integration tests.
 *
 * Uses the same secret and algorithm (HS256) configured in
 * src/test/resources/application.properties, so the full security pipeline
 * (BearerTokenAuthenticationFilter → JwtDecoder → JwtAuthenticationConverter)
 * is exercised exactly as in production.
 *
 * The nimbus-jose-jwt dependency is already on the test classpath as a
 * transitive dependency of spring-boot-starter-oauth2-resource-server.
 */
public final class JwtTestUtils {

    /** Must match jwt.secret in src/test/resources/application.properties */
    private static final String TEST_SECRET = "test-secret-with-more-than-32-characters";

    private JwtTestUtils() {}

    /**
     * Returns a signed JWT string valid for 1 hour, carrying the custom
     * {@code id} claim required by SecurityConfig#userIdValidator().
     *
     * @param userId numeric user identifier to place in the "id" claim
     */
    public static String validToken(String userId) {
        return tokenWithIdClaim(userId);
    }

    /**
     * Returns a signed JWT valid for 1 hour but without the {@code id} claim,
     * which must be rejected by {@link com.project.ponto.config.SecurityConfig}.
     */
    public static String tokenWithoutId() {
        return tokenWithIdClaim(null);
    }

    private static String tokenWithIdClaim(String userId) {
        try {
            JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plusSeconds(3600)));
            if (userId != null) {
                claims.claim("id", userId);
            }

            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
            jwt.sign(new MACSigner(TEST_SECRET.getBytes(StandardCharsets.UTF_8)));
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate test JWT", e);
        }
    }
}

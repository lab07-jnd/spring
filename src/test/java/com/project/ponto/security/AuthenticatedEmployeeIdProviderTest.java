package com.project.ponto.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AuthenticatedEmployeeIdProviderTest {

    private final AuthenticatedEmployeeIdProvider provider = new AuthenticatedEmployeeIdProvider();

    @Test
    void mustReturnNumericIdFromJwtClaim() {
        assertThat(provider.getEmployeeId(authentication("42"))).isEqualTo(42L);
    }

    @Test
    void mustThrowWhenIdClaimIsMissing() {
        assertThatThrownBy(() -> provider.getEmployeeId(authentication(null)))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("'id' claim");
    }

    @Test
    void mustThrowWhenIdClaimIsNotNumeric() {
        assertThatThrownBy(() -> provider.getEmployeeId(authentication("abc")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("must be numeric");
    }

    @Test
    void mustThrowWhenAuthenticationIsNotJwt() {
        var authentication = new UsernamePasswordAuthenticationToken("user", "password");

        assertThatThrownBy(() -> provider.getEmployeeId(authentication))
                .isInstanceOf(InsufficientAuthenticationException.class);
    }

    private static JwtAuthenticationToken authentication(String id) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "none");
        if (id != null) {
            builder.claim("id", id);
        } else {
            builder.subject("employee-without-id");
        }
        return new JwtAuthenticationToken(builder.build());
    }
}

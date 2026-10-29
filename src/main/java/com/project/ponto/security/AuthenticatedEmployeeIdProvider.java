package com.project.ponto.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.project.ponto.config.SecurityConfig;

@Component
public class AuthenticatedEmployeeIdProvider {

    public Long getEmployeeId(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new InsufficientAuthenticationException("JWT authentication is required");
        }

        String id = jwtAuthentication.getToken().getClaimAsString(SecurityConfig.USER_ID_CLAIM);
        if (id == null || id.isBlank()) {
            throw new BadCredentialsException("JWT is missing the 'id' claim");
        }

        try {
            return Long.valueOf(id);
        } catch (NumberFormatException ex) {
            throw new BadCredentialsException("JWT 'id' claim must be numeric", ex);
        }
    }
}

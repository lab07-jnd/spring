package com.project.ponto.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public record Email(

        @Column(name = "email", nullable = false, length = 254)
        String value

) {

    private static final Pattern EMAIL_REGEX = Pattern.compile(
            """
            ^(?<local>[A-Za-z0-9._%+\\-]+)\
            @\
            (?<domain>(?:[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,})$
            """,
            Pattern.COMMENTS
    );

    private static final int MAX_TOTAL_LENGTH = 254;
    private static final int MAX_LOCAL_LENGTH = 64;

    public Email {
        if (value == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        String normalized = value.strip().toLowerCase();

        validateFormat(normalized);
        validateTotalLength(normalized);

        String local = extractLocal(normalized);
        validateLocalLength(local);
        validateLocalDots(local);

        value = normalized;
    }

    // ----- parsing helpers -----

    private static String extractLocal(String email) {
        return email.substring(0, email.indexOf('@'));
    }

    private static String extractDomain(String email) {
        return email.substring(email.indexOf('@') + 1);
    }

    // ----- private validations -----

    private static void validateFormat(String email) {
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!EMAIL_REGEX.matcher(email).matches()) {
            throw new IllegalArgumentException(
                    "Invalid email: %s. Expected format: user@domain.com".formatted(email)
            );
        }
    }

    private static void validateTotalLength(String email) {
        if (email.length() > MAX_TOTAL_LENGTH) {
            throw new IllegalArgumentException(
                    "Email exceeds the limit of 254 characters, received " + email.length()
            );
        }
    }

    private static void validateLocalLength(String local) {
        if (local.length() > MAX_LOCAL_LENGTH) {
            throw new IllegalArgumentException(
                    "Local part of the email exceeds the limit of 64 characters, received "
                            + local.length()
            );
        }
    }

    private static void validateLocalDots(String local) {
        if (local.contains("..") || local.startsWith(".") || local.endsWith(".")) {
            throw new IllegalArgumentException(
                    "Local part of the email cannot contain consecutive dots, "
                            + "nor start/end with a dot: " + local
            );
        }
    }

    /** Normalized email (lower case, no spaces). */
    public String raw() {
        return value;
    }

    /** Canonical display form — same normalized value. */
    public String formatted() {
        return value;
    }

    public String localPart() {
        return extractLocal(value);
    }

    public String domain() {
        return extractDomain(value);
    }

    /** Validates without throwing an exception. */
    public static boolean isValid(String emailAddress) {
        try {
            new Email(emailAddress);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return value;
    }
}

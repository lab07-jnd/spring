package com.project.ponto.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record CPF(

        @Column(name = "cpf", nullable = false, length = 11)
        String value

) {

    private static final int DIGIT_COUNT = 11;

    /** Compact constructor: normalizes and validates before assignment. */
    public CPF {
        if (value == null) {
            throw new IllegalArgumentException("CPF cannot be null");
        }

        String digits = value.replaceAll("\\D", "");

        if (digits.length() != DIGIT_COUNT) {
            throw new IllegalArgumentException(
                    "CPF must contain 11 digits: %s. Format: XXX.XXX.XXX-XX".formatted(value)
            );
        }

        if (!validateCheckDigits(digits)) {
            throw new IllegalArgumentException("CPF with invalid check digits");
        }

        value = digits;
    }

    private static boolean validateCheckDigits(String cpf) {
        // Rejects repeated sequences: 000..., 111..., etc.
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int first = calculateDigit(cpf, 9, 10);
        int second = calculateDigit(cpf, 10, 11);

        return (cpf.charAt(9)  - '0') == first
                && (cpf.charAt(10) - '0') == second;
    }

    private static int calculateDigit(String cpf, int limit, int initialWeight) {
        int sum = 0;
        for (int i = 0; i < limit; i++) {
            sum += (cpf.charAt(i) - '0') * (initialWeight - i);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    /** CPF without formatting (digits only). */
    public String raw() {
        return value;
    }

    /** Formatted CPF: XXX.XXX.XXX-XX */
    public String formatted() {
        return "%s.%s.%s-%s".formatted(
                value.substring(0, 3),
                value.substring(3, 6),
                value.substring(6, 9),
                value.substring(9)
        );
    }

    /** Validates without throwing an exception. */
    public static boolean isValid(String cpf) {
        try {
            new CPF(cpf);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return formatted();
    }
}

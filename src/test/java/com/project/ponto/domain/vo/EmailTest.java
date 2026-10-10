package com.project.ponto.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Email Value Object Unit Tests")
class EmailTest {

    @Nested
    @DisplayName("1. Valid Construction and Normalization")
    class ValidConstruction {

        @ParameterizedTest
        @CsvSource({
                "'User@Domain.COM', 'user@domain.com', 'user', 'domain.com'",
                "' user@domain.com ', 'user@domain.com', 'user', 'domain.com'",
                "'first.last@company.com.br', 'first.last@company.com.br', 'first.last', 'company.com.br'",
                "'user+tag@gmail.com', 'user+tag@gmail.com', 'user+tag', 'gmail.com'",
                "'u_s%e-r@my-domain.com', 'u_s%e-r@my-domain.com', 'u_s%e-r', 'my-domain.com'",
                "'a@b.co', 'a@b.co', 'a', 'b.co'",
                "'user@domain.technology', 'user@domain.technology', 'user', 'domain.technology'"
        })
        @DisplayName("Must accept and normalize valid emails")
        void mustNormalizeValidInputs(String input, String expectedRaw, String expectedLocal, String expectedDomain) {
            Email email = new Email(input);

            assertThat(email.raw()).isEqualTo(expectedRaw);
            assertThat(email.formatted()).isEqualTo(expectedRaw);
            assertThat(email.localPart()).isEqualTo(expectedLocal);
            assertThat(email.domain()).isEqualTo(expectedDomain);
            assertThat(email.toString()).isEqualTo(expectedRaw);
        }
    }

    @Nested
    @DisplayName("2. Rejection of Invalid Inputs")
    class InvalidRejection {

        @Test
        @DisplayName("Must reject null email")
        void mustRejectWhenNull() {
            assertThatThrownBy(() -> new Email(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject empty email")
        void mustRejectWhenEmpty() {
            assertThatThrownBy(() -> new Email(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject email with spaces only")
        void mustRejectWhenSpacesOnly() {
            assertThatThrownBy(() -> new Email("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject email without @")
        void mustRejectWhenMissingAtSign() {
            assertThatThrownBy(() -> new Email("no-at-sign.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject email without local part")
        void mustRejectWhenMissingLocal() {
            assertThatThrownBy(() -> new Email("@domain.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject email without domain")
        void mustRejectWhenMissingDomain() {
            assertThatThrownBy(() -> new Email("user@"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject email without TLD")
        void mustRejectWhenMissingTld() {
            assertThatThrownBy(() -> new Email("user@domain"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject email with 1-letter TLD")
        void mustRejectWhenSingleLetterTld() {
            assertThatThrownBy(() -> new Email("user@domain.c"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject email with two @ signs")
        void mustRejectWhenTwoAtSigns() {
            assertThatThrownBy(() -> new Email("a@b@c.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject email with leading dot in local part")
        void mustRejectWhenLeadingDotInLocal() {
            assertThatThrownBy(() -> new Email(".user@domain.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject email with trailing dot in local part")
        void mustRejectWhenTrailingDotInLocal() {
            assertThatThrownBy(() -> new Email("user.@domain.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject email with consecutive dots in local part")
        void mustRejectWhenConsecutiveDots() {
            assertThatThrownBy(() -> new Email("us..er@domain.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject email with domain label starting with hyphen")
        void mustRejectWhenLabelStartsWithHyphen() {
            assertThatThrownBy(() -> new Email("user@-domain.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject email with domain label ending with hyphen")
        void mustRejectWhenLabelEndsWithHyphen() {
            assertThatThrownBy(() -> new Email("user@domain-.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("3. Length Limit Validation")
    class Limits {

        @Test
        @DisplayName("Must accept local part with exactly 64 characters")
        void mustAcceptLocalWith64Characters() {
            String localPart64 = "a".repeat(64);
            Email email = new Email(localPart64 + "@domain.com");
            assertThat(email.localPart()).hasSize(64);
        }

        @Test
        @DisplayName("Must reject local part with 65 characters")
        void mustRejectLocalWith65Characters() {
            String localPart65 = "a".repeat(65);
            assertThatThrownBy(() -> new Email(localPart65 + "@domain.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must accept email with total of exactly 254 characters")
        void mustAcceptTotalWith254Characters() {
            // "a".repeat(64) + "@" + "b".repeat(180) + ".com" = 64 + 1 + 180 + 4 = 249
            // Adjusting the domain to total 254:
            // Local (64) + "@" (1) + domain + ".com" (4) = 254 -> domain needs 254 - 64 - 1 - 4 = 185
            // Ex.: "b".repeat(60) + "." + "b".repeat(60) + "." + "b".repeat(61) + ".com" -> 60+1+60+1+61+4 = 187 domain characters
            String localPart64 = "a".repeat(64);
            String label1 = "b".repeat(60);
            String label2 = "c".repeat(60);
            String label3 = "d".repeat(63); // 60+1+60+1+63 = 185 -> + 4 (".com") = 189. 189 + 65 = 254
            String emailStr = localPart64 + "@" + label1 + "." + label2 + "." + label3 + ".com";

            Email email = new Email(emailStr);
            assertThat(email.raw()).hasSize(254);
        }

        @Test
        @DisplayName("Must reject email with total of 255 characters")
        void mustRejectTotalWith255Characters() {
            String localPart64 = "a".repeat(64);
            String label1 = "b".repeat(60);
            String label2 = "c".repeat(60);
            String label3 = "d".repeat(64); // +1 char
            String emailStr = localPart64 + "@" + label1 + "." + label2 + "." + label3 + ".com";

            assertThatThrownBy(() -> new Email(emailStr))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("4. Value Object Contract (Equals, HashCode and ToString)")
    class ValueObjectContract {

        @Test
        @DisplayName("Must be equal by value for instances built from equivalent inputs")
        void mustBeEqualByValueWhenEquivalentInputs() {
            Email email1 = new Email("User@Domain.COM");
            Email email2 = new Email("user@domain.com");

            assertThat(email1).isEqualTo(email2);
        }

        @Test
        @DisplayName("Must have same hashCode for equal instances")
        void mustHaveSameHashCodeWhenInstancesEqual() {
            Email email1 = new Email("User@Domain.COM");
            Email email2 = new Email("user@domain.com");

            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("Must be different when emails have distinct values")
        void mustBeDifferentWhenDistinctValues() {
            Email email1 = new Email("user1@domain.com");
            Email email2 = new Email("user2@domain.com");

            assertThat(email1).isNotEqualTo(email2);
        }

        @Test
        @DisplayName("Must return false when compared with null")
        void mustReturnFalseWhenComparedWithNull() {
            Email email = new Email("user@domain.com");

            assertThat(email.equals(null)).isFalse();
        }

        @Test
        @DisplayName("Must return false when compared with a different type")
        void mustReturnFalseWhenComparedWithDifferentType() {
            Email email = new Email("user@domain.com");

            assertThat(email.equals("user@domain.com")).isFalse();
        }

        @Test
        @DisplayName("toString must be stable and consistent with formatted() and raw()")
        void mustHaveToStringConsistentWithFormattedAndRaw() {
            Email email = new Email("User@Domain.COM");

            assertThat(email.toString()).isEqualTo("user@domain.com");
            assertThat(email.toString()).isEqualTo(email.formatted());
            assertThat(email.toString()).isEqualTo(email.raw());
        }
    }

    @Nested
    @DisplayName("5. Validation via isValid API")
    class IsValidApi {

        @Test
        @DisplayName("isValid must return true for valid input")
        void mustReturnTrueWhenEmailValid() {
            assertThat(Email.isValid("user@domain.com")).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "   ",
                "no-at-sign.com",
                "@domain.com",
                "user@",
                "user@domain",
                "user@domain.c",
                "a@b@c.com",
                ".user@domain.com",
                "user.@domain.com",
                "us..er@domain.com",
                "user@-domain.com",
                "user@domain-.com"
        })
        @DisplayName("isValid must return false for invalid inputs without throwing")
        void mustReturnFalseWhenEmailInvalid(String invalidInput) {
            assertThat(Email.isValid(invalidInput)).isFalse();
        }

        @Test
        @DisplayName("isValid(null) must return false without throwing NullPointerException")
        void mustReturnFalseWhenNullWithoutThrowingException() {
            assertThat(Email.isValid(null)).isFalse();
        }
    }
}

package com.project.ponto.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CPF Value Object Unit Tests")
class CPFTest {

    @Nested
    @DisplayName("1. Valid Construction and Normalization")
    class ValidConstruction {

        @ParameterizedTest
        @CsvSource({
                "'529.982.247-25', '52998224725'",
                "'52998224725', '52998224725'",
                "' 529.982.247-25 ', '52998224725'",
                "'529 982 247 25', '52998224725'",
                "'529.982.247/25', '52998224725'"
        })
        @DisplayName("Must accept and normalize valid CPF inputs")
        void mustNormalizeValidInputsWhenFormatIsCorrect(String input, String expectedRaw) {
            CPF cpf = new CPF(input);

            assertThat(cpf.raw()).isEqualTo(expectedRaw);
            assertThat(cpf.formatted()).isEqualTo("529.982.247-25");
            assertThat(cpf.toString()).isEqualTo(cpf.formatted());
        }

        @Test
        @DisplayName("Must produce the same raw for equivalent inputs with and without mask")
        void mustProduceSameRawWithAndWithoutMask() {
            CPF maskedCpf = new CPF("529.982.247-25");
            CPF unmaskedCpf = new CPF("52998224725");

            assertThat(maskedCpf.raw()).isEqualTo(unmaskedCpf.raw());
        }
    }

    @Nested
    @DisplayName("2. Rejection of Invalid Inputs")
    class InvalidRejection {

        @Test
        @DisplayName("Must reject null CPF")
        void mustRejectWhenNull() {
            assertThatThrownBy(() -> new CPF(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject empty CPF")
        void mustRejectWhenEmpty() {
            assertThatThrownBy(() -> new CPF(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject CPF with fewer than 11 digits")
        void mustRejectWhenFewerThan11Digits() {
            assertThatThrownBy(() -> new CPF("123"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject CPF with more than 11 digits")
        void mustRejectWhenMoreThan11Digits() {
            assertThatThrownBy(() -> new CPF("123456789012"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject CPF with letters only")
        void mustRejectWhenLettersOnly() {
            assertThatThrownBy(() -> new CPF("abcdefghijk"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject CPF with repeated digits")
        void mustRejectWhenRepeatedDigits() {
            assertThatThrownBy(() -> new CPF("111.111.111-11"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject CPF with incorrect first check digit")
        void mustRejectWhenFirstCheckDigitWrong() {
            assertThatThrownBy(() -> new CPF("529.982.247-35"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject CPF with incorrect second check digit")
        void mustRejectWhenSecondCheckDigitWrong() {
            assertThatThrownBy(() -> new CPF("529.982.247-26"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject CPF with both check digits incorrect")
        void mustRejectWhenBothCheckDigitsWrong() {
            assertThatThrownBy(() -> new CPF("529.982.247-00"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("3. Length Limit Validation")
    class Limits {

        @Test
        @DisplayName("Must accept input with exactly 11 digits")
        void mustAcceptWhenExactly11Digits() {
            CPF cpf = new CPF("52998224725");
            assertThat(cpf.raw()).hasSize(11);
        }

        @Test
        @DisplayName("Must reject input with 10 digits")
        void mustRejectWhen10Digits() {
            assertThatThrownBy(() -> new CPF("5299822472"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Must reject input with 12 digits")
        void mustRejectWhen12Digits() {
            assertThatThrownBy(() -> new CPF("529982247250"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("4. Value Object Contract (Equals, HashCode and ToString)")
    class ValueObjectContract {

        @Test
        @DisplayName("Must be equal by value for instances built from equivalent inputs")
        void mustBeEqualByValueWhenEquivalentInputs() {
            CPF cpf1 = new CPF("529.982.247-25");
            CPF cpf2 = new CPF("52998224725");

            assertThat(cpf1).isEqualTo(cpf2);
        }

        @Test
        @DisplayName("Must have same hashCode for equal instances")
        void mustHaveSameHashCodeWhenInstancesEqual() {
            CPF cpf1 = new CPF("529.982.247-25");
            CPF cpf2 = new CPF("52998224725");

            assertThat(cpf1.hashCode()).isEqualTo(cpf2.hashCode());
        }

        @Test
        @DisplayName("Must be different when CPFs have distinct values")
        void mustBeDifferentWhenDistinctValues() {
            CPF cpf1 = new CPF("529.982.247-25");
            CPF cpf2 = new CPF("111.444.777-35");

            assertThat(cpf1).isNotEqualTo(cpf2);
        }

        @Test
        @DisplayName("Must return false when compared with null")
        void mustReturnFalseWhenComparedWithNull() {
            CPF cpf = new CPF("529.982.247-25");

            assertThat(cpf.equals(null)).isFalse();
        }

        @Test
        @DisplayName("Must return false when compared with a different type")
        void mustReturnFalseWhenComparedWithDifferentType() {
            CPF cpf = new CPF("529.982.247-25");

            assertThat(cpf.equals("52998224725")).isFalse();
        }

        @Test
        @DisplayName("toString must be stable and consistent with formatted()")
        void mustHaveToStringConsistentWithFormatted() {
            CPF cpf = new CPF("529.982.247-25");

            assertThat(cpf.toString()).isEqualTo("529.982.247-25");
            assertThat(cpf.toString()).isEqualTo(cpf.formatted());
        }
    }

    @Nested
    @DisplayName("5. Validation via isValid API")
    class IsValidApi {

        @Test
        @DisplayName("isValid must return true for valid input")
        void mustReturnTrueWhenCpfValid() {
            assertThat(CPF.isValid("529.982.247-25")).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "   ",
                "123",
                "123456789012",
                "abcdefghijk",
                "111.111.111-11",
                "529.982.247-35",
                "529.982.247-26",
                "529.982.247-00"
        })
        @DisplayName("isValid must return false for invalid inputs without throwing")
        void mustReturnFalseWhenCpfInvalid(String invalidInput) {
            assertThat(CPF.isValid(invalidInput)).isFalse();
        }

        @Test
        @DisplayName("isValid(null) must return false without throwing NullPointerException")
        void mustReturnFalseWhenNullWithoutThrowingException() {
            assertThat(CPF.isValid(null)).isFalse();
        }
    }
}

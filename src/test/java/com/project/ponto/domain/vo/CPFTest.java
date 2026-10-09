package com.project.ponto.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes de Unidade do Value Object CPF")
class CPFTest {

    @Nested
    @DisplayName("1. Construção Válida e Normalização")
    class ConstrucaoValida {

        @ParameterizedTest
        @CsvSource({
                "'529.982.247-25', '52998224725'",
                "'52998224725', '52998224725'",
                "' 529.982.247-25 ', '52998224725'",
                "'529 982 247 25', '52998224725'",
                "'529.982.247/25', '52998224725'"
        })
        @DisplayName("Deve aceitar e normalizar entradas válidas de CPF")
        void deveNormalizarEntradasValidasQuandoFormatoCorreto(String entrada, String rawEsperado) {
            CPF cpf = new CPF(entrada);

            assertThat(cpf.raw()).isEqualTo(rawEsperado);
            assertThat(cpf.formatted()).isEqualTo("529.982.247-25");
            assertThat(cpf.toString()).isEqualTo(cpf.formatted());
        }

        @Test
        @DisplayName("Deve produzir o mesmo raw para entradas equivalentes com e sem máscara")
        void deveProduzirMesmoRawQuandoComESemMascara() {
            CPF cpfComMascara = new CPF("529.982.247-25");
            CPF cpfSemMascara = new CPF("52998224725");

            assertThat(cpfComMascara.raw()).isEqualTo(cpfSemMascara.raw());
        }
    }

    @Nested
    @DisplayName("2. Rejeição de Entradas Inválidas")
    class RejeicaoInvalida {

        @Test
        @DisplayName("Deve rejeitar CPF nulo")
        void deveRejeitarQuandoNulo() {
            assertThatThrownBy(() -> new CPF(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar CPF vazio")
        void deveRejeitarQuandoVazio() {
            assertThatThrownBy(() -> new CPF(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar CPF com menos de 11 dígitos")
        void deveRejeitarQuandoMenosDe11Digitos() {
            assertThatThrownBy(() -> new CPF("123"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar CPF com mais de 11 dígitos")
        void deveRejeitarQuandoMaisDe11Digitos() {
            assertThatThrownBy(() -> new CPF("123456789012"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar CPF com somente letras")
        void deveRejeitarQuandoSomenteLetras() {
            assertThatThrownBy(() -> new CPF("abcdefghijk"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar CPF com dígitos repetidos")
        void deveRejeitarQuandoDigitosRepetidos() {
            assertThatThrownBy(() -> new CPF("111.111.111-11"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar CPF com primeiro dígito verificador incorreto")
        void deveRejeitarQuandoPrimeiroDvErrado() {
            assertThatThrownBy(() -> new CPF("529.982.247-35"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar CPF com segundo dígito verificador incorreto")
        void deveRejeitarQuandoSegundoDvErrado() {
            assertThatThrownBy(() -> new CPF("529.982.247-26"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar CPF com ambos os dígitos verificadores incorretos")
        void deveRejeitarQuandoAmbosDvsErrados() {
            assertThatThrownBy(() -> new CPF("529.982.247-00"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("3. Validação de Limites de Tamanho")
    class Limites {

        @Test
        @DisplayName("Deve aceitar entrada com exatamente 11 dígitos")
        void deveAceitarQuandoExatamente11Digitos() {
            CPF cpf = new CPF("52998224725");
            assertThat(cpf.raw()).hasSize(11);
        }

        @Test
        @DisplayName("Deve rejeitar entrada com 10 dígitos")
        void deveRejeitarQuando10Digitos() {
            assertThatThrownBy(() -> new CPF("5299822472"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar entrada com 12 dígitos")
        void deveRejeitarQuando12Digitos() {
            assertThatThrownBy(() -> new CPF("529982247250"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("4. Contrato de Value Object (Equals, HashCode e ToString)")
    class ContratoValueObject {

        @Test
        @DisplayName("Deve ser igual por valor para instâncias construídas de entradas equivalentes")
        void deveSerIgualPorValorQuandoEntradasEquivalentes() {
            CPF cpf1 = new CPF("529.982.247-25");
            CPF cpf2 = new CPF("52998224725");

            assertThat(cpf1).isEqualTo(cpf2);
        }

        @Test
        @DisplayName("Deve possuir mesmo hashCode para instâncias iguais")
        void devePossuirMesmoHashCodeQuandoInstanciasIguais() {
            CPF cpf1 = new CPF("529.982.247-25");
            CPF cpf2 = new CPF("52998224725");

            assertThat(cpf1.hashCode()).isEqualTo(cpf2.hashCode());
        }

        @Test
        @DisplayName("Deve ser diferente quando os CPFs possuem valores distintos")
        void deveSerDiferenteQuandoValoresDistintos() {
            CPF cpf1 = new CPF("529.982.247-25");
            CPF cpf2 = new CPF("111.444.777-35");

            assertThat(cpf1).isNotEqualTo(cpf2);
        }

        @Test
        @DisplayName("Deve retornar false ao comparar com null")
        void deveRetornarFalseQuandoComparadoComNull() {
            CPF cpf = new CPF("529.982.247-25");

            assertThat(cpf.equals(null)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false ao comparar com tipo diferente")
        void deveRetornarFalseQuandoComparadoComTipoDiferente() {
            CPF cpf = new CPF("529.982.247-25");

            assertThat(cpf.equals("52998224725")).isFalse();
        }

        @Test
        @DisplayName("toString deve ser estável e consistente com formatted()")
        void deveTerToStringConsistenteComFormatted() {
            CPF cpf = new CPF("529.982.247-25");

            assertThat(cpf.toString()).isEqualTo("529.982.247-25");
            assertThat(cpf.toString()).isEqualTo(cpf.formatted());
        }
    }

    @Nested
    @DisplayName("5. Validação via API isValid")
    class ApiIsValid {

        @Test
        @DisplayName("isValid deve retornar true para entrada válida")
        void deveRetornarTrueQuandoCpfValido() {
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
        @DisplayName("isValid deve retornar false para entradas inválidas sem lançar exceção")
        void deveRetornarFalseQuandoCpfInvalido(String entradaInvalida) {
            assertThat(CPF.isValid(entradaInvalida)).isFalse();
        }

        @Test
        @DisplayName("isValid(null) deve retornar false sem lançar NullPointerException")
        void deveRetornarFalseQuandoNullSemLancarExcecao() {
            assertThat(CPF.isValid(null)).isFalse();
        }
    }
}

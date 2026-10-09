package com.project.ponto.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes de Unidade do Value Object Email")
class EmailTest {

    @Nested
    @DisplayName("1. Construção Válida e Normalização")
    class ConstrucaoValida {

        @ParameterizedTest
        @CsvSource({
                "'Usuario@Dominio.COM', 'usuario@dominio.com', 'usuario', 'dominio.com'",
                "' user@dominio.com ', 'user@dominio.com', 'user', 'dominio.com'",
                "'nome.sobrenome@empresa.com.br', 'nome.sobrenome@empresa.com.br', 'nome.sobrenome', 'empresa.com.br'",
                "'user+tag@gmail.com', 'user+tag@gmail.com', 'user+tag', 'gmail.com'",
                "'u_s%e-r@meu-dominio.com', 'u_s%e-r@meu-dominio.com', 'u_s%e-r', 'meu-dominio.com'",
                "'a@b.co', 'a@b.co', 'a', 'b.co'",
                "'user@dominio.technology', 'user@dominio.technology', 'user', 'dominio.technology'"
        })
        @DisplayName("Deve aceitar e normalizar e-mails válidos")
        void deveNormalizarEntradasValidas(String entrada, String rawEsperado, String localEsperado, String dominioEsperado) {
            Email email = new Email(entrada);

            assertThat(email.raw()).isEqualTo(rawEsperado);
            assertThat(email.formatted()).isEqualTo(rawEsperado);
            assertThat(email.localPart()).isEqualTo(localEsperado);
            assertThat(email.domain()).isEqualTo(dominioEsperado);
            assertThat(email.toString()).isEqualTo(rawEsperado);
        }
    }

    @Nested
    @DisplayName("2. Rejeição de Entradas Inválidas")
    class RejeicaoInvalida {

        @Test
        @DisplayName("Deve rejeitar e-mail nulo")
        void deveRejeitarQuandoNulo() {
            assertThatThrownBy(() -> new Email(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail vazio")
        void deveRejeitarQuandoVazio() {
            assertThatThrownBy(() -> new Email(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail só com espaços")
        void deveRejeitarQuandoSoEspacos() {
            assertThatThrownBy(() -> new Email("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail sem @")
        void deveRejeitarQuandoSemArroba() {
            assertThatThrownBy(() -> new Email("sem-arroba.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail sem parte local")
        void deveRejeitarQuandoSemLocal() {
            assertThatThrownBy(() -> new Email("@dominio.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail sem domínio")
        void deveRejeitarQuandoSemDominio() {
            assertThatThrownBy(() -> new Email("user@"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail sem TLD")
        void deveRejeitarQuandoSemTld() {
            assertThatThrownBy(() -> new Email("user@dominio"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail com TLD de 1 letra")
        void deveRejeitarQuandoTldDe1Letra() {
            assertThatThrownBy(() -> new Email("user@dominio.c"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail com dois @")
        void deveRejeitarQuandoDoisArrobas() {
            assertThatThrownBy(() -> new Email("a@b@c.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail com ponto inicial no local")
        void deveRejeitarQuandoPontoInicialNoLocal() {
            assertThatThrownBy(() -> new Email(".user@dominio.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail com ponto final no local")
        void deveRejeitarQuandoPontoFinalNoLocal() {
            assertThatThrownBy(() -> new Email("user.@dominio.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail com pontos consecutivos no local")
        void deveRejeitarQuandoPontosConsecutivos() {
            assertThatThrownBy(() -> new Email("us..er@dominio.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail com rótulo do domínio começando com hífen")
        void deveRejeitarQuandoRotuloComecaComHifen() {
            assertThatThrownBy(() -> new Email("user@-dominio.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail com rótulo do domínio terminando com hífen")
        void deveRejeitarQuandoRotuloTerminaComHifen() {
            assertThatThrownBy(() -> new Email("user@dominio-.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("3. Validação de Limites de Tamanho")
    class Limites {

        @Test
        @DisplayName("Deve aceitar parte local com exatamente 64 caracteres")
        void deveAceitarLocalCom64Caracteres() {
            String local64 = "a".repeat(64);
            Email email = new Email(local64 + "@dominio.com");
            assertThat(email.localPart()).hasSize(64);
        }

        @Test
        @DisplayName("Deve rejeitar parte local com 65 caracteres")
        void deveRejeitarLocalCom65Caracteres() {
            String local65 = "a".repeat(65);
            assertThatThrownBy(() -> new Email(local65 + "@dominio.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve aceitar e-mail com total de exatamente 254 caracteres")
        void deveAceitarTotalCom254Caracteres() {
            // "a".repeat(64) + "@" + "b".repeat(180) + ".com" = 64 + 1 + 180 + 4 = 249
            // Ajustando domínio para dar 254 total:
            // Local (64) + "@" (1) + domínio + ".com" (4) = 254 -> domínio precisa ter 254 - 64 - 1 - 4 = 185
            // Ex.: "b".repeat(60) + "." + "b".repeat(60) + "." + "b".repeat(61) + ".com" -> 60+1+60+1+61+4 = 187 caracteres de domínio
            String local64 = "a".repeat(64);
            String label1 = "b".repeat(60);
            String label2 = "c".repeat(60);
            String label3 = "d".repeat(63); // 60+1+60+1+63 = 185 -> + 4 (".com") = 189. 189 + 65 = 254
            String emailStr = local64 + "@" + label1 + "." + label2 + "." + label3 + ".com";

            Email email = new Email(emailStr);
            assertThat(email.raw()).hasSize(254);
        }

        @Test
        @DisplayName("Deve rejeitar e-mail com total de 255 caracteres")
        void deveRejeitarTotalCom255Caracteres() {
            String local64 = "a".repeat(64);
            String label1 = "b".repeat(60);
            String label2 = "c".repeat(60);
            String label3 = "d".repeat(64); // +1 char
            String emailStr = local64 + "@" + label1 + "." + label2 + "." + label3 + ".com";

            assertThatThrownBy(() -> new Email(emailStr))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("4. Contrato de Value Object (Equals, HashCode e ToString)")
    class ContratoValueObject {

        @Test
        @DisplayName("Deve ser igual por valor para instâncias construídas de entradas equivalentes")
        void deveSerIgualPorValorQuandoEntradasEquivalentes() {
            Email email1 = new Email("Usuario@Dominio.COM");
            Email email2 = new Email("usuario@dominio.com");

            assertThat(email1).isEqualTo(email2);
        }

        @Test
        @DisplayName("Deve possuir mesmo hashCode para instâncias iguais")
        void devePossuirMesmoHashCodeQuandoInstanciasIguais() {
            Email email1 = new Email("Usuario@Dominio.COM");
            Email email2 = new Email("usuario@dominio.com");

            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("Deve ser diferente quando e-mails possuem valores distintos")
        void deveSerDiferenteQuandoValoresDistintos() {
            Email email1 = new Email("user1@dominio.com");
            Email email2 = new Email("user2@dominio.com");

            assertThat(email1).isNotEqualTo(email2);
        }

        @Test
        @DisplayName("Deve retornar false ao comparar com null")
        void deveRetornarFalseQuandoComparadoComNull() {
            Email email = new Email("user@dominio.com");

            assertThat(email.equals(null)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false ao comparar com tipo diferente")
        void deveRetornarFalseQuandoComparadoComTipoDiferente() {
            Email email = new Email("user@dominio.com");

            assertThat(email.equals("user@dominio.com")).isFalse();
        }

        @Test
        @DisplayName("toString deve ser estável e consistente com formatted() e raw()")
        void deveTerToStringConsistenteComFormattedERaw() {
            Email email = new Email("Usuario@Dominio.COM");

            assertThat(email.toString()).isEqualTo("usuario@dominio.com");
            assertThat(email.toString()).isEqualTo(email.formatted());
            assertThat(email.toString()).isEqualTo(email.raw());
        }
    }

    @Nested
    @DisplayName("5. Validação via API isValid")
    class ApiIsValid {

        @Test
        @DisplayName("isValid deve retornar true para entrada válida")
        void deveRetornarTrueQuandoEmailValido() {
            assertThat(Email.isValid("usuario@dominio.com")).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "   ",
                "sem-arroba.com",
                "@dominio.com",
                "user@",
                "user@dominio",
                "user@dominio.c",
                "a@b@c.com",
                ".user@dominio.com",
                "user.@dominio.com",
                "us..er@dominio.com",
                "user@-dominio.com",
                "user@dominio-.com"
        })
        @DisplayName("isValid deve retornar false para entradas inválidas sem lançar exceção")
        void deveRetornarFalseQuandoEmailInvalido(String entradaInvalida) {
            assertThat(Email.isValid(entradaInvalida)).isFalse();
        }

        @Test
        @DisplayName("isValid(null) deve retornar false sem lançar NullPointerException")
        void deveRetornarFalseQuandoNullSemLancarExcecao() {
            assertThat(Email.isValid(null)).isFalse();
        }
    }
}

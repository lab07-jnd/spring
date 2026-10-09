package com.project.ponto.domain.vo;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DisplayName("Testes de Persistência JPA do Value Object Email")
class EmailPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Entity
    @Table(name = "pessoa_teste_email")
    static class PessoaTesteEmail {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Embedded
        private Email email;

        protected PessoaTesteEmail() {}

        public PessoaTesteEmail(Email email) {
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public Email getEmail() {
            return email;
        }
    }

    @Nested
    @DisplayName("6. Persistência JPA")
    class PersistenciaJpa {

        @Test
        @DisplayName("Deve realizar round-trip de Email válido pelo banco de dados")
        void deveRealizarRoundTripComSucesso() {
            Email emailOriginal = new Email("user@dominio.com");
            PessoaTesteEmail pessoa = new PessoaTesteEmail(emailOriginal);

            PessoaTesteEmail salva = em.persistAndFlush(pessoa);
            em.clear();

            PessoaTesteEmail recarregada = em.find(PessoaTesteEmail.class, salva.getId());

            assertThat(recarregada).isNotNull();
            assertThat(recarregada.getEmail().raw()).isEqualTo("user@dominio.com");
            assertThat(recarregada.getEmail()).isEqualTo(emailOriginal);
            assertThat(recarregada.getEmail().formatted()).isEqualTo("user@dominio.com");
        }

        @Test
        @DisplayName("Deve persistir a forma canônica normalizada (lowercase e stripped)")
        void devePersistirValorNormalizado() {
            Email emailComMascara = new Email(" Usuario@Dominio.COM ");
            PessoaTesteEmail pessoa = new PessoaTesteEmail(emailComMascara);

            PessoaTesteEmail salva = em.persistAndFlush(pessoa);
            em.clear();

            Object valorNoBanco = em.getEntityManager()
                    .createNativeQuery("SELECT email FROM pessoa_teste_email WHERE id = :id")
                    .setParameter("id", salva.getId())
                    .getSingleResult();

            assertThat(valorNoBanco).isEqualTo("usuario@dominio.com");
        }

        @Test
        @DisplayName("Deve falhar ao carregar entidade se o valor armazenado no banco for inválido")
        void deveFalharAoCarregarDadoInvalidoDoBanco() {
            em.getEntityManager()
                    .createNativeQuery("INSERT INTO pessoa_teste_email (id, email) VALUES (999, 'invalido-sem-arroba.com')")
                    .executeUpdate();
            em.clear();

            assertThatThrownBy(() -> em.find(PessoaTesteEmail.class, 999L))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Deve garantir igualdade por equals após o round-trip")
        void deveManterIgualdadeAposRoundTrip() {
            Email emailOriginal = new Email("USER@DOMINIO.COM");
            PessoaTesteEmail pessoa = new PessoaTesteEmail(emailOriginal);

            PessoaTesteEmail salva = em.persistAndFlush(pessoa);
            em.clear();

            PessoaTesteEmail recarregada = em.find(PessoaTesteEmail.class, salva.getId());
            Email emailEsperado = new Email("user@dominio.com");

            assertThat(recarregada.getEmail()).isEqualTo(emailEsperado);
        }
    }
}

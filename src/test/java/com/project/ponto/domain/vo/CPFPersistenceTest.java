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
@DisplayName("Testes de Persistência JPA do Value Object CPF")
class CPFPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Entity
    @Table(name = "pessoa_teste_cpf")
    static class PessoaTesteCPF {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Embedded
        private CPF cpf;

        protected PessoaTesteCPF() {}

        public PessoaTesteCPF(CPF cpf) {
            this.cpf = cpf;
        }

        public Long getId() {
            return id;
        }

        public CPF getCpf() {
            return cpf;
        }
    }

    @Nested
    @DisplayName("6. Persistência JPA")
    class PersistenciaJpa {

        @Test
        @DisplayName("Deve realizar round-trip de CPF válido pelo banco de dados")
        void deveRealizarRoundTripComSucesso() {
            CPF cpfOriginal = new CPF("529.982.247-25");
            PessoaTesteCPF pessoa = new PessoaTesteCPF(cpfOriginal);

            PessoaTesteCPF salva = em.persistAndFlush(pessoa);
            em.clear();

            PessoaTesteCPF recarregada = em.find(PessoaTesteCPF.class, salva.getId());

            assertThat(recarregada).isNotNull();
            assertThat(recarregada.getCpf().raw()).isEqualTo("52998224725");
            assertThat(recarregada.getCpf()).isEqualTo(cpfOriginal);
            assertThat(recarregada.getCpf().formatted()).isEqualTo("529.982.247-25");
        }

        @Test
        @DisplayName("Deve persistir o valor normalizado mesmo quando construído com entrada formatada")
        void devePersistirValorNormalizado() {
            CPF cpfComMascara = new CPF(" 529.982.247-25 ");
            PessoaTesteCPF pessoa = new PessoaTesteCPF(cpfComMascara);

            PessoaTesteCPF salva = em.persistAndFlush(pessoa);
            em.clear();

            Object valorNoBanco = em.getEntityManager()
                    .createNativeQuery("SELECT cpf FROM pessoa_teste_cpf WHERE id = :id")
                    .setParameter("id", salva.getId())
                    .getSingleResult();

            assertThat(valorNoBanco).isEqualTo("52998224725");
        }

        @Test
        @DisplayName("Deve falhar ao carregar entidade se o valor armazenado no banco for inválido")
        void deveFalharAoCarregarDadoInvalidoDoBanco() {
            em.getEntityManager()
                    .createNativeQuery("INSERT INTO pessoa_teste_cpf (id, cpf) VALUES (999, '00000000000')")
                    .executeUpdate();
            em.clear();

            assertThatThrownBy(() -> em.find(PessoaTesteCPF.class, 999L))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Deve garantir igualdade por equals após o round-trip")
        void deveManterIgualdadeAposRoundTrip() {
            CPF cpfOriginal = new CPF("52998224725");
            PessoaTesteCPF pessoa = new PessoaTesteCPF(cpfOriginal);

            PessoaTesteCPF salva = em.persistAndFlush(pessoa);
            em.clear();

            PessoaTesteCPF recarregada = em.find(PessoaTesteCPF.class, salva.getId());
            CPF cpfEsperado = new CPF("529.982.247-25");

            assertThat(recarregada.getCpf()).isEqualTo(cpfEsperado);
        }
    }
}

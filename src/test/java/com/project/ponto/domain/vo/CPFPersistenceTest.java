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
@DisplayName("CPF Value Object JPA Persistence Tests")
class CPFPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Entity
    @Table(name = "person_test_cpf")
    static class PersonTestCPF {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Embedded
        private CPF cpf;

        protected PersonTestCPF() {}

        public PersonTestCPF(CPF cpf) {
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
    @DisplayName("6. JPA Persistence")
    class JpaPersistence {

        @Test
        @DisplayName("Must perform round-trip of a valid CPF through the database")
        void mustPerformRoundTripSuccessfully() {
            CPF originalCpf = new CPF("529.982.247-25");
            PersonTestCPF person = new PersonTestCPF(originalCpf);

            PersonTestCPF saved = em.persistAndFlush(person);
            em.clear();

            PersonTestCPF reloaded = em.find(PersonTestCPF.class, saved.getId());

            assertThat(reloaded).isNotNull();
            assertThat(reloaded.getCpf().raw()).isEqualTo("52998224725");
            assertThat(reloaded.getCpf()).isEqualTo(originalCpf);
            assertThat(reloaded.getCpf().formatted()).isEqualTo("529.982.247-25");
        }

        @Test
        @DisplayName("Must persist the normalized value even when constructed with formatted input")
        void mustPersistNormalizedValue() {
            CPF maskedCpf = new CPF(" 529.982.247-25 ");
            PersonTestCPF person = new PersonTestCPF(maskedCpf);

            PersonTestCPF saved = em.persistAndFlush(person);
            em.clear();

            Object valueInDatabase = em.getEntityManager()
                    .createNativeQuery("SELECT cpf FROM person_test_cpf WHERE id = :id")
                    .setParameter("id", saved.getId())
                    .getSingleResult();

            assertThat(valueInDatabase).isEqualTo("52998224725");
        }

        @Test
        @DisplayName("Must fail to load entity if the value stored in the database is invalid")
        void mustFailToLoadInvalidDataFromDatabase() {
            em.getEntityManager()
                    .createNativeQuery("INSERT INTO person_test_cpf (id, cpf) VALUES (999, '00000000000')")
                    .executeUpdate();
            em.clear();

            assertThatThrownBy(() -> em.find(PersonTestCPF.class, 999L))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Must guarantee equality by equals after round-trip")
        void mustMaintainEqualityAfterRoundTrip() {
            CPF originalCpf = new CPF("52998224725");
            PersonTestCPF person = new PersonTestCPF(originalCpf);

            PersonTestCPF saved = em.persistAndFlush(person);
            em.clear();

            PersonTestCPF reloaded = em.find(PersonTestCPF.class, saved.getId());
            CPF expectedCpf = new CPF("529.982.247-25");

            assertThat(reloaded.getCpf()).isEqualTo(expectedCpf);
        }
    }
}

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
@DisplayName("Email Value Object JPA Persistence Tests")
class EmailPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Entity
    @Table(name = "person_test_email")
    static class PersonTestEmail {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Embedded
        private Email email;

        protected PersonTestEmail() {}

        public PersonTestEmail(Email email) {
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
    @DisplayName("6. JPA Persistence")
    class JpaPersistence {

        @Test
        @DisplayName("Must perform round-trip of a valid Email through the database")
        void mustPerformRoundTripSuccessfully() {
            Email originalEmail = new Email("user@domain.com");
            PersonTestEmail person = new PersonTestEmail(originalEmail);

            PersonTestEmail saved = em.persistAndFlush(person);
            em.clear();

            PersonTestEmail reloaded = em.find(PersonTestEmail.class, saved.getId());

            assertThat(reloaded).isNotNull();
            assertThat(reloaded.getEmail().raw()).isEqualTo("user@domain.com");
            assertThat(reloaded.getEmail()).isEqualTo(originalEmail);
            assertThat(reloaded.getEmail().formatted()).isEqualTo("user@domain.com");
        }

        @Test
        @DisplayName("Must persist the normalized canonical form (lowercase and stripped)")
        void mustPersistNormalizedValue() {
            Email maskedEmail = new Email(" User@Domain.COM ");
            PersonTestEmail person = new PersonTestEmail(maskedEmail);

            PersonTestEmail saved = em.persistAndFlush(person);
            em.clear();

            Object valueInDatabase = em.getEntityManager()
                    .createNativeQuery("SELECT email FROM person_test_email WHERE id = :id")
                    .setParameter("id", saved.getId())
                    .getSingleResult();

            assertThat(valueInDatabase).isEqualTo("user@domain.com");
        }

        @Test
        @DisplayName("Must fail to load entity if the value stored in the database is invalid")
        void mustFailToLoadInvalidDataFromDatabase() {
            em.getEntityManager()
                    .createNativeQuery("INSERT INTO person_test_email (id, email) VALUES (999, 'invalid-no-at-sign.com')")
                    .executeUpdate();
            em.clear();

            assertThatThrownBy(() -> em.find(PersonTestEmail.class, 999L))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Must guarantee equality by equals after round-trip")
        void mustMaintainEqualityAfterRoundTrip() {
            Email originalEmail = new Email("USER@DOMAIN.COM");
            PersonTestEmail person = new PersonTestEmail(originalEmail);

            PersonTestEmail saved = em.persistAndFlush(person);
            em.clear();

            PersonTestEmail reloaded = em.find(PersonTestEmail.class, saved.getId());
            Email expectedEmail = new Email("user@domain.com");

            assertThat(reloaded.getEmail()).isEqualTo(expectedEmail);
        }
    }
}

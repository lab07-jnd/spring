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
@DisplayName("Geolocation Value Object JPA Persistence Tests")
class GeolocationPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Entity
    @Table(name = "location_test_geolocation")
    static class LocationTestEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Embedded
        private Geolocation geolocation;

        protected LocationTestEntity() {}

        public LocationTestEntity(Geolocation geolocation) {
            this.geolocation = geolocation;
        }

        public Long getId() {
            return id;
        }

        public Geolocation getGeolocation() {
            return geolocation;
        }
    }

    @Nested
    @DisplayName("JPA Persistence Round-Trip and BVA")
    class JpaPersistence {

        @Test
        @DisplayName("Must perform round-trip of nominal coordinates through database")
        void mustPerformRoundTripNominal() {
            Geolocation original = new Geolocation(-23.550520, -46.633308);
            LocationTestEntity entity = new LocationTestEntity(original);

            LocationTestEntity saved = em.persistAndFlush(entity);
            em.clear();

            LocationTestEntity reloaded = em.find(LocationTestEntity.class, saved.getId());

            assertThat(reloaded).isNotNull();
            assertThat(reloaded.getGeolocation()).isNotNull();
            assertThat(reloaded.getGeolocation().latitude()).isEqualTo(-23.550520);
            assertThat(reloaded.getGeolocation().longitude()).isEqualTo(-46.633308);
            assertThat(reloaded.getGeolocation()).isEqualTo(original);
        }

        @Test
        @DisplayName("Must perform round-trip of boundary coordinates (-90.0, -180.0)")
        void mustPerformRoundTripMinBoundaries() {
            Geolocation original = new Geolocation(-90.0, -180.0);
            LocationTestEntity entity = new LocationTestEntity(original);

            LocationTestEntity saved = em.persistAndFlush(entity);
            em.clear();

            LocationTestEntity reloaded = em.find(LocationTestEntity.class, saved.getId());

            assertThat(reloaded).isNotNull();
            assertThat(reloaded.getGeolocation()).isNotNull();
            assertThat(reloaded.getGeolocation().latitude()).isEqualTo(-90.0);
            assertThat(reloaded.getGeolocation().longitude()).isEqualTo(-180.0);
        }

        @Test
        @DisplayName("Must perform round-trip of boundary coordinates (90.0, 180.0)")
        void mustPerformRoundTripMaxBoundaries() {
            Geolocation original = new Geolocation(90.0, 180.0);
            LocationTestEntity entity = new LocationTestEntity(original);

            LocationTestEntity saved = em.persistAndFlush(entity);
            em.clear();

            LocationTestEntity reloaded = em.find(LocationTestEntity.class, saved.getId());

            assertThat(reloaded).isNotNull();
            assertThat(reloaded.getGeolocation()).isNotNull();
            assertThat(reloaded.getGeolocation().latitude()).isEqualTo(90.0);
            assertThat(reloaded.getGeolocation().longitude()).isEqualTo(180.0);
        }

        @Test
        @DisplayName("Must persist null coordinates when geolocation is null or empty")
        void mustPersistNullWhenAbsent() {
            LocationTestEntity entity = new LocationTestEntity(null);

            LocationTestEntity saved = em.persistAndFlush(entity);
            em.clear();

            LocationTestEntity reloaded = em.find(LocationTestEntity.class, saved.getId());

            assertThat(reloaded).isNotNull();
            assertThat(reloaded.getGeolocation()).isNull();
        }

        @Test
        @DisplayName("Must fail to load entity if persisted values violate domain range")
        void mustFailToLoadInvalidDataFromDatabase() {
            em.getEntityManager()
                    .createNativeQuery("INSERT INTO location_test_geolocation (id, latitude, longitude) VALUES (999, 100.0, 200.0)")
                    .executeUpdate();
            em.clear();

            assertThatThrownBy(() -> em.find(LocationTestEntity.class, 999L))
                    .isInstanceOf(Exception.class);
        }
    }
}

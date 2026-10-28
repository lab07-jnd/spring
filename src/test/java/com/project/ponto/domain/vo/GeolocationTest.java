package com.project.ponto.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Geolocation Value Object Unit Tests")
class GeolocationTest {

    @Nested
    @DisplayName("1. Normal BVA: Valid Construction and Boundary Values")
    class ValidConstruction {

        @ParameterizedTest
        @CsvSource({
                "-90.0, -180.0",
                "90.0, 180.0",
                "-89.9999999, -179.9999999",
                "89.9999999, 179.9999999",
                "0.0, 0.0",
                "-23.550520, -46.633308"
        })
        @DisplayName("Must accept valid boundary and nominal coordinate inputs")
        void mustAcceptValidCoordinates(Double latitude, Double longitude) {
            Geolocation geo = new Geolocation(latitude, longitude);

            assertThat(geo.latitude()).isEqualTo(latitude);
            assertThat(geo.longitude()).isEqualTo(longitude);
        }

        @Test
        @DisplayName("Factory method Geolocation.of returns null when both coordinates are null")
        void ofFactoryReturnsNullWhenBothNull() {
            assertThat(Geolocation.of(null, null)).isNull();
        }

        @Test
        @DisplayName("Factory method Geolocation.of returns instance when coordinates are provided")
        void ofFactoryReturnsInstance() {
            Geolocation geo = Geolocation.of(-23.55, -46.63);
            assertThat(geo).isNotNull();
            assertThat(geo.latitude()).isEqualTo(-23.55);
            assertThat(geo.longitude()).isEqualTo(-46.63);
        }
    }

    @Nested
    @DisplayName("2. Robust BVA: Rejection of Out-of-Bounds Coordinates")
    class RobustRejection {

        @Test
        @DisplayName("Must reject latitude below -90.0 (-90.0000001)")
        void mustRejectLatitudeBelowMin() {
            assertThatThrownBy(() -> new Geolocation(-90.0000001, 0.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Latitude must be between");
        }

        @Test
        @DisplayName("Must reject latitude above 90.0 (90.0000001)")
        void mustRejectLatitudeAboveMax() {
            assertThatThrownBy(() -> new Geolocation(90.0000001, 0.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Latitude must be between");
        }

        @Test
        @DisplayName("Must reject longitude below -180.0 (-180.0000001)")
        void mustRejectLongitudeBelowMin() {
            assertThatThrownBy(() -> new Geolocation(0.0, -180.0000001))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Longitude must be between");
        }

        @Test
        @DisplayName("Must reject longitude above 180.0 (180.0000001)")
        void mustRejectLongitudeAboveMax() {
            assertThatThrownBy(() -> new Geolocation(0.0, 180.0000001))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Longitude must be between");
        }
    }

    @Nested
    @DisplayName("3. Robust Worst-Case: Incomplete and Multiple Out-of-Bounds Coordinates")
    class RobustWorstCase {

        @Test
        @DisplayName("Must reject latitude provided without longitude")
        void mustRejectLatitudeWithoutLongitude() {
            assertThatThrownBy(() -> new Geolocation(-23.55, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Both latitude and longitude must be provided together");
        }

        @Test
        @DisplayName("Must reject longitude provided without latitude")
        void mustRejectLongitudeWithoutLatitude() {
            assertThatThrownBy(() -> new Geolocation(null, -46.63))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Both latitude and longitude must be provided together");
        }

        @Test
        @DisplayName("Must reject when both coordinates are null in constructor")
        void mustRejectBothCoordinatesNull() {
            assertThatThrownBy(() -> new Geolocation(null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Both latitude and longitude must be provided together and cannot be null");
        }

        @Test
        @DisplayName("Must reject when both coordinates are out-of-bounds simultaneously")
        void mustRejectWhenBothCoordinatesOutOfBounds() {
            assertThatThrownBy(() -> new Geolocation(-90.001, 180.001))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("4. Value Object Contract (Equals, HashCode and ToString)")
    class ValueObjectContract {

        @Test
        @DisplayName("Must be equal by value for instances built with same coordinates")
        void mustBeEqualByValue() {
            Geolocation geo1 = new Geolocation(-23.55, -46.63);
            Geolocation geo2 = new Geolocation(-23.55, -46.63);

            assertThat(geo1).isEqualTo(geo2);
            assertThat(geo1.hashCode()).isEqualTo(geo2.hashCode());
        }

        @Test
        @DisplayName("Must be different when coordinates differ")
        void mustBeDifferent() {
            Geolocation geo1 = new Geolocation(-23.55, -46.63);
            Geolocation geo2 = new Geolocation(-23.56, -46.63);

            assertThat(geo1).isNotEqualTo(geo2);
        }

        @Test
        @DisplayName("toString must format coordinate pair nicely")
        void toStringFormatting() {
            Geolocation geo = new Geolocation(-23.55, -46.63);
            assertThat(geo.toString()).isEqualTo("(-23.55, -46.63)");
        }
    }

    @Nested
    @DisplayName("5. Validation via isValid API")
    class IsValidApi {

        @Test
        @DisplayName("isValid must return true for valid coordinates")
        void mustReturnTrueForValid() {
            assertThat(Geolocation.isValid(-90.0, -180.0)).isTrue();
            assertThat(Geolocation.isValid(90.0, 180.0)).isTrue();
        }

        @Test
        @DisplayName("isValid must return false for out-of-bounds or null coordinates without throwing")
        void mustReturnFalseForInvalid() {
            assertThat(Geolocation.isValid(-90.0001, 0.0)).isFalse();
            assertThat(Geolocation.isValid(0.0, 180.0001)).isFalse();
            assertThat(Geolocation.isValid(-23.55, null)).isFalse();
            assertThat(Geolocation.isValid(null, -46.63)).isFalse();
            assertThat(Geolocation.isValid(null, null)).isFalse();
        }
    }
}

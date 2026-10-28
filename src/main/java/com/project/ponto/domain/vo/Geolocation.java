package com.project.ponto.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Geolocation(

        @Column(name = "latitude")
        Double latitude,

        @Column(name = "longitude")
        Double longitude

) {

    public static final double MIN_LATITUDE = -90.0;
    public static final double MAX_LATITUDE = 90.0;
    public static final double MIN_LONGITUDE = -180.0;
    public static final double MAX_LONGITUDE = 180.0;

    public Geolocation {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Both latitude and longitude must be provided together and cannot be null");
        }
        if (!isLatitudeValid(latitude)) {
            throw new IllegalArgumentException(
                    "Latitude must be between %s and %s, received: %s".formatted(MIN_LATITUDE, MAX_LATITUDE, latitude)
            );
        }
        if (!isLongitudeValid(longitude)) {
            throw new IllegalArgumentException(
                    "Longitude must be between %s and %s, received: %s".formatted(MIN_LONGITUDE, MAX_LONGITUDE, longitude)
            );
        }
    }

    public static Geolocation of(Double latitude, Double longitude) {
        if (latitude == null && longitude == null) {
            return null;
        }
        return new Geolocation(latitude, longitude);
    }

    public static boolean isValid(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }
        return isLatitudeValid(latitude) && isLongitudeValid(longitude);
    }

    private static boolean isLatitudeValid(double lat) {
        return lat >= MIN_LATITUDE && lat <= MAX_LATITUDE;
    }

    private static boolean isLongitudeValid(double lon) {
        return lon >= MIN_LONGITUDE && lon <= MAX_LONGITUDE;
    }

    @Override
    public String toString() {
        return "(%s, %s)".formatted(latitude, longitude);
    }
}
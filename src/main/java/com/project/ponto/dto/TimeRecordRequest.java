package com.project.ponto.dto;

import com.project.ponto.domain.enums.TimeRecordOriginEnum;
import com.project.ponto.domain.enums.TimeRecordTypeEnum;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TimeRecordRequest(
        @NotNull(message = "Employee ID must be provided")
        Long employeeId,

        @NotNull(message = "Time record type must be provided")
        TimeRecordTypeEnum type,

        @NotNull(message = "Time record origin must be provided")
        TimeRecordOriginEnum origin,

        @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90.0")
        @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90.0")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180.0")
        @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180.0")
        Double longitude,

        @Size(max = 255, message = "The notes field exceeds the limit of {max} characters")
        String notes
) {
    @AssertTrue(message = "Both latitude and longitude must be provided together")
    private boolean isGeolocationConsistent() {
        return (latitude == null && longitude == null) || (latitude != null && longitude != null);
    }
}
package com.project.ponto.dto;

import java.time.LocalDate;

import com.project.ponto.domain.enums.JustificationTypeEnum;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JustificationRequest(
        @NotNull(message = "Start date must be provided")
        LocalDate startDate,

        @NotNull(message = "End date must be provided")
        LocalDate endDate,

        @NotBlank(message = "Reason must not be blank")
        String reason,

        @NotNull(message = "Justification type must be provided")
        JustificationTypeEnum type,

        @Size(max = 500, message = "Attachment URL must not exceed 500 characters")
        String attachmentUrl
) {
    @AssertTrue(message = "End date must be on or after start date")
    private boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }
}

package com.project.ponto.dto;

import jakarta.validation.constraints.Size;

public record JustificationRejectionRequest(
        @Size(max = 255, message = "Rejection reason must not exceed 255 characters")
        String rejectionReason
) {}

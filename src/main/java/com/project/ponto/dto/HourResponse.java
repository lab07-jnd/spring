package com.project.ponto.dto;

import com.project.ponto.domain.model.HourBank;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HourResponse(
        Long id,

        int balanceMinutes,

        int previousBalanceMinutes,

        LocalDate periodStart,

        LocalDate periodEnd,

        LocalDate expirationDate,

        LocalDateTime lastUpdatedAt
        ) {

    public static HourResponse from(HourBank h) {
        return new HourResponse(
                h.getId(),
                h.getBalanceMinutes(),
                h.getPreviousBalanceMinutes(),
                h.getPeriodStart(),
                h.getPeriodEnd(),
                h.getExpirationDate(),
                h.getLastUpdatedAt()

        );
    }
}

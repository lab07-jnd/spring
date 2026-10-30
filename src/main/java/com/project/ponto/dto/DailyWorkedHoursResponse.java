package com.project.ponto.dto;

import java.time.LocalDate;


public record DailyWorkedHoursResponse(
        LocalDate date,
        long workedMinutes,
        long expectedMinutes,
        long overtimeMinutes,
        long shortfallMinutes,
        boolean complete
) {}

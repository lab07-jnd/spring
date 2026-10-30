package com.project.ponto.dto;

import java.time.LocalDate;
import java.util.List;

public record WorkedHoursResponse(
        Long employeeId,
        LocalDate startDate,
        LocalDate endDate,
        List<DailyWorkedHoursResponse> days,
        long totalWorkedMinutes,
        long totalOvertimeMinutes
) {}
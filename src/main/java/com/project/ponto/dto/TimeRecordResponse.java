package com.project.ponto.dto;

import java.time.LocalDateTime;

import com.project.ponto.domain.enums.TimeRecordOriginEnum;
import com.project.ponto.domain.enums.TimeRecordTypeEnum;

public record TimeRecordResponse(
        Long id,
        LocalDateTime dateTime,
        TimeRecordTypeEnum type,
        TimeRecordOriginEnum origin,
        String notes,
        LocalDateTime createdAt,
        Long contractId,
        Long justificationId
) {}

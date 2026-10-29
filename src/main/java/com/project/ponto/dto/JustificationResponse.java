package com.project.ponto.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.project.ponto.domain.enums.ApprovalStatusEnum;
import com.project.ponto.domain.enums.JustificationTypeEnum;
import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.Justification;

public record JustificationResponse(
        Long id,
        Long employeeId,
        String employeeName,
        Long approverId,
        String approverName,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        String rejectionReason,
        JustificationTypeEnum type,
        ApprovalStatusEnum status,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        String attachmentUrl
) {

    public static JustificationResponse from(Justification justification) {
        Employee employee = justification.getEmployee();
        Employee approver = justification.getApprover();

        return new JustificationResponse(
                justification.getId(),
                employee != null ? employee.getId() : null,
                employee != null ? employee.getName() : null,
                approver != null ? approver.getId() : null,
                approver != null ? approver.getName() : null,
                justification.getStartDate(),
                justification.getEndDate(),
                justification.getReason(),
                justification.getRejectionReason(),
                justification.getType(),
                justification.getStatus(),
                justification.getRequestedAt(),
                justification.getApprovedAt(),
                justification.getAttachmentUrl()
        );
    }
}

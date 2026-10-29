package com.project.ponto.service.strategy;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.project.ponto.domain.enums.ApprovalStatusEnum;
import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.Justification;

@Component
public class ApproveJustificationHandler implements JustificationTransitionHandler {

    @Override
    public ApprovalStatusEnum targetStatus() {
        return ApprovalStatusEnum.APPROVED;
    }

    @Override
    public void process(Justification justification, Employee approver, String notes) {
        justification.setStatus(ApprovalStatusEnum.APPROVED);
        justification.setApprover(approver);
        justification.setApprovedAt(LocalDateTime.now());
    }
}

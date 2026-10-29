package com.project.ponto.service.strategy;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.project.ponto.domain.enums.ApprovalStatusEnum;
import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.Justification;

@Component
public class RejectJustificationHandler implements JustificationTransitionHandler {

    @Override
    public ApprovalStatusEnum targetStatus() {
        return ApprovalStatusEnum.REJECTED;
    }

    @Override
    public void process(Justification justification, Employee approver, String notes) {
        justification.setStatus(ApprovalStatusEnum.REJECTED);
        justification.setApprover(approver);
        justification.setApprovedAt(LocalDateTime.now());
        justification.setRejectionReason(notes);
    }
}

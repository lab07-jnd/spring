package com.project.ponto.service.strategy;

import com.project.ponto.domain.enums.ApprovalStatusEnum;
import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.Justification;

public interface JustificationTransitionHandler {

    ApprovalStatusEnum targetStatus();

    void process(Justification justification, Employee approver, String notes);
}

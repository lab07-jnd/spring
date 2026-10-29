package com.project.ponto.service;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ponto.domain.enums.ApprovalStatusEnum;
import com.project.ponto.domain.enums.EmployeeStatusEnum;
import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.Justification;
import com.project.ponto.dto.JustificationRejectionRequest;
import com.project.ponto.dto.JustificationRequest;
import com.project.ponto.dto.JustificationResponse;
import com.project.ponto.exception.ConflictException;
import com.project.ponto.exception.ResourceNotFoundException;
import com.project.ponto.repository.EmployeeRepository;
import com.project.ponto.repository.JustificationRepository;
import com.project.ponto.service.strategy.JustificationTransitionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JustificationService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final Collection<ApprovalStatusEnum> BLOCKING_STATUSES =
            EnumSet.of(ApprovalStatusEnum.PENDING, ApprovalStatusEnum.APPROVED);

    private final JustificationRepository justificationRepository;
    private final EmployeeRepository employeeRepository;
    private final Validator validator;
    private final List<JustificationTransitionHandler> transitionHandlers;

    @Transactional
    public JustificationResponse create(Long employeeId, JustificationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Justification request must be provided");
        }
        validate(request);

        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID must be provided");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        if (employee.getStatus() != EmployeeStatusEnum.ACTIVE) {
            throw new ConflictException("Employee is not active. Current status: " + employee.getStatus());
        }

        boolean overlaps = justificationRepository
                .existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employeeId, BLOCKING_STATUSES, request.endDate(), request.startDate());

        if (overlaps) {
            throw new ConflictException(
                    "There is already a pending or approved justification overlapping the requested period");
        }

        Justification justification = new Justification();
        justification.setEmployee(employee);
        justification.setStartDate(request.startDate());
        justification.setEndDate(request.endDate());
        justification.setReason(request.reason());
        justification.setType(request.type());
        justification.setAttachmentUrl(request.attachmentUrl());
        justification.setStatus(ApprovalStatusEnum.PENDING);

        return JustificationResponse.from(justificationRepository.save(justification));
    }

    @Transactional(readOnly = true)
    public List<JustificationResponse> listByEmployee(Long employeeId, Integer page, Integer size) {
        Pageable pageable = toPageable(page, size);

        if (employeeId == null || !employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }

        return justificationRepository.findByEmployeeIdOrderByRequestedAtDescIdDesc(employeeId, pageable)
                .stream()
                .map(JustificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JustificationResponse> listPending(Integer page, Integer size) {
        Pageable pageable = toPageable(page, size);

        return justificationRepository.findByStatusOrderByRequestedAtAscIdAsc(ApprovalStatusEnum.PENDING, pageable)
                .stream()
                .map(JustificationResponse::from)
                .toList();
    }

    @Transactional
    public JustificationResponse approve(Long managerId, Long justificationId) {
        return transition(managerId, justificationId, ApprovalStatusEnum.APPROVED, null);
    }

    @Transactional
    public JustificationResponse reject(Long managerId, Long justificationId, String rejectionReason) {
        validate(new JustificationRejectionRequest(rejectionReason));

        return transition(managerId, justificationId, ApprovalStatusEnum.REJECTED, rejectionReason);
    }

    private JustificationResponse transition(Long managerId, Long justificationId,
            ApprovalStatusEnum targetStatus, String notes) {
        if (managerId == null) {
            throw new IllegalArgumentException("Manager ID must be provided");
        }

        Justification justification = justificationRepository.findById(justificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Justification not found with id: " + justificationId));

        if (justification.getStatus() != ApprovalStatusEnum.PENDING) {
            throw new ConflictException(
                    "Justification is not pending. Current status: " + justification.getStatus());
        }

        Long employeeId = justification.getEmployee() != null ? justification.getEmployee().getId() : null;
        if (managerId.equals(employeeId)) {
            throw new ConflictException("A manager cannot approve or reject their own justification");
        }

        Employee approver = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + managerId));

        JustificationTransitionHandler handler = transitionHandlers.stream()
                .filter(candidate -> candidate.targetStatus() == targetStatus)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No transition handler configured for " + targetStatus));

        handler.process(justification, approver, notes);

        return JustificationResponse.from(justificationRepository.save(justification));
    }

    private <T> void validate(T target) {
        Set<ConstraintViolation<T>> violations = validator.validate(target);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(message);
        }
    }

    private static Pageable toPageable(Integer page, Integer size) {
        if (page == null) {
            return Pageable.unpaged();
        }
        if (page < 1) {
            throw new IllegalArgumentException("A página deve ser maior ou igual a 1");
        }
        int pageSize = (size != null && size > 0) ? size : DEFAULT_PAGE_SIZE;
        return PageRequest.of(page - 1, pageSize);
    }
}

package com.project.ponto.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ponto.domain.enums.ApprovalStatusEnum;
import com.project.ponto.domain.model.Justification;

public interface JustificationRepository extends JpaRepository<Justification, Long> {

    boolean existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId,
            Collection<ApprovalStatusEnum> statuses,
            LocalDate endDate,
            LocalDate startDate
    );

    List<Justification> findByEmployeeIdOrderByRequestedAtDescIdDesc(Long employeeId, Pageable pageable);

    List<Justification> findByStatusOrderByRequestedAtAscIdAsc(ApprovalStatusEnum status, Pageable pageable);
}

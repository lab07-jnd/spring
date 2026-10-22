package com.project.ponto.repository;

import com.project.ponto.domain.enums.ContractStatusEnum;
import com.project.ponto.domain.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findFirstByEmployeeIdAndStatus(Long employeeId, ContractStatusEnum status);

    boolean existsByEmployeeId(Long employeeId);
}

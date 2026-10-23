package com.project.ponto.repository;
import  com.project.ponto.domain.model.HourBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HourBankRepository extends JpaRepository<HourBank, Long> {
    List<HourBank> findByEmployeeId(Long employeeId);
    
}

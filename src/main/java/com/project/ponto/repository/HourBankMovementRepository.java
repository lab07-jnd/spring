package com.project.ponto.repository;

import com.project.ponto.domain.model.HourBankMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HourBankMovementRepository extends JpaRepository<HourBankMovement, Long> {
}

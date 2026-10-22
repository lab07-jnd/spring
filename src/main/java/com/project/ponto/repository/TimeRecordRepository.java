package com.project.ponto.repository;

import com.project.ponto.domain.model.TimeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeRecordRepository extends JpaRepository<TimeRecord, Long> {

    List<TimeRecord> findByContractIdAndDateTimeBetweenOrderByDateTimeAsc(
            Long contractId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    Optional<TimeRecord> findTopByContractIdOrderByDateTimeDesc(Long contractId);

    Optional<TimeRecord> findTopByContractIdAndDateTimeBetweenOrderByDateTimeDesc(
            Long contractId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );
}

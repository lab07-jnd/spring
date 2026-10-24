package com.project.ponto.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.project.ponto.domain.model.TimeRecord;
import com.project.ponto.dto.TimeRecordResponse;

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

    // Janela semiaberta [start, endExclusive): quem chama passa o dia seguinte ao último, às 00:00.
    // t.id desempata pontos com o mesmo dateTime, para páginas consecutivas não se sobreporem.
    // A ordem dos argumentos do "select new" segue o construtor canônico de TimeRecordResponse.
    @Query("""
            select new com.project.ponto.dto.TimeRecordResponse(
                t.id, e.id, e.name, c.id, t.dateTime, t.type, t.origin,
                t.geolocation.latitude, t.geolocation.longitude,
                t.notes, t.createdAt, j.id)
            from TimeRecord t
            join t.contract c
            join c.employee e
            left join t.justification j
            where e.id = :employeeId
              and t.dateTime >= :start
              and t.dateTime < :endExclusive
            order by t.dateTime asc, t.id asc
            """)
    List<TimeRecordResponse> findByEmployeeAndPeriod(Long employeeId, LocalDateTime start,
            LocalDateTime endExclusive, Pageable pageable);
}
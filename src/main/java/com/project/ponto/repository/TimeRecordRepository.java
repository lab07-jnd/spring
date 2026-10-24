package com.project.ponto.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.project.ponto.domain.model.TimeRecord;
import com.project.ponto.dto.TimeRecordResponse;

public interface TimeRecordRepository extends JpaRepository<TimeRecord, Long> {

    // Janela semiaberta [start, endExclusive): quem chama passa o dia seguinte ao último, às 00:00.
    // t.id desempata pontos com o mesmo dateTime, para páginas consecutivas não se sobreporem.
    @Query("""
            select new com.project.ponto.dto.TimeRecordResponse(
                t.id, t.dateTime, t.type, t.origin, t.notes, t.createdAt, c.id, j.id)
            from TimeRecord t
            join t.contract c
            left join t.justification j
            where c.employee.id = :employeeId
              and t.dateTime >= :start
              and t.dateTime < :endExclusive
            order by t.dateTime asc, t.id asc
            """)
    List<TimeRecordResponse> findByEmployeeAndPeriod(Long employeeId, LocalDateTime start,
            LocalDateTime endExclusive, Pageable pageable);
}

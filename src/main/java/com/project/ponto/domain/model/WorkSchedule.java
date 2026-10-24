package com.project.ponto.domain.model;

import com.project.ponto.domain.enums.DayOfWeekEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalTime startTime;

    private LocalTime endTime;

    private int dailyWorkloadMinutes;

    private int breakMinutes;

    // Minutos que a entrada pode passar do startTime sem virar atraso (padrão: 5)
    private int lateToleranceMinutes = 5;

    @ElementCollection(targetClass = DayOfWeekEnum.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "work_schedule_worked_days", joinColumns = @JoinColumn(name = "work_schedule_id"))
    @Column(name = "day_of_week")
    private List<DayOfWeekEnum> workedDays;

    private boolean active;
}

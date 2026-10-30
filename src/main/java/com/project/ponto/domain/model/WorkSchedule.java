package com.project.ponto.domain.model;

import com.project.ponto.domain.enums.DayOfWeekEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
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

    // Minutos que a entrada pode passar do startTime sem virar atraso
    private int lateToleranceMinutes = 5;

    @ElementCollection(targetClass = DayOfWeekEnum.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "work_schedule_worked_days", joinColumns = @JoinColumn(name = "work_schedule_id"))
    @Column(name = "day_of_week")
    private List<DayOfWeekEnum> workedDays;

    private boolean active;

    // Minutos de atraso de uma entrada (CLOCK_IN): passou da tolerância, conta o atraso inteiro, não só o
    // excedente. Só minutos completos contam (08:10:59 = 10 min). Jornada que atravessa a meia-noite fica
    // fora do escopo: o horário esperado é o startTime no mesmo dia da entrada
    public int lateMinutesAt(LocalDateTime clockIn) {
        if (!active || !isWorkedDay(clockIn.getDayOfWeek())) {
            return 0;
        }
        long minutesAfterStart = Duration.between(clockIn.toLocalDate().atTime(startTime), clockIn).toMinutes();
        return minutesAfterStart > lateToleranceMinutes ? (int) minutesAfterStart : 0;
    }

    // DayOfWeekEnum usa os mesmos nomes de java.time.DayOfWeek
    private boolean isWorkedDay(DayOfWeek day) {
        return workedDays.contains(DayOfWeekEnum.valueOf(day.name()));
    }
}

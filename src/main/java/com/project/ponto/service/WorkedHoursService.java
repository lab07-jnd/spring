package com.project.ponto.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ponto.domain.enums.ContractStatusEnum;
import com.project.ponto.domain.enums.DayOfWeekEnum;
import com.project.ponto.domain.enums.TimeRecordTypeEnum;
import com.project.ponto.domain.model.Contract;
import com.project.ponto.domain.model.TimeRecord;
import com.project.ponto.domain.model.WorkSchedule;
import com.project.ponto.dto.DailyWorkedHoursResponse;
import com.project.ponto.dto.WorkedHoursResponse;
import com.project.ponto.exception.ResourceNotFoundException;
import com.project.ponto.repository.ContractRepository;
import com.project.ponto.repository.EmployeeRepository;
import com.project.ponto.repository.TimeRecordRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class WorkedHoursService {

    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;

    @Transactional(readOnly = true)
    public WorkedHoursResponse calculate(Long employeeId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Enter startDate and endDate, with the start date no later than the end date");
        }
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }

        Contract contract = contractRepository
                .findFirstByEmployeeIdAndStatus(employeeId, ContractStatusEnum.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee without active contact"));
        WorkSchedule schedule = contract.getWorkSchedule();

        List<TimeRecord> records = timeRecordRepository
                .findByContractIdAndDateTimeBetweenOrderByDateTimeAsc(
                        contract.getId(), startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

        Map<LocalDate, List<TimeRecord>> byDay = records.stream()
                .collect(Collectors.groupingBy(r -> r.getDateTime().toLocalDate(), TreeMap::new, Collectors.toList()));

        List<DailyWorkedHoursResponse> days = byDay.entrySet().stream()
                .map(e -> summarize(e.getKey(), e.getValue(), schedule))
                .toList();

        return new WorkedHoursResponse(employeeId, startDate, endDate, days,
                days.stream().mapToLong(DailyWorkedHoursResponse::workedMinutes).sum(),
                days.stream().mapToLong(DailyWorkedHoursResponse::overtimeMinutes).sum());
    }

    private DailyWorkedHoursResponse summarize(LocalDate date, List<TimeRecord> dayRecords, WorkSchedule schedule) {
        Duration worked = Duration.ZERO;
        LocalDateTime openSince = null;

        for (TimeRecord record : dayRecords) {
            switch (record.getType()) {
                case CLOCK_IN, LUNCH_IN -> openSince = record.getDateTime();
                case LUNCH_OUT, CLOCK_OUT -> {
                    if (openSince != null) {
                        worked = worked.plus(Duration.between(openSince, record.getDateTime()));
                        openSince = null;
                    }
                }
            }
        }

        // converte no fim para não perder ate 59 seg por intervalo
        long workedMinutes = worked.toMinutes();
        boolean complete = dayRecords.getLast().getType() == TimeRecordTypeEnum.CLOCK_OUT;

        boolean workday = schedule.getWorkedDays()
                .contains(DayOfWeekEnum.valueOf(date.getDayOfWeek().name()));
        long expected = workday ? schedule.getDailyWorkloadMinutes() : 0;

        long diff = workedMinutes - expected;
        long overtime = complete ? Math.max(0, diff) : 0;
        long shortfall = complete ? Math.max(0, -diff) : 0;

        return new DailyWorkedHoursResponse(date, workedMinutes, expected, overtime, shortfall, complete);
    }
}
package com.project.ponto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.ponto.domain.enums.ContractStatusEnum;
import com.project.ponto.domain.enums.DayOfWeekEnum;
import com.project.ponto.domain.enums.EmployeeStatusEnum;
import com.project.ponto.domain.enums.TimeRecordOriginEnum;
import com.project.ponto.domain.enums.TimeRecordTypeEnum;
import com.project.ponto.domain.model.Contract;
import com.project.ponto.domain.model.Employee;
import com.project.ponto.domain.model.TimeRecord;
import com.project.ponto.domain.model.WorkSchedule;
import com.project.ponto.dto.TimeRecordRequest;
import com.project.ponto.dto.TimeRecordResponse;
import com.project.ponto.repository.ContractRepository;
import com.project.ponto.repository.EmployeeRepository;
import com.project.ponto.repository.TimeRecordRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimeRecordService - late minutes on register")
class TimeRecordServiceLatenessTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    // 2026-10-26 is a Monday
    private static final LocalDateTime MONDAY_08_12 = LocalDateTime.of(2026, 10, 26, 8, 12);
    private static final Long EMPLOYEE_ID = 7L;
    private static final Long CONTRACT_ID = 3L;

    @Mock
    private TimeRecordRepository timeRecordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ContractRepository contractRepository;

    // Starts at 08:00, Monday to Friday, with the default tolerance (5 minutes)
    private WorkSchedule schedule;

    @BeforeEach
    void setUp() {
        schedule = new WorkSchedule();
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setWorkedDays(List.of(DayOfWeekEnum.MONDAY, DayOfWeekEnum.TUESDAY, DayOfWeekEnum.WEDNESDAY,
                DayOfWeekEnum.THURSDAY, DayOfWeekEnum.FRIDAY));
        schedule.setActive(true);

        Employee employee = new Employee();
        employee.setId(EMPLOYEE_ID);
        employee.setStatus(EmployeeStatusEnum.ACTIVE);

        Contract contract = new Contract();
        contract.setId(CONTRACT_ID);
        contract.setEmployee(employee);
        contract.setWorkSchedule(schedule);

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(contractRepository.findFirstByEmployeeIdAndStatus(EMPLOYEE_ID, ContractStatusEnum.ACTIVE))
                .thenReturn(Optional.of(contract));
        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // The unstubbed record queries return Optional.empty(): no record yet today
    }

    @Test
    @DisplayName("Must store the whole delay on the first clock-in of the day")
    void mustStoreWholeDelayOnFirstClockInOfDay() {
        serviceAt(MONDAY_08_12).register(clockIn());

        assertThat(savedRecord().getLateMinutes()).isEqualTo(12);
    }

    @Test
    @DisplayName("Must return the late minutes in the response")
    void mustReturnLateMinutesInResponse() {
        TimeRecordResponse response = serviceAt(MONDAY_08_12).register(clockIn());

        assertThat(response.lateMinutes()).isEqualTo(12);
    }

    @Test
    @DisplayName("Must apply the tolerance of the employee's work schedule")
    void mustApplyToleranceOfWorkSchedule() {
        schedule.setLateToleranceMinutes(15);

        serviceAt(MONDAY_08_12).register(clockIn());

        assertThat(savedRecord().getLateMinutes()).isZero();
    }

    @Test
    @DisplayName("Must not count lateness on a clock-in after a clock-out of the same day")
    void mustNotCountLatenessOnClockInAfterClockOutOfSameDay() {
        TimeRecord clockOut = new TimeRecord();
        clockOut.setType(TimeRecordTypeEnum.CLOCK_OUT);
        clockOut.setDateTime(LocalDateTime.of(2026, 10, 26, 12, 0));
        when(timeRecordRepository.findTopByContractIdAndDateTimeBetweenOrderByDateTimeDesc(eq(CONTRACT_ID), any(), any()))
                .thenReturn(Optional.of(clockOut));

        serviceAt(LocalDateTime.of(2026, 10, 26, 13, 0)).register(clockIn());

        assertThat(savedRecord().getLateMinutes()).isZero();
    }

    private TimeRecord savedRecord() {
        ArgumentCaptor<TimeRecord> saved = ArgumentCaptor.forClass(TimeRecord.class);
        verify(timeRecordRepository).save(saved.capture());
        return saved.getValue();
    }

    private static TimeRecordRequest clockIn() {
        return new TimeRecordRequest(EMPLOYEE_ID, TimeRecordTypeEnum.CLOCK_IN, TimeRecordOriginEnum.APP, null, null, null);
    }

    private TimeRecordService serviceAt(LocalDateTime saoPauloTime) {
        return new TimeRecordService(timeRecordRepository, employeeRepository, contractRepository,
                Clock.fixed(saoPauloTime.atZone(SAO_PAULO).toInstant(), SAO_PAULO));
    }
}

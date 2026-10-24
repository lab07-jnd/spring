package com.project.ponto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.project.ponto.domain.enums.TimeRecordOriginEnum;
import com.project.ponto.domain.enums.TimeRecordTypeEnum;
import com.project.ponto.dto.TimeRecordResponse;
import com.project.ponto.exception.ResourceNotFoundException;
import com.project.ponto.repository.EmployeeRepository;
import com.project.ponto.repository.TimeRecordRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimeRecordService - list by employee and period")
class TimeRecordServiceTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    // 2024-02-15 12:00 in Sao Paulo. A month far from the real one, so a service that
    // ignores the injected Clock (e.g. LocalDate.now()) computes another window and fails
    private static final Instant MID_FEBRUARY_2024 = Instant.parse("2024-02-15T15:00:00Z");
    private static final Long EMPLOYEE_ID = 7L;

    @Mock
    private TimeRecordRepository timeRecordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private TimeRecordService service;

    @BeforeEach
    void setUp() {
        service = serviceAt(MID_FEBRUARY_2024);
    }

    @Test
    @DisplayName("Must throw not found when the employee does not exist")
    void mustThrowNotFoundWhenEmployeeDoesNotExist() {
        when(employeeRepository.existsById(EMPLOYEE_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.listByEmployee(EMPLOYEE_ID, null, null, null, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Funcionário não encontrado com id: 7");
    }

    @Test
    @DisplayName("Must return the records found by the repository")
    void mustReturnRecordsFoundByRepository() {
        givenEmployeeExists();
        List<TimeRecordResponse> found = List.of(new TimeRecordResponse(
                1L, LocalDateTime.of(2026, 10, 10, 8, 0), TimeRecordTypeEnum.CLOCK_IN, TimeRecordOriginEnum.WEB,
                null, LocalDateTime.of(2026, 10, 10, 8, 0, 1), 3L, null));
        when(timeRecordRepository.findByEmployeeAndPeriod(eq(EMPLOYEE_ID), any(), any(), any())).thenReturn(found);

        List<TimeRecordResponse> result = service.listByEmployee(
                EMPLOYEE_ID, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), null, null);

        assertThat(result).isEqualTo(found);
    }

    @Test
    @DisplayName("Must return an empty list, not not found, when the employee exists but has no records")
    void mustReturnEmptyListWhenEmployeeHasNoRecords() {
        givenEmployeeExists();
        when(timeRecordRepository.findByEmployeeAndPeriod(eq(EMPLOYEE_ID), any(), any(), any())).thenReturn(List.of());

        List<TimeRecordResponse> result = service.listByEmployee(EMPLOYEE_ID, null, null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Must default to the current month when no dates are given")
    void mustDefaultToCurrentMonth() {
        givenEmployeeExists();

        service.listByEmployee(EMPLOYEE_ID, null, null, null, null);

        verify(timeRecordRepository).findByEmployeeAndPeriod(eq(EMPLOYEE_ID),
                eq(LocalDateTime.of(2024, 2, 1, 0, 0)), eq(LocalDateTime.of(2024, 3, 1, 0, 0)), any());
    }

    @Test
    @DisplayName("Must compute the default month in Sao Paulo time, not UTC")
    void mustComputeDefaultMonthInSaoPauloTime() {
        givenEmployeeExists();
        // 2024-02-29 22:00 in Sao Paulo (leap day) is already 2024-03-01 01:00 in UTC
        TimeRecordService leapDayNight = serviceAt(Instant.parse("2024-03-01T01:00:00Z"));

        leapDayNight.listByEmployee(EMPLOYEE_ID, null, null, null, null);

        verify(timeRecordRepository).findByEmployeeAndPeriod(eq(EMPLOYEE_ID),
                eq(LocalDateTime.of(2024, 2, 1, 0, 0)), eq(LocalDateTime.of(2024, 3, 1, 0, 0)), any());
    }

    @Test
    @DisplayName("Must turn the given dates into [startDate 00:00, endDate + 1 day 00:00)")
    void mustTurnGivenDatesIntoHalfOpenWindow() {
        givenEmployeeExists();

        service.listByEmployee(EMPLOYEE_ID, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 10), null, null);

        verify(timeRecordRepository).findByEmployeeAndPeriod(eq(EMPLOYEE_ID),
                eq(LocalDateTime.of(2026, 10, 5, 0, 0)), eq(LocalDateTime.of(2026, 10, 11, 0, 0)), any());
    }

    @Test
    @DisplayName("Must accept equal start and end dates as a single-day window")
    void mustAcceptSingleDay() {
        givenEmployeeExists();

        service.listByEmployee(EMPLOYEE_ID, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 10), null, null);

        verify(timeRecordRepository).findByEmployeeAndPeriod(eq(EMPLOYEE_ID),
                eq(LocalDateTime.of(2026, 10, 10, 0, 0)), eq(LocalDateTime.of(2026, 10, 11, 0, 0)), any());
    }

    @ParameterizedTest(name = "startDate={0}, endDate={1}")
    @MethodSource("onlyOneDate")
    @DisplayName("Must reject a request with only one of the dates")
    void mustRejectOnlyOneDate(LocalDate startDate, LocalDate endDate) {
        assertThatThrownBy(() -> service.listByEmployee(EMPLOYEE_ID, startDate, endDate, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Must reject a start date after the end date")
    void mustRejectStartAfterEnd() {
        assertThatThrownBy(() -> service.listByEmployee(
                EMPLOYEE_ID, LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 10), null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "page={0}")
    @ValueSource(ints = {0, -1})
    @DisplayName("Must reject a page lower than 1")
    void mustRejectPageLowerThanOne(int page) {
        assertThatThrownBy(() -> service.listByEmployee(EMPLOYEE_ID, null, null, page, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A página deve ser maior ou igual a 1");
    }

    @Test
    @DisplayName("Must convert the 1-based page into a 0-based PageRequest")
    void mustConvertOneBasedPage() {
        givenEmployeeExists();

        service.listByEmployee(EMPLOYEE_ID, null, null, 2, 5);

        verify(timeRecordRepository).findByEmployeeAndPeriod(eq(EMPLOYEE_ID), any(), any(), eq(PageRequest.of(1, 5)));
    }

    @ParameterizedTest(name = "size={0}")
    @NullSource
    @ValueSource(ints = {0, -1})
    @DisplayName("Must fall back to a page size of 10 when size is missing or not positive")
    void mustDefaultPageSizeToTen(Integer size) {
        givenEmployeeExists();

        service.listByEmployee(EMPLOYEE_ID, null, null, 1, size);

        verify(timeRecordRepository).findByEmployeeAndPeriod(eq(EMPLOYEE_ID), any(), any(), eq(PageRequest.of(0, 10)));
    }

    @Test
    @DisplayName("Must not paginate when no page is given, even with a size")
    void mustNotPaginateWithoutPage() {
        givenEmployeeExists();

        service.listByEmployee(EMPLOYEE_ID, null, null, null, 5);

        verify(timeRecordRepository).findByEmployeeAndPeriod(eq(EMPLOYEE_ID), any(), any(),
                argThat(pageable -> pageable != null && pageable.isUnpaged()));
    }

    static Stream<Arguments> onlyOneDate() {
        return Stream.of(
                Arguments.of(LocalDate.of(2026, 10, 5), null),
                Arguments.of(null, LocalDate.of(2026, 10, 10)));
    }

    private void givenEmployeeExists() {
        when(employeeRepository.existsById(EMPLOYEE_ID)).thenReturn(true);
    }

    private TimeRecordService serviceAt(Instant now) {
        return new TimeRecordService(timeRecordRepository, employeeRepository, Clock.fixed(now, SAO_PAULO));
    }
}
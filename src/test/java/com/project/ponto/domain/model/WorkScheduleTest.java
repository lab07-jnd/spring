package com.project.ponto.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.project.ponto.domain.enums.DayOfWeekEnum;

@DisplayName("WorkSchedule - late tolerance rule")
class WorkScheduleTest {

    // 2026-10-19 is a Monday and 2026-10-24 a Saturday
    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 19);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 10, 24);

    @Nested
    @DisplayName("1. Default Tolerance")
    class DefaultTolerance {

        @Test
        @DisplayName("Must tolerate up to 5 minutes when the tolerance is not configured")
        void mustTolerateFiveMinutesByDefault() {
            WorkSchedule schedule = mondayToFridayAtEight();

            assertThat(schedule.lateMinutesAt(MONDAY.atTime(8, 5))).isZero();
            assertThat(schedule.lateMinutesAt(MONDAY.atTime(8, 6))).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("2. Clock-in On Time or Within the Tolerance")
    class WithinTolerance {

        @Test
        @DisplayName("Must not count lateness when clocking in on time, even without tolerance")
        void mustNotCountLatenessWhenOnTime() {
            assertThat(scheduleWithTolerance(0).lateMinutesAt(MONDAY.atTime(8, 0))).isZero();
        }

        @Test
        @DisplayName("Must not count lateness when clocking in before the start time")
        void mustNotCountLatenessWhenEarly() {
            assertThat(scheduleWithTolerance(0).lateMinutesAt(MONDAY.atTime(7, 45))).isZero();
        }

        @Test
        @DisplayName("Must not count lateness when clocking in exactly at the tolerance limit")
        void mustNotCountLatenessAtToleranceLimit() {
            assertThat(scheduleWithTolerance(10).lateMinutesAt(MONDAY.atTime(8, 10))).isZero();
        }

        @Test
        @DisplayName("Must ignore the seconds of the last tolerated minute (08:10:59 is 10 minutes)")
        void mustIgnoreSecondsOfLastToleratedMinute() {
            assertThat(scheduleWithTolerance(10).lateMinutesAt(MONDAY.atTime(8, 10, 59))).isZero();
        }
    }

    @Nested
    @DisplayName("3. Clock-in After the Tolerance")
    class AfterTolerance {

        @Test
        @DisplayName("Must count the whole delay, not only the excess, one minute after the tolerance")
        void mustCountWholeDelayOneMinuteAfterTolerance() {
            assertThat(scheduleWithTolerance(10).lateMinutesAt(MONDAY.atTime(8, 11))).isEqualTo(11);
        }

        @Test
        @DisplayName("Must count only the whole minutes of the delay (08:11:59 is 11 minutes)")
        void mustCountOnlyWholeMinutesOfDelay() {
            assertThat(scheduleWithTolerance(10).lateMinutesAt(MONDAY.atTime(8, 11, 59))).isEqualTo(11);
        }
    }

    @Nested
    @DisplayName("4. Worked Days and Inactive Schedule")
    class WorkedDaysAndStatus {

        @ParameterizedTest(name = "{0} on {1}")
        @CsvSource({
                "MONDAY, 2026-10-19",
                "TUESDAY, 2026-10-20",
                "WEDNESDAY, 2026-10-21",
                "THURSDAY, 2026-10-22",
                "FRIDAY, 2026-10-23",
                "SATURDAY, 2026-10-24",
                "SUNDAY, 2026-10-25"
        })
        @DisplayName("Must match each worked day to the same day of the calendar")
        void mustMatchEachWorkedDayToCalendarDay(DayOfWeekEnum workedDay, LocalDate date) {
            WorkSchedule schedule = scheduleWithTolerance(10);
            schedule.setWorkedDays(List.of(workedDay));

            assertThat(schedule.lateMinutesAt(date.atTime(9, 0))).isEqualTo(60);
        }

        @Test
        @DisplayName("Must not count lateness on a day that is not worked")
        void mustNotCountLatenessOnDayNotWorked() {
            assertThat(scheduleWithTolerance(10).lateMinutesAt(SATURDAY.atTime(9, 0))).isZero();
        }

        @Test
        @DisplayName("Must not count lateness when the schedule is inactive")
        void mustNotCountLatenessWhenScheduleIsInactive() {
            WorkSchedule schedule = scheduleWithTolerance(10);
            schedule.setActive(false);

            assertThat(schedule.lateMinutesAt(MONDAY.atTime(9, 0))).isZero();
        }
    }

    // Starts at 08:00, Monday to Friday, with the default tolerance
    private static WorkSchedule mondayToFridayAtEight() {
        WorkSchedule schedule = new WorkSchedule();
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setWorkedDays(List.of(DayOfWeekEnum.MONDAY, DayOfWeekEnum.TUESDAY, DayOfWeekEnum.WEDNESDAY,
                DayOfWeekEnum.THURSDAY, DayOfWeekEnum.FRIDAY));
        schedule.setActive(true);
        return schedule;
    }

    private static WorkSchedule scheduleWithTolerance(int toleranceMinutes) {
        WorkSchedule schedule = mondayToFridayAtEight();
        schedule.setLateToleranceMinutes(toleranceMinutes);
        return schedule;
    }
}

-- V3: Add late tolerance to work_schedule table
--
-- Adds late_tolerance_minutes, mapped from WorkSchedule.lateToleranceMinutes:
-- how many minutes a clock-in may pass start_time without counting as late.
-- Existing schedules get the default of 5 minutes, the same default the
-- application uses.
-- =============================================================================

ALTER TABLE work_schedule
    ADD COLUMN late_tolerance_minutes INTEGER NOT NULL DEFAULT 5;

ALTER TABLE work_schedule
    ADD CONSTRAINT ck_work_schedule_late_tolerance_minutes
        CHECK (late_tolerance_minutes >= 0);

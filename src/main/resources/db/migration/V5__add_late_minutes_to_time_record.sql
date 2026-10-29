-- V5: Add late minutes to time_record table
--
-- Adds late_minutes, mapped from TimeRecord.lateMinutes: how many minutes late
-- the first clock-in of the day was, computed from the work schedule when the
-- record is registered. It is stored so that a later change to the schedule
-- does not rewrite the history. Other records, and the records that existed
-- before this migration, get 0.
-- =============================================================================

ALTER TABLE time_record
    ADD COLUMN late_minutes INTEGER NOT NULL DEFAULT 0;

ALTER TABLE time_record
    ADD CONSTRAINT ck_time_record_late_minutes
        CHECK (late_minutes >= 0);

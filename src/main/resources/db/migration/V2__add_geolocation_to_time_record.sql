-- V2: Add geolocation coordinates to time_record table
--
-- Adds latitude and longitude columns to time_record, mapped from the
-- Geolocation embeddable Value Object.
-- The columns are nullable because geolocation is optional.
-- Database CHECK constraints ensure valid geographic coordinate ranges.
-- =============================================================================

ALTER TABLE time_record
    ADD COLUMN latitude DOUBLE PRECISION,
    ADD COLUMN longitude DOUBLE PRECISION;

ALTER TABLE time_record
    ADD CONSTRAINT ck_time_record_latitude
        CHECK (latitude IS NULL OR (latitude >= -90.0 AND latitude <= 90.0)),
    ADD CONSTRAINT ck_time_record_longitude
        CHECK (longitude IS NULL OR (longitude >= -180.0 AND longitude <= 180.0));

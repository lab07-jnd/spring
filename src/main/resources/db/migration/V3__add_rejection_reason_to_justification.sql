-- V3: Add rejection_reason column to justification table
ALTER TABLE justification
    ADD COLUMN rejection_reason VARCHAR(255);

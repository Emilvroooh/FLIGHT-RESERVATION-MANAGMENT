-- Add passenger government verification ID fields to an existing web database.
-- Historical passengers retain NULL here; all new bookings require a valid ID in application validation.
USE flight_booking_web;

ALTER TABLE passengers
    ADD COLUMN verification_id_type VARCHAR(20) NULL AFTER passport_no,
    ADD COLUMN verification_id_number VARCHAR(32) NULL AFTER verification_id_type,
    ADD CONSTRAINT chk_passengers_verification_type
        CHECK (verification_id_type IS NULL OR verification_id_type IN ('PAN Card', 'Aadhaar Card', 'Passport'));

USE flight_booking_web;

ALTER TABLE flights
    ADD COLUMN flight_status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' AFTER arrival_time,
    ADD CONSTRAINT chk_flights_status CHECK (flight_status IN ('SCHEDULED','CLOSING_SOON','DEPARTED','LANDED'));

UPDATE flights
SET flight_status = CASE
    WHEN arrival_time <= CURRENT_TIMESTAMP THEN 'LANDED'
    WHEN departure_time <= CURRENT_TIMESTAMP THEN 'DEPARTED'
    WHEN departure_time <= DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 30 MINUTE) THEN 'CLOSING_SOON'
    ELSE 'SCHEDULED'
END;

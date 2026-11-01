-- Run once after migration_add_cabin_classes.sql on an existing database.
USE flight_booking;

ALTER TABLE payments
    ADD COLUMN refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN refund_date DATETIME(6) NULL,
    DROP CHECK chk_payments_status,
    ADD CONSTRAINT chk_payments_status CHECK (payment_status IN ('PENDING','PAID','FAILED','REFUNDED','PARTIALLY_REFUNDED'));

ALTER TABLE tickets ADD COLUMN cancelled_at DATETIME(6) NULL;

CREATE TABLE IF NOT EXISTS booking_cancellations (
    cancellation_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    booking_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    flight_id BIGINT UNSIGNED NOT NULL,
    flight_number VARCHAR(20) NOT NULL,
    departure_city VARCHAR(120) NOT NULL,
    destination_city VARCHAR(120) NOT NULL,
    departure_time DATETIME NOT NULL,
    cancelled_at DATETIME(6) NOT NULL,
    seconds_before_departure BIGINT UNSIGNED NOT NULL,
    original_amount DECIMAL(10,2) NOT NULL,
    refund_percentage DECIMAL(5,2) NOT NULL,
    refunded_amount DECIMAL(10,2) NOT NULL,
    refund_reason VARCHAR(240) NOT NULL,
    PRIMARY KEY (cancellation_id),
    UNIQUE KEY uq_booking_cancellations_booking (booking_id),
    KEY idx_booking_cancellations_user_date (user_id,cancelled_at),
    CONSTRAINT fk_booking_cancellations_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_booking_cancellations_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_booking_cancellations_flight FOREIGN KEY (flight_id) REFERENCES flights(flight_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_booking_cancellations_refund CHECK (refund_percentage BETWEEN 0 AND 100 AND refunded_amount BETWEEN 0 AND original_amount)
) ENGINE=InnoDB;

USE flight_booking;

CREATE TABLE IF NOT EXISTS cancellation_requests (
    request_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    booking_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    request_date DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    request_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    admin_response VARCHAR(500) NULL,
    reviewed_by BIGINT UNSIGNED NULL,
    reviewed_at DATETIME(6) NULL,
    PRIMARY KEY (request_id),
    KEY idx_cancellation_requests_status_date (request_status, request_date),
    KEY idx_cancellation_requests_booking (booking_id, request_id),
    CONSTRAINT fk_cancellation_requests_booking FOREIGN KEY (booking_id)
        REFERENCES bookings (booking_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_cancellation_requests_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_cancellation_requests_admin FOREIGN KEY (reviewed_by)
        REFERENCES admins (admin_id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT chk_cancellation_requests_status CHECK (request_status IN ('PENDING', 'APPROVED', 'REJECTED'))
) ENGINE=InnoDB;

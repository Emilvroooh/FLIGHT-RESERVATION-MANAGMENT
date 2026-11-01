-- Flight Booking System schema for MySQL 8.0+
-- Run with: mysql -u <user> -p < schema.sql

CREATE DATABASE IF NOT EXISTS flight_booking
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE flight_booking;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    phone VARCHAR(32) NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS airlines (
    airline_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    airline_name VARCHAR(120) NOT NULL,
    country VARCHAR(100) NOT NULL,
    contact_number VARCHAR(32) NULL,
    PRIMARY KEY (airline_id),
    UNIQUE KEY uq_airlines_name (airline_name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS flights (
    flight_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    airline_id BIGINT UNSIGNED NOT NULL,
    flight_number VARCHAR(20) NOT NULL,
    departure_city VARCHAR(120) NOT NULL,
    destination_city VARCHAR(120) NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    available_seats INT UNSIGNED NOT NULL,
    ticket_price DECIMAL(10,2) NOT NULL,
    economy_capacity INT UNSIGNED NOT NULL,
    economy_available_seats INT UNSIGNED NOT NULL,
    economy_ticket_price DECIMAL(10,2) NOT NULL,
    business_capacity INT UNSIGNED NOT NULL,
    business_available_seats INT UNSIGNED NOT NULL,
    business_ticket_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (flight_id),
    UNIQUE KEY uq_flights_number_departure (flight_number, departure_time),
    KEY idx_flights_route_time (departure_city, destination_city, departure_time),
    CONSTRAINT fk_flights_airline FOREIGN KEY (airline_id)
        REFERENCES airlines (airline_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_flights_cities CHECK (departure_city <> destination_city),
    CONSTRAINT chk_flights_times CHECK (arrival_time > departure_time),
    CONSTRAINT chk_flights_price CHECK (ticket_price = economy_ticket_price AND economy_ticket_price >= 0 AND business_ticket_price >= economy_ticket_price * 2.5 AND business_ticket_price <= economy_ticket_price * 3.0),
    CONSTRAINT chk_flights_class_seats CHECK (available_seats = economy_available_seats + business_available_seats AND economy_available_seats <= economy_capacity AND business_available_seats <= business_capacity)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bookings (
    booking_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    flight_id BIGINT UNSIGNED NOT NULL,
    booking_date DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (booking_id),
    KEY idx_bookings_user_date (user_id, booking_date),
    KEY idx_bookings_flight_status (flight_id, status),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_flight FOREIGN KEY (flight_id)
        REFERENCES flights (flight_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_bookings_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'FAILED')),
    CONSTRAINT chk_bookings_total CHECK (total_amount >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS passengers (
    passenger_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    booking_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(120) NOT NULL,
    age TINYINT UNSIGNED NOT NULL,
    gender VARCHAR(24) NOT NULL,
    passport_no VARCHAR(32) NULL,
    PRIMARY KEY (passenger_id),
    KEY idx_passengers_booking (booking_id),
    CONSTRAINT fk_passengers_booking FOREIGN KEY (booking_id)
        REFERENCES bookings (booking_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_passengers_age CHECK (age <= 120)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    booking_id BIGINT UNSIGNED NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATETIME(6) NULL DEFAULT NULL,
    payment_method VARCHAR(32) NOT NULL,
    payment_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    refund_date DATETIME(6) NULL,
    PRIMARY KEY (payment_id),
    UNIQUE KEY uq_payments_booking (booking_id),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id)
        REFERENCES bookings (booking_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_payments_amount CHECK (amount >= 0),
    CONSTRAINT chk_payments_status CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tickets (
    ticket_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ticket_number VARCHAR(40) NOT NULL,
    booking_id BIGINT UNSIGNED NOT NULL,
    flight_id BIGINT UNSIGNED NOT NULL,
    passenger_id BIGINT UNSIGNED NOT NULL,
    seat_number VARCHAR(8) NOT NULL,
    cabin_class VARCHAR(20) NOT NULL DEFAULT 'ECONOMY',
    ticket_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    ticket_status VARCHAR(24) NOT NULL DEFAULT 'ISSUED',
    cancelled_at DATETIME(6) NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ticket_id),
    UNIQUE KEY uq_tickets_number (ticket_number),
    UNIQUE KEY uq_tickets_passenger (passenger_id),
    KEY idx_tickets_flight_status_seat (flight_id, ticket_status, seat_number),
    KEY idx_tickets_booking (booking_id),
    CONSTRAINT fk_tickets_booking FOREIGN KEY (booking_id)
        REFERENCES bookings (booking_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_tickets_flight FOREIGN KEY (flight_id)
        REFERENCES flights (flight_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_passenger FOREIGN KEY (passenger_id)
        REFERENCES passengers (passenger_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_tickets_status CHECK (ticket_status IN ('ISSUED', 'CANCELLED', 'USED')),
    CONSTRAINT chk_tickets_cabin_class CHECK (cabin_class IN ('ECONOMY', 'BUSINESS')),
    CONSTRAINT chk_tickets_price CHECK (ticket_price >= 0)
) ENGINE=InnoDB;

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
    CONSTRAINT fk_booking_cancellations_booking_cascade FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_booking_cancellations_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_booking_cancellations_flight FOREIGN KEY (flight_id) REFERENCES flights(flight_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_booking_cancellations_refund CHECK (refund_percentage BETWEEN 0 AND 100 AND refunded_amount BETWEEN 0 AND original_amount)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS admins (
    admin_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (admin_id),
    UNIQUE KEY uq_admins_username (username)
) ENGINE=InnoDB;

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

-- Airline catalogue data. Run seed_airlines_flights.sql after this script
-- to add the 40 upcoming sample flights. No demo logins are seeded.
INSERT INTO airlines (airline_name, country, contact_number)
VALUES ('SkyWays', 'India', '+91-11-5555-0101'),
       ('Coastal Air', 'India', '+91-22-5555-0102'),
       ('AeroGlobal', 'United Arab Emirates', '+971-4-555-0301'),
       ('Horizon Airlines', 'India', '+91-80-5555-0401'),
       ('CloudCrest Air', 'India', '+91-20-5555-0501'),
       ('Apex Airways', 'United Kingdom', '+44-20-5555-0601')
ON DUPLICATE KEY UPDATE country = VALUES(country), contact_number = VALUES(contact_number);


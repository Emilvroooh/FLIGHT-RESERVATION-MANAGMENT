-- Run once for databases created before interactive seat selection.
-- The old unique key also blocked reuse of seats from cancelled tickets.
USE flight_booking;

-- Add the replacement first: MySQL requires an index beginning with flight_id
-- to remain in place for the tickets.flight_id foreign key.
SET @add_seat_lookup = IF(
    EXISTS (SELECT 1 FROM information_schema.statistics
            WHERE table_schema=DATABASE() AND table_name='tickets' AND index_name='idx_tickets_flight_status_seat'),
    'SELECT 1',
    'ALTER TABLE tickets ADD INDEX idx_tickets_flight_status_seat (flight_id, ticket_status, seat_number)'
);
PREPARE add_seat_lookup_stmt FROM @add_seat_lookup;
EXECUTE add_seat_lookup_stmt;
DEALLOCATE PREPARE add_seat_lookup_stmt;

SET @drop_old_seat_key = IF(
    EXISTS (SELECT 1 FROM information_schema.statistics
            WHERE table_schema=DATABASE() AND table_name='tickets' AND index_name='uq_tickets_flight_seat'),
    'ALTER TABLE tickets DROP INDEX uq_tickets_flight_seat',
    'SELECT 1'
);
PREPARE drop_seat_key_stmt FROM @drop_old_seat_key;
EXECUTE drop_seat_key_stmt;
DEALLOCATE PREPARE drop_seat_key_stmt;

-- Run once on an existing database to allow an admin to permanently delete a booking
-- together with its saved cancellation/refund row. Other child rows already cascade.
USE flight_booking;

ALTER TABLE booking_cancellations
    DROP FOREIGN KEY fk_booking_cancellations_booking;

ALTER TABLE booking_cancellations
    ADD CONSTRAINT fk_booking_cancellations_booking_cascade
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON UPDATE CASCADE ON DELETE CASCADE;

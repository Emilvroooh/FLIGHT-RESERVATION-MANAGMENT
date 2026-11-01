-- Run once in MySQL Workbench on an existing flight_booking database.
USE flight_booking;

ALTER TABLE flights
    ADD COLUMN economy_capacity INT UNSIGNED NOT NULL DEFAULT 0,
    ADD COLUMN economy_available_seats INT UNSIGNED NOT NULL DEFAULT 0,
    ADD COLUMN economy_ticket_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN business_capacity INT UNSIGNED NOT NULL DEFAULT 0,
    ADD COLUMN business_available_seats INT UNSIGNED NOT NULL DEFAULT 0,
    ADD COLUMN business_ticket_price DECIMAL(10,2) NOT NULL DEFAULT 0;

ALTER TABLE tickets
    ADD COLUMN cabin_class VARCHAR(20) NOT NULL DEFAULT 'ECONOMY',
    ADD COLUMN ticket_price DECIMAL(10,2) NOT NULL DEFAULT 0;

-- Existing flight ticket_price becomes the Economy fare. Business seats use
-- complete rows at the front, approximately one-third of total capacity.
UPDATE flights f
LEFT JOIN (
    SELECT flight_id, COUNT(*) AS occupied_count
    FROM tickets WHERE ticket_status IN ('ISSUED','USED') GROUP BY flight_id
) t ON t.flight_id=f.flight_id
SET f.business_capacity=LEAST(f.available_seats+COALESCE(t.occupied_count,0),
                              GREATEST(1,CEIL((f.available_seats+COALESCE(t.occupied_count,0))/18)*6)),
    f.economy_capacity=(f.available_seats+COALESCE(t.occupied_count,0))-
                       LEAST(f.available_seats+COALESCE(t.occupied_count,0),
                             GREATEST(1,CEIL((f.available_seats+COALESCE(t.occupied_count,0))/18)*6)),
    f.economy_ticket_price=f.ticket_price,
    f.business_ticket_price=ROUND(f.ticket_price*2.75,2);

UPDATE tickets t JOIN flights f ON f.flight_id=t.flight_id
SET t.cabin_class=CASE
        WHEN CAST(LEFT(t.seat_number,LENGTH(t.seat_number)-1) AS UNSIGNED)<=CEIL(f.business_capacity/6) THEN 'BUSINESS'
        ELSE 'ECONOMY' END,
    t.ticket_price=CASE
        WHEN CAST(LEFT(t.seat_number,LENGTH(t.seat_number)-1) AS UNSIGNED)<=CEIL(f.business_capacity/6) THEN f.business_ticket_price
        ELSE f.economy_ticket_price END;

UPDATE flights f
LEFT JOIN (
    SELECT flight_id,
           SUM(cabin_class='BUSINESS' AND ticket_status IN ('ISSUED','USED')) AS business_taken,
           SUM(cabin_class='ECONOMY' AND ticket_status IN ('ISSUED','USED')) AS economy_taken
    FROM tickets GROUP BY flight_id
) t ON t.flight_id=f.flight_id
SET f.business_available_seats=GREATEST(0,f.business_capacity-COALESCE(t.business_taken,0)),
    f.economy_available_seats=GREATEST(0,f.economy_capacity-COALESCE(t.economy_taken,0)),
    f.available_seats=GREATEST(0,f.business_capacity-COALESCE(t.business_taken,0))+
                      GREATEST(0,f.economy_capacity-COALESCE(t.economy_taken,0));

ALTER TABLE flights
    ADD CONSTRAINT chk_flights_cabin_pricing CHECK (
        ticket_price=economy_ticket_price AND economy_ticket_price>=0 AND
        business_ticket_price>=economy_ticket_price*2.5 AND business_ticket_price<=economy_ticket_price*3.0
    ),
    ADD CONSTRAINT chk_flights_cabin_inventory CHECK (
        available_seats=economy_available_seats+business_available_seats AND
        economy_available_seats<=economy_capacity AND business_available_seats<=business_capacity
    );

ALTER TABLE tickets
    ADD CONSTRAINT chk_tickets_cabin_class CHECK (cabin_class IN ('ECONOMY','BUSINESS')),
    ADD CONSTRAINT chk_tickets_cabin_price CHECK (ticket_price>=0);

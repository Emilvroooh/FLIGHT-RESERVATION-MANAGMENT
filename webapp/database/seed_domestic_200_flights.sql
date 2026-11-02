-- Adds 208 upcoming domestic flight departures (26 city pairs x 2 directions x 4 times).
-- Uses a derived table and UNION ALL so the seed runs without CTE support.
-- Run after schema.sql and seed_airlines_flights.sql / cabin-class migration.
-- Re-running on the same dates leaves existing flights unchanged.
USE flight_booking_web;

INSERT INTO airlines (airline_name, country, contact_number)
VALUES ('SkyWays', 'India', '+91-11-5555-0101'),
       ('Coastal Air', 'India', '+91-22-5555-0102'),
       ('Horizon Airlines', 'India', '+91-80-5555-0401'),
       ('CloudCrest Air', 'India', '+91-20-5555-0501')
ON DUPLICATE KEY UPDATE country=VALUES(country), contact_number=VALUES(contact_number);

INSERT INTO flights
    (airline_id,flight_number,departure_city,destination_city,departure_time,arrival_time,
     available_seats,ticket_price,economy_capacity,economy_available_seats,economy_ticket_price,
     business_capacity,business_available_seats,business_ticket_price)
SELECT a.airline_id,s.flight_number,s.departure_city,s.destination_city,
       TIMESTAMP(DATE_ADD(CURRENT_DATE,INTERVAL s.day_offset DAY),s.departure_clock),
       DATE_ADD(TIMESTAMP(DATE_ADD(CURRENT_DATE,INTERVAL s.day_offset DAY),s.departure_clock),INTERVAL s.duration_hours HOUR),
       120,s.fare,84,84,s.fare,36,36,ROUND(s.fare*2.75,2)
FROM airlines a
JOIN (
    SELECT 'SkyWays' airline_name,'IN-0001' flight_number,'Mumbai' departure_city,'Delhi' destination_city,1 day_offset,'06:00:00' departure_clock,2 duration_hours,3599 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0002' flight_number,'Mumbai' departure_city,'Delhi' destination_city,1 day_offset,'10:00:00' departure_clock,2 duration_hours,3749 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0003' flight_number,'Mumbai' departure_city,'Delhi' destination_city,1 day_offset,'14:00:00' departure_clock,2 duration_hours,3899 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0004' flight_number,'Mumbai' departure_city,'Delhi' destination_city,1 day_offset,'18:00:00' departure_clock,2 duration_hours,4049 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0005' flight_number,'Delhi' departure_city,'Mumbai' destination_city,1 day_offset,'07:00:00' departure_clock,2 duration_hours,3674 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0006' flight_number,'Delhi' departure_city,'Mumbai' destination_city,1 day_offset,'11:00:00' departure_clock,2 duration_hours,3824 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0007' flight_number,'Delhi' departure_city,'Mumbai' destination_city,1 day_offset,'15:00:00' departure_clock,2 duration_hours,3974 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0008' flight_number,'Delhi' departure_city,'Mumbai' destination_city,1 day_offset,'19:00:00' departure_clock,2 duration_hours,4124 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0009' flight_number,'Mumbai' departure_city,'Bengaluru' destination_city,2 day_offset,'06:00:00' departure_clock,2 duration_hours,3899 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0010' flight_number,'Mumbai' departure_city,'Bengaluru' destination_city,2 day_offset,'10:00:00' departure_clock,2 duration_hours,4049 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0011' flight_number,'Mumbai' departure_city,'Bengaluru' destination_city,2 day_offset,'14:00:00' departure_clock,2 duration_hours,4199 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0012' flight_number,'Mumbai' departure_city,'Bengaluru' destination_city,2 day_offset,'18:00:00' departure_clock,2 duration_hours,4349 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0013' flight_number,'Bengaluru' departure_city,'Mumbai' destination_city,2 day_offset,'07:00:00' departure_clock,2 duration_hours,3974 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0014' flight_number,'Bengaluru' departure_city,'Mumbai' destination_city,2 day_offset,'11:00:00' departure_clock,2 duration_hours,4124 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0015' flight_number,'Bengaluru' departure_city,'Mumbai' destination_city,2 day_offset,'15:00:00' departure_clock,2 duration_hours,4274 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0016' flight_number,'Bengaluru' departure_city,'Mumbai' destination_city,2 day_offset,'19:00:00' departure_clock,2 duration_hours,4424 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0017' flight_number,'Mumbai' departure_city,'Chennai' destination_city,3 day_offset,'06:00:00' departure_clock,2 duration_hours,3799 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0018' flight_number,'Mumbai' departure_city,'Chennai' destination_city,3 day_offset,'10:00:00' departure_clock,2 duration_hours,3949 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0019' flight_number,'Mumbai' departure_city,'Chennai' destination_city,3 day_offset,'14:00:00' departure_clock,2 duration_hours,4099 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0020' flight_number,'Mumbai' departure_city,'Chennai' destination_city,3 day_offset,'18:00:00' departure_clock,2 duration_hours,4249 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0021' flight_number,'Chennai' departure_city,'Mumbai' destination_city,3 day_offset,'07:00:00' departure_clock,2 duration_hours,3874 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0022' flight_number,'Chennai' departure_city,'Mumbai' destination_city,3 day_offset,'11:00:00' departure_clock,2 duration_hours,4024 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0023' flight_number,'Chennai' departure_city,'Mumbai' destination_city,3 day_offset,'15:00:00' departure_clock,2 duration_hours,4174 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0024' flight_number,'Chennai' departure_city,'Mumbai' destination_city,3 day_offset,'19:00:00' departure_clock,2 duration_hours,4324 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0025' flight_number,'Mumbai' departure_city,'Hyderabad' destination_city,4 day_offset,'06:00:00' departure_clock,2 duration_hours,3299 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0026' flight_number,'Mumbai' departure_city,'Hyderabad' destination_city,4 day_offset,'10:00:00' departure_clock,2 duration_hours,3449 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0027' flight_number,'Mumbai' departure_city,'Hyderabad' destination_city,4 day_offset,'14:00:00' departure_clock,2 duration_hours,3599 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0028' flight_number,'Mumbai' departure_city,'Hyderabad' destination_city,4 day_offset,'18:00:00' departure_clock,2 duration_hours,3749 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0029' flight_number,'Hyderabad' departure_city,'Mumbai' destination_city,4 day_offset,'07:00:00' departure_clock,2 duration_hours,3374 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0030' flight_number,'Hyderabad' departure_city,'Mumbai' destination_city,4 day_offset,'11:00:00' departure_clock,2 duration_hours,3524 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0031' flight_number,'Hyderabad' departure_city,'Mumbai' destination_city,4 day_offset,'15:00:00' departure_clock,2 duration_hours,3674 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0032' flight_number,'Hyderabad' departure_city,'Mumbai' destination_city,4 day_offset,'19:00:00' departure_clock,2 duration_hours,3824 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0033' flight_number,'Mumbai' departure_city,'Kolkata' destination_city,5 day_offset,'06:00:00' departure_clock,3 duration_hours,4499 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0034' flight_number,'Mumbai' departure_city,'Kolkata' destination_city,5 day_offset,'10:00:00' departure_clock,3 duration_hours,4649 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0035' flight_number,'Mumbai' departure_city,'Kolkata' destination_city,5 day_offset,'14:00:00' departure_clock,3 duration_hours,4799 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0036' flight_number,'Mumbai' departure_city,'Kolkata' destination_city,5 day_offset,'18:00:00' departure_clock,3 duration_hours,4949 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0037' flight_number,'Kolkata' departure_city,'Mumbai' destination_city,5 day_offset,'07:00:00' departure_clock,3 duration_hours,4574 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0038' flight_number,'Kolkata' departure_city,'Mumbai' destination_city,5 day_offset,'11:00:00' departure_clock,3 duration_hours,4724 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0039' flight_number,'Kolkata' departure_city,'Mumbai' destination_city,5 day_offset,'15:00:00' departure_clock,3 duration_hours,4874 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0040' flight_number,'Kolkata' departure_city,'Mumbai' destination_city,5 day_offset,'19:00:00' departure_clock,3 duration_hours,5024 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0041' flight_number,'Mumbai' departure_city,'Goa' destination_city,6 day_offset,'06:00:00' departure_clock,1 duration_hours,2999 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0042' flight_number,'Mumbai' departure_city,'Goa' destination_city,6 day_offset,'10:00:00' departure_clock,1 duration_hours,3149 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0043' flight_number,'Mumbai' departure_city,'Goa' destination_city,6 day_offset,'14:00:00' departure_clock,1 duration_hours,3299 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0044' flight_number,'Mumbai' departure_city,'Goa' destination_city,6 day_offset,'18:00:00' departure_clock,1 duration_hours,3449 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0045' flight_number,'Goa' departure_city,'Mumbai' destination_city,6 day_offset,'07:00:00' departure_clock,1 duration_hours,3074 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0046' flight_number,'Goa' departure_city,'Mumbai' destination_city,6 day_offset,'11:00:00' departure_clock,1 duration_hours,3224 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0047' flight_number,'Goa' departure_city,'Mumbai' destination_city,6 day_offset,'15:00:00' departure_clock,1 duration_hours,3374 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0048' flight_number,'Goa' departure_city,'Mumbai' destination_city,6 day_offset,'19:00:00' departure_clock,1 duration_hours,3524 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0049' flight_number,'Delhi' departure_city,'Bengaluru' destination_city,7 day_offset,'06:00:00' departure_clock,3 duration_hours,4299 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0050' flight_number,'Delhi' departure_city,'Bengaluru' destination_city,7 day_offset,'10:00:00' departure_clock,3 duration_hours,4449 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0051' flight_number,'Delhi' departure_city,'Bengaluru' destination_city,7 day_offset,'14:00:00' departure_clock,3 duration_hours,4599 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0052' flight_number,'Delhi' departure_city,'Bengaluru' destination_city,7 day_offset,'18:00:00' departure_clock,3 duration_hours,4749 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0053' flight_number,'Bengaluru' departure_city,'Delhi' destination_city,7 day_offset,'07:00:00' departure_clock,3 duration_hours,4374 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0054' flight_number,'Bengaluru' departure_city,'Delhi' destination_city,7 day_offset,'11:00:00' departure_clock,3 duration_hours,4524 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0055' flight_number,'Bengaluru' departure_city,'Delhi' destination_city,7 day_offset,'15:00:00' departure_clock,3 duration_hours,4674 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0056' flight_number,'Bengaluru' departure_city,'Delhi' destination_city,7 day_offset,'19:00:00' departure_clock,3 duration_hours,4824 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0057' flight_number,'Delhi' departure_city,'Chennai' destination_city,8 day_offset,'06:00:00' departure_clock,3 duration_hours,4599 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0058' flight_number,'Delhi' departure_city,'Chennai' destination_city,8 day_offset,'10:00:00' departure_clock,3 duration_hours,4749 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0059' flight_number,'Delhi' departure_city,'Chennai' destination_city,8 day_offset,'14:00:00' departure_clock,3 duration_hours,4899 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0060' flight_number,'Delhi' departure_city,'Chennai' destination_city,8 day_offset,'18:00:00' departure_clock,3 duration_hours,5049 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0061' flight_number,'Chennai' departure_city,'Delhi' destination_city,8 day_offset,'07:00:00' departure_clock,3 duration_hours,4674 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0062' flight_number,'Chennai' departure_city,'Delhi' destination_city,8 day_offset,'11:00:00' departure_clock,3 duration_hours,4824 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0063' flight_number,'Chennai' departure_city,'Delhi' destination_city,8 day_offset,'15:00:00' departure_clock,3 duration_hours,4974 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0064' flight_number,'Chennai' departure_city,'Delhi' destination_city,8 day_offset,'19:00:00' departure_clock,3 duration_hours,5124 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0065' flight_number,'Delhi' departure_city,'Hyderabad' destination_city,9 day_offset,'06:00:00' departure_clock,2 duration_hours,3999 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0066' flight_number,'Delhi' departure_city,'Hyderabad' destination_city,9 day_offset,'10:00:00' departure_clock,2 duration_hours,4149 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0067' flight_number,'Delhi' departure_city,'Hyderabad' destination_city,9 day_offset,'14:00:00' departure_clock,2 duration_hours,4299 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0068' flight_number,'Delhi' departure_city,'Hyderabad' destination_city,9 day_offset,'18:00:00' departure_clock,2 duration_hours,4449 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0069' flight_number,'Hyderabad' departure_city,'Delhi' destination_city,9 day_offset,'07:00:00' departure_clock,2 duration_hours,4074 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0070' flight_number,'Hyderabad' departure_city,'Delhi' destination_city,9 day_offset,'11:00:00' departure_clock,2 duration_hours,4224 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0071' flight_number,'Hyderabad' departure_city,'Delhi' destination_city,9 day_offset,'15:00:00' departure_clock,2 duration_hours,4374 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0072' flight_number,'Hyderabad' departure_city,'Delhi' destination_city,9 day_offset,'19:00:00' departure_clock,2 duration_hours,4524 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0073' flight_number,'Delhi' departure_city,'Kolkata' destination_city,10 day_offset,'06:00:00' departure_clock,2 duration_hours,3499 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0074' flight_number,'Delhi' departure_city,'Kolkata' destination_city,10 day_offset,'10:00:00' departure_clock,2 duration_hours,3649 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0075' flight_number,'Delhi' departure_city,'Kolkata' destination_city,10 day_offset,'14:00:00' departure_clock,2 duration_hours,3799 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0076' flight_number,'Delhi' departure_city,'Kolkata' destination_city,10 day_offset,'18:00:00' departure_clock,2 duration_hours,3949 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0077' flight_number,'Kolkata' departure_city,'Delhi' destination_city,10 day_offset,'07:00:00' departure_clock,2 duration_hours,3574 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0078' flight_number,'Kolkata' departure_city,'Delhi' destination_city,10 day_offset,'11:00:00' departure_clock,2 duration_hours,3724 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0079' flight_number,'Kolkata' departure_city,'Delhi' destination_city,10 day_offset,'15:00:00' departure_clock,2 duration_hours,3874 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0080' flight_number,'Kolkata' departure_city,'Delhi' destination_city,10 day_offset,'19:00:00' departure_clock,2 duration_hours,4024 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0081' flight_number,'Delhi' departure_city,'Jaipur' destination_city,11 day_offset,'06:00:00' departure_clock,1 duration_hours,2499 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0082' flight_number,'Delhi' departure_city,'Jaipur' destination_city,11 day_offset,'10:00:00' departure_clock,1 duration_hours,2649 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0083' flight_number,'Delhi' departure_city,'Jaipur' destination_city,11 day_offset,'14:00:00' departure_clock,1 duration_hours,2799 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0084' flight_number,'Delhi' departure_city,'Jaipur' destination_city,11 day_offset,'18:00:00' departure_clock,1 duration_hours,2949 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0085' flight_number,'Jaipur' departure_city,'Delhi' destination_city,11 day_offset,'07:00:00' departure_clock,1 duration_hours,2574 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0086' flight_number,'Jaipur' departure_city,'Delhi' destination_city,11 day_offset,'11:00:00' departure_clock,1 duration_hours,2724 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0087' flight_number,'Jaipur' departure_city,'Delhi' destination_city,11 day_offset,'15:00:00' departure_clock,1 duration_hours,2874 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0088' flight_number,'Jaipur' departure_city,'Delhi' destination_city,11 day_offset,'19:00:00' departure_clock,1 duration_hours,3024 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0089' flight_number,'Delhi' departure_city,'Lucknow' destination_city,12 day_offset,'06:00:00' departure_clock,1 duration_hours,2699 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0090' flight_number,'Delhi' departure_city,'Lucknow' destination_city,12 day_offset,'10:00:00' departure_clock,1 duration_hours,2849 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0091' flight_number,'Delhi' departure_city,'Lucknow' destination_city,12 day_offset,'14:00:00' departure_clock,1 duration_hours,2999 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0092' flight_number,'Delhi' departure_city,'Lucknow' destination_city,12 day_offset,'18:00:00' departure_clock,1 duration_hours,3149 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0093' flight_number,'Lucknow' departure_city,'Delhi' destination_city,12 day_offset,'07:00:00' departure_clock,1 duration_hours,2774 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0094' flight_number,'Lucknow' departure_city,'Delhi' destination_city,12 day_offset,'11:00:00' departure_clock,1 duration_hours,2924 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0095' flight_number,'Lucknow' departure_city,'Delhi' destination_city,12 day_offset,'15:00:00' departure_clock,1 duration_hours,3074 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0096' flight_number,'Lucknow' departure_city,'Delhi' destination_city,12 day_offset,'19:00:00' departure_clock,1 duration_hours,3224 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0097' flight_number,'Bengaluru' departure_city,'Chennai' destination_city,13 day_offset,'06:00:00' departure_clock,1 duration_hours,2399 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0098' flight_number,'Bengaluru' departure_city,'Chennai' destination_city,13 day_offset,'10:00:00' departure_clock,1 duration_hours,2549 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0099' flight_number,'Bengaluru' departure_city,'Chennai' destination_city,13 day_offset,'14:00:00' departure_clock,1 duration_hours,2699 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0100' flight_number,'Bengaluru' departure_city,'Chennai' destination_city,13 day_offset,'18:00:00' departure_clock,1 duration_hours,2849 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0101' flight_number,'Chennai' departure_city,'Bengaluru' destination_city,13 day_offset,'07:00:00' departure_clock,1 duration_hours,2474 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0102' flight_number,'Chennai' departure_city,'Bengaluru' destination_city,13 day_offset,'11:00:00' departure_clock,1 duration_hours,2624 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0103' flight_number,'Chennai' departure_city,'Bengaluru' destination_city,13 day_offset,'15:00:00' departure_clock,1 duration_hours,2774 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0104' flight_number,'Chennai' departure_city,'Bengaluru' destination_city,13 day_offset,'19:00:00' departure_clock,1 duration_hours,2924 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0105' flight_number,'Bengaluru' departure_city,'Hyderabad' destination_city,14 day_offset,'06:00:00' departure_clock,1 duration_hours,2799 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0106' flight_number,'Bengaluru' departure_city,'Hyderabad' destination_city,14 day_offset,'10:00:00' departure_clock,1 duration_hours,2949 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0107' flight_number,'Bengaluru' departure_city,'Hyderabad' destination_city,14 day_offset,'14:00:00' departure_clock,1 duration_hours,3099 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0108' flight_number,'Bengaluru' departure_city,'Hyderabad' destination_city,14 day_offset,'18:00:00' departure_clock,1 duration_hours,3249 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0109' flight_number,'Hyderabad' departure_city,'Bengaluru' destination_city,14 day_offset,'07:00:00' departure_clock,1 duration_hours,2874 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0110' flight_number,'Hyderabad' departure_city,'Bengaluru' destination_city,14 day_offset,'11:00:00' departure_clock,1 duration_hours,3024 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0111' flight_number,'Hyderabad' departure_city,'Bengaluru' destination_city,14 day_offset,'15:00:00' departure_clock,1 duration_hours,3174 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0112' flight_number,'Hyderabad' departure_city,'Bengaluru' destination_city,14 day_offset,'19:00:00' departure_clock,1 duration_hours,3324 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0113' flight_number,'Bengaluru' departure_city,'Kochi' destination_city,15 day_offset,'06:00:00' departure_clock,1 duration_hours,2999 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0114' flight_number,'Bengaluru' departure_city,'Kochi' destination_city,15 day_offset,'10:00:00' departure_clock,1 duration_hours,3149 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0115' flight_number,'Bengaluru' departure_city,'Kochi' destination_city,15 day_offset,'14:00:00' departure_clock,1 duration_hours,3299 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0116' flight_number,'Bengaluru' departure_city,'Kochi' destination_city,15 day_offset,'18:00:00' departure_clock,1 duration_hours,3449 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0117' flight_number,'Kochi' departure_city,'Bengaluru' destination_city,15 day_offset,'07:00:00' departure_clock,1 duration_hours,3074 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0118' flight_number,'Kochi' departure_city,'Bengaluru' destination_city,15 day_offset,'11:00:00' departure_clock,1 duration_hours,3224 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0119' flight_number,'Kochi' departure_city,'Bengaluru' destination_city,15 day_offset,'15:00:00' departure_clock,1 duration_hours,3374 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0120' flight_number,'Kochi' departure_city,'Bengaluru' destination_city,15 day_offset,'19:00:00' departure_clock,1 duration_hours,3524 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0121' flight_number,'Bengaluru' departure_city,'Goa' destination_city,16 day_offset,'06:00:00' departure_clock,1 duration_hours,3199 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0122' flight_number,'Bengaluru' departure_city,'Goa' destination_city,16 day_offset,'10:00:00' departure_clock,1 duration_hours,3349 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0123' flight_number,'Bengaluru' departure_city,'Goa' destination_city,16 day_offset,'14:00:00' departure_clock,1 duration_hours,3499 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0124' flight_number,'Bengaluru' departure_city,'Goa' destination_city,16 day_offset,'18:00:00' departure_clock,1 duration_hours,3649 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0125' flight_number,'Goa' departure_city,'Bengaluru' destination_city,16 day_offset,'07:00:00' departure_clock,1 duration_hours,3274 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0126' flight_number,'Goa' departure_city,'Bengaluru' destination_city,16 day_offset,'11:00:00' departure_clock,1 duration_hours,3424 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0127' flight_number,'Goa' departure_city,'Bengaluru' destination_city,16 day_offset,'15:00:00' departure_clock,1 duration_hours,3574 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0128' flight_number,'Goa' departure_city,'Bengaluru' destination_city,16 day_offset,'19:00:00' departure_clock,1 duration_hours,3724 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0129' flight_number,'Chennai' departure_city,'Hyderabad' destination_city,17 day_offset,'06:00:00' departure_clock,1 duration_hours,2799 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0130' flight_number,'Chennai' departure_city,'Hyderabad' destination_city,17 day_offset,'10:00:00' departure_clock,1 duration_hours,2949 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0131' flight_number,'Chennai' departure_city,'Hyderabad' destination_city,17 day_offset,'14:00:00' departure_clock,1 duration_hours,3099 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0132' flight_number,'Chennai' departure_city,'Hyderabad' destination_city,17 day_offset,'18:00:00' departure_clock,1 duration_hours,3249 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0133' flight_number,'Hyderabad' departure_city,'Chennai' destination_city,17 day_offset,'07:00:00' departure_clock,1 duration_hours,2874 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0134' flight_number,'Hyderabad' departure_city,'Chennai' destination_city,17 day_offset,'11:00:00' departure_clock,1 duration_hours,3024 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0135' flight_number,'Hyderabad' departure_city,'Chennai' destination_city,17 day_offset,'15:00:00' departure_clock,1 duration_hours,3174 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0136' flight_number,'Hyderabad' departure_city,'Chennai' destination_city,17 day_offset,'19:00:00' departure_clock,1 duration_hours,3324 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0137' flight_number,'Chennai' departure_city,'Kolkata' destination_city,18 day_offset,'06:00:00' departure_clock,3 duration_hours,4399 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0138' flight_number,'Chennai' departure_city,'Kolkata' destination_city,18 day_offset,'10:00:00' departure_clock,3 duration_hours,4549 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0139' flight_number,'Chennai' departure_city,'Kolkata' destination_city,18 day_offset,'14:00:00' departure_clock,3 duration_hours,4699 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0140' flight_number,'Chennai' departure_city,'Kolkata' destination_city,18 day_offset,'18:00:00' departure_clock,3 duration_hours,4849 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0141' flight_number,'Kolkata' departure_city,'Chennai' destination_city,18 day_offset,'07:00:00' departure_clock,3 duration_hours,4474 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0142' flight_number,'Kolkata' departure_city,'Chennai' destination_city,18 day_offset,'11:00:00' departure_clock,3 duration_hours,4624 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0143' flight_number,'Kolkata' departure_city,'Chennai' destination_city,18 day_offset,'15:00:00' departure_clock,3 duration_hours,4774 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0144' flight_number,'Kolkata' departure_city,'Chennai' destination_city,18 day_offset,'19:00:00' departure_clock,3 duration_hours,4924 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0145' flight_number,'Chennai' departure_city,'Trivandrum' destination_city,19 day_offset,'06:00:00' departure_clock,1 duration_hours,2899 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0146' flight_number,'Chennai' departure_city,'Trivandrum' destination_city,19 day_offset,'10:00:00' departure_clock,1 duration_hours,3049 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0147' flight_number,'Chennai' departure_city,'Trivandrum' destination_city,19 day_offset,'14:00:00' departure_clock,1 duration_hours,3199 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0148' flight_number,'Chennai' departure_city,'Trivandrum' destination_city,19 day_offset,'18:00:00' departure_clock,1 duration_hours,3349 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0149' flight_number,'Trivandrum' departure_city,'Chennai' destination_city,19 day_offset,'07:00:00' departure_clock,1 duration_hours,2974 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0150' flight_number,'Trivandrum' departure_city,'Chennai' destination_city,19 day_offset,'11:00:00' departure_clock,1 duration_hours,3124 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0151' flight_number,'Trivandrum' departure_city,'Chennai' destination_city,19 day_offset,'15:00:00' departure_clock,1 duration_hours,3274 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0152' flight_number,'Trivandrum' departure_city,'Chennai' destination_city,19 day_offset,'19:00:00' departure_clock,1 duration_hours,3424 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0153' flight_number,'Hyderabad' departure_city,'Pune' destination_city,20 day_offset,'06:00:00' departure_clock,1 duration_hours,2699 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0154' flight_number,'Hyderabad' departure_city,'Pune' destination_city,20 day_offset,'10:00:00' departure_clock,1 duration_hours,2849 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0155' flight_number,'Hyderabad' departure_city,'Pune' destination_city,20 day_offset,'14:00:00' departure_clock,1 duration_hours,2999 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0156' flight_number,'Hyderabad' departure_city,'Pune' destination_city,20 day_offset,'18:00:00' departure_clock,1 duration_hours,3149 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0157' flight_number,'Pune' departure_city,'Hyderabad' destination_city,20 day_offset,'07:00:00' departure_clock,1 duration_hours,2774 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0158' flight_number,'Pune' departure_city,'Hyderabad' destination_city,20 day_offset,'11:00:00' departure_clock,1 duration_hours,2924 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0159' flight_number,'Pune' departure_city,'Hyderabad' destination_city,20 day_offset,'15:00:00' departure_clock,1 duration_hours,3074 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0160' flight_number,'Pune' departure_city,'Hyderabad' destination_city,20 day_offset,'19:00:00' departure_clock,1 duration_hours,3224 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0161' flight_number,'Hyderabad' departure_city,'Kochi' destination_city,21 day_offset,'06:00:00' departure_clock,2 duration_hours,3199 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0162' flight_number,'Hyderabad' departure_city,'Kochi' destination_city,21 day_offset,'10:00:00' departure_clock,2 duration_hours,3349 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0163' flight_number,'Hyderabad' departure_city,'Kochi' destination_city,21 day_offset,'14:00:00' departure_clock,2 duration_hours,3499 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0164' flight_number,'Hyderabad' departure_city,'Kochi' destination_city,21 day_offset,'18:00:00' departure_clock,2 duration_hours,3649 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0165' flight_number,'Kochi' departure_city,'Hyderabad' destination_city,21 day_offset,'07:00:00' departure_clock,2 duration_hours,3274 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0166' flight_number,'Kochi' departure_city,'Hyderabad' destination_city,21 day_offset,'11:00:00' departure_clock,2 duration_hours,3424 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0167' flight_number,'Kochi' departure_city,'Hyderabad' destination_city,21 day_offset,'15:00:00' departure_clock,2 duration_hours,3574 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0168' flight_number,'Kochi' departure_city,'Hyderabad' destination_city,21 day_offset,'19:00:00' departure_clock,2 duration_hours,3724 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0169' flight_number,'Kolkata' departure_city,'Guwahati' destination_city,22 day_offset,'06:00:00' departure_clock,1 duration_hours,2799 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0170' flight_number,'Kolkata' departure_city,'Guwahati' destination_city,22 day_offset,'10:00:00' departure_clock,1 duration_hours,2949 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0171' flight_number,'Kolkata' departure_city,'Guwahati' destination_city,22 day_offset,'14:00:00' departure_clock,1 duration_hours,3099 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0172' flight_number,'Kolkata' departure_city,'Guwahati' destination_city,22 day_offset,'18:00:00' departure_clock,1 duration_hours,3249 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0173' flight_number,'Guwahati' departure_city,'Kolkata' destination_city,22 day_offset,'07:00:00' departure_clock,1 duration_hours,2874 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0174' flight_number,'Guwahati' departure_city,'Kolkata' destination_city,22 day_offset,'11:00:00' departure_clock,1 duration_hours,3024 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0175' flight_number,'Guwahati' departure_city,'Kolkata' destination_city,22 day_offset,'15:00:00' departure_clock,1 duration_hours,3174 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0176' flight_number,'Guwahati' departure_city,'Kolkata' destination_city,22 day_offset,'19:00:00' departure_clock,1 duration_hours,3324 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0177' flight_number,'Kolkata' departure_city,'Bhubaneswar' destination_city,23 day_offset,'06:00:00' departure_clock,1 duration_hours,2599 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0178' flight_number,'Kolkata' departure_city,'Bhubaneswar' destination_city,23 day_offset,'10:00:00' departure_clock,1 duration_hours,2749 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0179' flight_number,'Kolkata' departure_city,'Bhubaneswar' destination_city,23 day_offset,'14:00:00' departure_clock,1 duration_hours,2899 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0180' flight_number,'Kolkata' departure_city,'Bhubaneswar' destination_city,23 day_offset,'18:00:00' departure_clock,1 duration_hours,3049 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0181' flight_number,'Bhubaneswar' departure_city,'Kolkata' destination_city,23 day_offset,'07:00:00' departure_clock,1 duration_hours,2674 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0182' flight_number,'Bhubaneswar' departure_city,'Kolkata' destination_city,23 day_offset,'11:00:00' departure_clock,1 duration_hours,2824 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0183' flight_number,'Bhubaneswar' departure_city,'Kolkata' destination_city,23 day_offset,'15:00:00' departure_clock,1 duration_hours,2974 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0184' flight_number,'Bhubaneswar' departure_city,'Kolkata' destination_city,23 day_offset,'19:00:00' departure_clock,1 duration_hours,3124 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0185' flight_number,'Pune' departure_city,'Ahmedabad' destination_city,24 day_offset,'06:00:00' departure_clock,1 duration_hours,2499 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0186' flight_number,'Pune' departure_city,'Ahmedabad' destination_city,24 day_offset,'10:00:00' departure_clock,1 duration_hours,2649 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0187' flight_number,'Pune' departure_city,'Ahmedabad' destination_city,24 day_offset,'14:00:00' departure_clock,1 duration_hours,2799 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0188' flight_number,'Pune' departure_city,'Ahmedabad' destination_city,24 day_offset,'18:00:00' departure_clock,1 duration_hours,2949 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0189' flight_number,'Ahmedabad' departure_city,'Pune' destination_city,24 day_offset,'07:00:00' departure_clock,1 duration_hours,2574 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0190' flight_number,'Ahmedabad' departure_city,'Pune' destination_city,24 day_offset,'11:00:00' departure_clock,1 duration_hours,2724 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0191' flight_number,'Ahmedabad' departure_city,'Pune' destination_city,24 day_offset,'15:00:00' departure_clock,1 duration_hours,2874 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0192' flight_number,'Ahmedabad' departure_city,'Pune' destination_city,24 day_offset,'19:00:00' departure_clock,1 duration_hours,3024 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0193' flight_number,'Jaipur' departure_city,'Ahmedabad' destination_city,25 day_offset,'06:00:00' departure_clock,1 duration_hours,2699 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0194' flight_number,'Jaipur' departure_city,'Ahmedabad' destination_city,25 day_offset,'10:00:00' departure_clock,1 duration_hours,2849 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0195' flight_number,'Jaipur' departure_city,'Ahmedabad' destination_city,25 day_offset,'14:00:00' departure_clock,1 duration_hours,2999 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0196' flight_number,'Jaipur' departure_city,'Ahmedabad' destination_city,25 day_offset,'18:00:00' departure_clock,1 duration_hours,3149 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0197' flight_number,'Ahmedabad' departure_city,'Jaipur' destination_city,25 day_offset,'07:00:00' departure_clock,1 duration_hours,2774 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0198' flight_number,'Ahmedabad' departure_city,'Jaipur' destination_city,25 day_offset,'11:00:00' departure_clock,1 duration_hours,2924 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0199' flight_number,'Ahmedabad' departure_city,'Jaipur' destination_city,25 day_offset,'15:00:00' departure_clock,1 duration_hours,3074 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0200' flight_number,'Ahmedabad' departure_city,'Jaipur' destination_city,25 day_offset,'19:00:00' departure_clock,1 duration_hours,3224 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0201' flight_number,'Patna' departure_city,'Lucknow' destination_city,26 day_offset,'06:00:00' departure_clock,1 duration_hours,2999 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0202' flight_number,'Patna' departure_city,'Lucknow' destination_city,26 day_offset,'10:00:00' departure_clock,1 duration_hours,3149 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0203' flight_number,'Patna' departure_city,'Lucknow' destination_city,26 day_offset,'14:00:00' departure_clock,1 duration_hours,3299 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0204' flight_number,'Patna' departure_city,'Lucknow' destination_city,26 day_offset,'18:00:00' departure_clock,1 duration_hours,3449 fare UNION ALL
    SELECT 'SkyWays' airline_name,'IN-0205' flight_number,'Lucknow' departure_city,'Patna' destination_city,26 day_offset,'07:00:00' departure_clock,1 duration_hours,3074 fare UNION ALL
    SELECT 'Coastal Air' airline_name,'IN-0206' flight_number,'Lucknow' departure_city,'Patna' destination_city,26 day_offset,'11:00:00' departure_clock,1 duration_hours,3224 fare UNION ALL
    SELECT 'Horizon Airlines' airline_name,'IN-0207' flight_number,'Lucknow' departure_city,'Patna' destination_city,26 day_offset,'15:00:00' departure_clock,1 duration_hours,3374 fare UNION ALL
    SELECT 'CloudCrest Air' airline_name,'IN-0208' flight_number,'Lucknow' departure_city,'Patna' destination_city,26 day_offset,'19:00:00' departure_clock,1 duration_hours,3524 fare
) s ON s.airline_name=a.airline_name
ON DUPLICATE KEY UPDATE flight_id=flight_id;

-- Verify inserted flights: this should report at least 208 for a fresh seed.
SELECT COUNT(*) AS domestic_seeded_flights FROM flights WHERE flight_number LIKE 'IN-%';


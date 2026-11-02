-- Run after schema.sql in MySQL Workbench to add/update the 40 sample flights.
-- Existing reservations and remaining-seat counts are preserved when rerun on the same day.
USE flight_booking_web;

INSERT INTO airlines (airline_name, country, contact_number)
VALUES ('SkyWays', 'India', '+91-11-5555-0101'),
       ('Coastal Air', 'India', '+91-22-5555-0102'),
       ('AeroGlobal', 'United Arab Emirates', '+971-4-555-0301'),
       ('Horizon Airlines', 'India', '+91-80-5555-0401'),
       ('CloudCrest Air', 'India', '+91-20-5555-0501'),
       ('Apex Airways', 'United Kingdom', '+44-20-5555-0601')
ON DUPLICATE KEY UPDATE country=VALUES(country), contact_number=VALUES(contact_number);

INSERT INTO flights
    (airline_id, flight_number, departure_city, destination_city,
     departure_time, arrival_time, available_seats, ticket_price,
     economy_capacity, economy_available_seats, economy_ticket_price,
     business_capacity, business_available_seats, business_ticket_price)
SELECT a.airline_id, s.flight_number, s.departure_city, s.destination_city,
       TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL s.day_offset DAY), s.departure_clock),
       DATE_ADD(TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL s.day_offset DAY), s.departure_clock), INTERVAL s.duration_hours HOUR),
       s.seats, s.fare,
       s.seats-LEAST(s.seats,GREATEST(1,CEIL(s.seats/18)*6)),
       s.seats-LEAST(s.seats,GREATEST(1,CEIL(s.seats/18)*6)),s.fare,
       LEAST(s.seats,GREATEST(1,CEIL(s.seats/18)*6)),
       LEAST(s.seats,GREATEST(1,CEIL(s.seats/18)*6)),ROUND(s.fare*2.75,2)
FROM airlines a
JOIN (
    SELECT 'Coastal Air' airline_name,'CA-101' flight_number,'Mumbai' departure_city,'Delhi' destination_city,1 day_offset,'08:00:00' departure_clock,2 duration_hours,120 seats,5999.00 fare
    UNION ALL SELECT 'Coastal Air','CA-102','Delhi','Mumbai',1,'13:00:00',2,120,5799.00
    UNION ALL SELECT 'SkyWays','SW-201','Bangalore','Chennai',2,'09:00:00',1,100,3299.00
    UNION ALL SELECT 'SkyWays','SW-202','Chennai','Bangalore',2,'15:00:00',1,100,3199.00
    UNION ALL SELECT 'AeroGlobal','AG-301','Dubai','Singapore',3,'08:30:00',7,180,18999.00
    UNION ALL SELECT 'AeroGlobal','AG-302','Singapore','Dubai',3,'19:00:00',7,180,17999.00
    UNION ALL SELECT 'Horizon Airlines','HA-401','Kochi','Hyderabad',4,'07:15:00',2,110,6499.00
    UNION ALL SELECT 'Horizon Airlines','HA-402','Hyderabad','Kochi',4,'16:30:00',2,110,6299.00
    UNION ALL SELECT 'CloudCrest Air','CC-501','Pune','Kolkata',5,'06:45:00',3,105,7999.00
    UNION ALL SELECT 'CloudCrest Air','CC-502','Kolkata','Pune',5,'14:30:00',3,105,7799.00
    UNION ALL SELECT 'Apex Airways','AP-601','London','Frankfurt',6,'09:10:00',2,160,22999.00
    UNION ALL SELECT 'Apex Airways','AP-602','Frankfurt','London',6,'17:40:00',2,160,21999.00
    UNION ALL SELECT 'Coastal Air','CA-103','Goa','Jaipur',7,'08:20:00',2,95,6899.00
    UNION ALL SELECT 'Coastal Air','CA-104','Jaipur','Goa',7,'15:10:00',2,95,6699.00
    UNION ALL SELECT 'SkyWays','SW-203','Ahmedabad','Lucknow',8,'07:30:00',2,100,6199.00
    UNION ALL SELECT 'SkyWays','SW-204','Lucknow','Ahmedabad',8,'16:00:00',2,100,5999.00
    UNION ALL SELECT 'AeroGlobal','AG-303','Abu Dhabi','Kuala Lumpur',9,'10:00:00',7,180,17499.00
    UNION ALL SELECT 'AeroGlobal','AG-304','Kuala Lumpur','Abu Dhabi',9,'20:30:00',7,180,16999.00
    UNION ALL SELECT 'Horizon Airlines','HA-403','Trivandrum','Coimbatore',10,'08:45:00',2,105,5299.00
    UNION ALL SELECT 'Horizon Airlines','HA-404','Coimbatore','Trivandrum',10,'15:30:00',2,105,5099.00
    UNION ALL SELECT 'CloudCrest Air','CC-503','Bhopal','Indore',11,'09:20:00',1,90,2899.00
    UNION ALL SELECT 'CloudCrest Air','CC-504','Indore','Bhopal',11,'13:50:00',1,90,2799.00
    UNION ALL SELECT 'Apex Airways','AP-603','New York','Paris',12,'18:15:00',8,200,45999.00
    UNION ALL SELECT 'Apex Airways','AP-604','Paris','New York',13,'11:30:00',8,200,44999.00
    UNION ALL SELECT 'Coastal Air','CA-105','Patna','Ranchi',14,'07:40:00',1,90,3999.00
    UNION ALL SELECT 'Coastal Air','CA-106','Ranchi','Patna',14,'14:20:00',1,90,3899.00
    UNION ALL SELECT 'SkyWays','SW-205','Guwahati','Bhubaneswar',15,'08:10:00',3,105,7299.00
    UNION ALL SELECT 'SkyWays','SW-206','Bhubaneswar','Guwahati',15,'16:10:00',3,105,7099.00
    UNION ALL SELECT 'AeroGlobal','AG-305','Doha','Bangkok',16,'09:40:00',6,175,15999.00
    UNION ALL SELECT 'AeroGlobal','AG-306','Bangkok','Doha',16,'20:00:00',6,175,15499.00
    UNION ALL SELECT 'Horizon Airlines','HA-405','Mangalore','Mysore',17,'07:00:00',1,85,3599.00
    UNION ALL SELECT 'Horizon Airlines','HA-406','Mysore','Mangalore',17,'14:00:00',1,85,3499.00
    UNION ALL SELECT 'CloudCrest Air','CC-505','Nagpur','Varanasi',18,'08:35:00',2,95,5799.00
    UNION ALL SELECT 'CloudCrest Air','CC-506','Varanasi','Nagpur',18,'15:25:00',2,95,5599.00
    UNION ALL SELECT 'Apex Airways','AP-605','Sydney','Tokyo',19,'10:15:00',9,200,38999.00
    UNION ALL SELECT 'Apex Airways','AP-606','Tokyo','Sydney',19,'21:10:00',9,200,37999.00
    UNION ALL SELECT 'Coastal Air','CA-107','Chandigarh','Amritsar',20,'08:00:00',1,85,3499.00
    UNION ALL SELECT 'Coastal Air','CA-108','Amritsar','Chandigarh',20,'14:30:00',1,85,3399.00
    UNION ALL SELECT 'SkyWays','SW-207','Surat','Vadodara',21,'09:15:00',1,80,2599.00
    UNION ALL SELECT 'SkyWays','SW-208','Vadodara','Surat',21,'15:45:00',1,80,2499.00
) s ON s.airline_name = a.airline_name
ON DUPLICATE KEY UPDATE ticket_price=VALUES(ticket_price),
                        economy_ticket_price=VALUES(economy_ticket_price),
                        business_ticket_price=VALUES(business_ticket_price);


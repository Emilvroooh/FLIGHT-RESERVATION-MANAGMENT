# Skyward Flight Booking Desktop

A Java 17+ desktop application built with Swing and JDBC. It connects to MySQL, supports user registration/login, flight search, multi-passenger bookings, simulated payment and ticket generation, and an admin console for flight and booking management.

## Requirements

- JDK 17 or newer
- Apache Maven 3.6.3 or newer
- MySQL 8.0+

## Database setup

For a fresh database, run `schema.sql` and then `seed_airlines_flights.sql`. For the existing database, do not rerun `schema.sql`: run `migration_add_interactive_seats.sql` if not already applied, then `migration_add_cabin_classes.sql`, followed by `migration_add_time_refunds.sql` and `migration_admin_delete_bookings.sql`. These migrations preserve current records and convert existing flights/tickets to Economy and Business inventory. Run `migration_add_cancellation_requests.sql` only if that feature table is still missing. The 40-flight seed script is only needed if those sample flights are not already present; run it after the cabin migration. To add 208 upcoming domestic departures, run `seed_domestic_200_flights.sql` after the cabin-class schema is ready (also in MySQL Workbench).

Create a restricted application account in a Workbench SQL tab. Replace the sample password with a private password you choose:

```sql
CREATE USER IF NOT EXISTS 'flight_app'@'localhost' IDENTIFIED BY 'choose-a-strong-password';
GRANT SELECT, INSERT, UPDATE, DELETE ON flight_booking.* TO 'flight_app'@'localhost';
```

In the PowerShell window used to run the program, set its database username and URL:

```powershell
$env:FLIGHT_DB_USER = 'flight_app'
$env:FLIGHT_DB_URL = 'jdbc:mysql://localhost:3306/flight_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
```

If `FLIGHT_DB_PASSWORD` is not already set, `run.ps1` securely prompts for it when launched.

### Forgotten passenger password

The **Forgot password?** link opens a passenger password reset form. Enter the passenger email and the matching new password twice. Passwords continue to be stored as PBKDF2 hashes.

The local-development URL enables MySQL public-key retrieval because the connection is not using TLS. For a server configured with TLS, use its certificate-verified SSL URL instead. Set `FLIGHT_DB_URL` only when your server uses a different host, port, database, or TLS configuration.

## Run from PowerShell

The simplest route is `run.ps1`. It compiles the Java sources with the installed JDK, downloads MySQL Connector/J on first run, then opens the Swing window. Run it from the project folder:

```powershell
Set-Location E:\flight
.\run.ps1
```

The first run needs internet access to download the JDBC driver and password compatibility library from Maven Central. The sign-in screen has **Create user account** for customers. If no admin exists yet, it also shows **Set up first administrator**; create that account once, then sign in with its username and password. Accounts created by the earlier web version can also sign in; their BCrypt hash is upgraded to PBKDF2 after a successful login.

Alternatively, after installing Maven, run `mvn exec:java`. You can also open this Maven project in IntelliJ IDEA, Eclipse, or NetBeans and run `com.flightbooking.FlightBookingApp`.

## Features

- Search sample and administrator-managed flights by route and optional departure date using the built-in calendar picker. The optional max Economy fare filter accepts values such as ₹4,000 and includes flights at or below the entered cap.
- Register a user account. Passwords are stored as PBKDF2 hashes.
- Reserve one to eight passengers per booking with individual name, age, gender, and passport fields, then select the seats on the cabin map. Passport number is optional.
- Flights have front Business Class rows and Economy rows. Business fares are kept between 2.5× and 3× the Economy fare; flight search and the admin flight editor show both fare and seat inventories.
- Admins can permanently delete selected bookings. Confirmed bookings must first go through cancellation approval so the time-based refund is recorded; deleting a cancelled booking also deletes its refund history.
- Users can request cancellation from My bookings or a boarding pass. The booking and seats remain active until an administrator approves; approval records the cancellation time and applies the refund tier based on time until departure (100% at 48+ hours, 50% at 12–48 hours, 30% at 2–12 hours, and 20% under 2 hours). Users can view the saved refund summary afterward. Refunds are simulated and recorded locally; no payment provider is contacted.
- View confirmed boarding passes from the passenger dashboard. Each pass is loaded from tickets belonging to the signed-in account and includes passenger, flight, seat, timing, fare, and booking reference details.
- Demo checkout simulates a successful payment without collecting card or bank details. It confirms the booking and issues a unique ticket/seat to each passenger.
- Review bookings and continue payment for pending bookings.
- Passengers can submit cancellation requests for pending or confirmed bookings before departure. Admins review them in the Cancellation requests tab; approval releases the seats, cancels tickets, and marks a simulated payment as refunded.
- Administer flights (add, edit, delete), review bookings and registered users, and approve or decline cancellation requests.
- An administrator can reset a passenger password from the Users tab.

## Notes

- The schema is MySQL 8+ and matches `schema.sql`.
- Payment is simulated; this application does not process real money.
- Keep the app database password in the environment instead of source code. Do not use the MySQL `root` account as the app account.

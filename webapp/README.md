# Skyward Web App

This is a separate full-stack web version of the desktop app. The Swing app at the repository root remains available. The web version uses its own `flight_booking_web` database, so its demo users, seed flights, and bookings remain separate from the desktop app. It uses Spring Boot 3 / Java 17, React, Vite, Tailwind CSS, MySQL, BCrypt password writes, and short-lived signed bearer tokens.

## 1. Prepare MySQL

Open and execute these three scripts in MySQL Workbench, in order. They create an isolated `flight_booking_web` database and do not modify the desktop app’s `flight_booking` database:

1. `webapp/database/schema.sql`
2. `webapp/database/seed_airlines_flights.sql`
3. `webapp/database/seed_domestic_200_flights.sql`

For an existing web database, if `flight_status` does not exist, run `webapp/database/migration_add_flight_status.sql`. If it already exists with the older three-state constraint, run `webapp/database/migration_update_landed_status.sql`. Also run `webapp/database/migration_add_government_verification_id.sql` once before using verification features. Historical passenger rows remain unchanged; new bookings require a valid verification ID.

The domestic seed creates 208 future flight departures across 26 Indian city pairs. The desktop database migrations and seed files are not needed for the isolated web app.

In MySQL Workbench, create an application-specific account for the web schema:

```sql
CREATE USER IF NOT EXISTS 'flight_web_app'@'localhost' IDENTIFIED BY 'choose-a-strong-private-password';
GRANT SELECT, INSERT, UPDATE, DELETE ON flight_booking_web.* TO 'flight_web_app'@'localhost';
```

Do not use the MySQL root account for the running application.

## 2. Start the Spring Boot API (PowerShell)

Install JDK 17+ and Maven 3.9+. From `E:\flight\webapp\backend`, set environment values for the MySQL connection and generate a unique 256-bit signing key for this deployment:

```powershell
$env:FLIGHT_DB_USER = 'flight_app'
$env:FLIGHT_DB_PASSWORD = 'your database password'
$env:FLIGHT_DB_URL = 'jdbc:mysql://localhost:3306/flight_booking_web?serverTimezone=UTC'
$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
$env:ADMIN_PASSKEY = '20080301'
mvn spring-boot:run
```

`JWT_SECRET` has no bundled default. The API will not start without it. Keep it stable and secret across restarts; changing it invalidates currently issued tokens. Set `ADMIN_PASSKEY` explicitly in deployments. The requested default is `20080301`; admin registration and each admin login require that value.

## 3. Configure password-reset email

Password recovery sends a cryptographically random, one-time link with a 15-minute expiry. The database stores only a SHA-256 token digest. Configure SMTP before enabling recovery:

```powershell
$env:SMTP_HOST = 'smtp.example.com'
$env:SMTP_PORT = '587'
$env:SMTP_USER = 'your smtp account'
$env:SMTP_PASSWORD = 'your smtp app password'
$env:FRONTEND_URL = 'http://localhost:5173'
```

The reset endpoint deliberately returns the same message whether or not an email exists. The user sets a new password through the emailed link; passwords are stored using BCrypt. Existing desktop PBKDF2 and BCrypt hashes remain verifiable, and legacy PBKDF2 hashes upgrade to BCrypt after a successful login.

## 4. Start React

Install Node.js 20.19+ or 22.12+ and run in `E:\flight\webapp\frontend`:

```powershell
npm install
npm run dev
```

Open `http://localhost:5173`. Vite proxies `/api` requests to Spring Boot at port 8080. For deployment, serve the built frontend behind HTTPS and set `FRONTEND_URL` to its exact origin.

## Main API routes

- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`
- `POST /api/auth/admin/register` (requires admin passkey)
- `POST /api/auth/password-reset/request`, `POST /api/auth/password-reset/complete`
- `GET /api/flights?from=&to=&date=&maxPrice=`, `GET /api/flights/{id}/seats`
- `POST /api/bookings`, `POST /api/bookings/{id}/pay`, `GET /api/bookings/mine`
- `GET /api/bookings/boarding-passes`, `POST /api/bookings/{id}/cancellation-request`
- Admin-only `/api/admin/users`, `/api/admin/bookings`, `/api/admin/cancellation-requests`, and `/api/admin/flights` routes
- Admin-only `GET /api/admin/flights/{id}/manifest` returns active passenger rows for a pre-flight manifest; verification numbers are masked by default in the React report and CSV export.

Seat maps poll active tickets every 2.5 seconds. Booking creation locks the flight row and checks all requested seats in the same transaction, so a concurrent booking receives a clear seat-taken error. Payment is simulated; no card or bank details are collected. Refunds are recorded according to the 48h / 12h / 2h time tiers when an administrator approves a cancellation request.

New bookings close exactly 30 minutes before departure: booking is permitted at `departure_time - 30 minutes` and rejected after that instant. The backend enforces this while holding a database row lock; the frontend refreshes the status clock and disables seat selection at cutoff. A scheduled backend task persists `SCHEDULED`, `CLOSING_SOON`, `DEPARTED`, and `LANDED` states every 15 seconds, using both scheduled departure and arrival times. Passenger boarding passes show live status badges, and the admin dashboard has a Departed / Landed view linked to historical manifests.

## Security deployment notes

Use HTTPS, a private generated `JWT_SECRET`, a non-root database account, SMTP credentials stored in a secret manager, and a private `ADMIN_PASSKEY`. The browser keeps the 30-minute access token in `sessionStorage`; protect the frontend against script injection and do not deploy the development Vite server as the production web server.

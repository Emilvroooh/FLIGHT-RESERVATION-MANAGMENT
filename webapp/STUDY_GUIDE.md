# Skyward Web App: Code Map and Group Study Guide

This guide describes the current project files in this repository. The old desktop application is still separate; the web conversion lives in `webapp/` and uses its own database (`flight_booking_web`).

## What replaces what?

| Desktop Swing concept | Web application equivalent | Where to look |
|---|---|---|
| `JFrame` / `AppFrame` | React application shell and browser page | `frontend/src/App.jsx` |
| `JPanel` / Swing screen | React component and JSX | `AuthScreen`, `PassengerHome`, `AdminHome` in `App.jsx` |
| `JButton.addActionListener(...)` | `onClick`, form `onSubmit`, and `fetch` | `App.jsx` |
| `JTable` | React list/table built from API data | `FlightCard`, `BoardingCard`, `DataTable` in `App.jsx` |
| Swing model / Java bean | JPA entity (database row) and API DTO (request/response) | `backend/.../entity/`, `api/ApiDtos.java` |
| Swing `dao` package / JDBC CRUD | Spring Data repository for common persistence; `BookingService` uses JDBC for the seat-lock transaction | `backend/.../repository/`, `service/BookingService.java` |
| Swing controller / event handler | REST controller and service | `backend/.../api/`, `service/` |
| Swing login form | React `AuthScreen` → `/api/auth` → Spring Security/JWT | `App.jsx`, `AuthController.java`, `security/` |

Java source files end in `.java`. Swing is a Java UI framework/package style, not a `.swing` file extension. The desktop project may have `ui` and `dao` packages; the web project has `api`, `entity`, `repository`, `security`, and `service` packages.

## Request flow to remember

1. A React component collects form values.
2. `api()` sends JSON to a REST endpoint and attaches the JWT after login.
3. A controller validates the request and calls a repository or service.
4. JPA repositories read/write mapped entities. `BookingService` uses one database transaction and locks the flight row while checking and reserving seats.
5. The controller returns JSON; React updates the view.

## Main files, side by side with their job

| File | Important code / responsibility | Why it is needed |
|---|---|---|
| `backend/src/main/java/com/skyward/entity/UserEntity.java` | `@Entity`, `@Table(name="users")`, `@Id`, `@GeneratedValue`, `password_hash` mapping | Maps one Java object to one user row. The password field stores a hash, not the raw password. |
| `backend/src/main/java/com/skyward/entity/FlightEntity.java` | `@ManyToOne` airline relation and economy/business capacities and fares | Defines flight data and the database relationship to its airline. |
| `backend/src/main/java/com/skyward/entity/BookingEntity.java` | User, flight, passenger, payment and ticket relations | Represents a booking and its related records. |
| Other `entity/*Entity.java` files | `AirlineEntity`, `PassengerEntity`, `PaymentEntity`, `TicketEntity`, cancellation and reset-token rows | Map the remaining schema tables. Relationships are expressed with JPA annotations. |
| `backend/src/main/java/com/skyward/repository/FlightRepository.java` | `extends JpaRepository<FlightEntity, Long>` plus a JPQL search query | Gives CRUD operations and a route/date/fare search without writing the basic SQL by hand. |
| `repository/TicketRepository.java` | Query for active seat numbers and counts by cabin | Feeds current occupancy to the seat map and flight cards. |
| `repository/UserRepository.java` | Email lookup and name/email typeahead search | Supports login, registration uniqueness, and admin autocomplete. |
| `backend/src/main/java/com/skyward/api/AuthController.java` | Register/login/reset endpoints; admin passkey validation; password encoder; JWT issue | Connects the auth UI to user persistence and access tokens. |
| `backend/src/main/java/com/skyward/security/SecurityConfig.java` | Password encoder, stateless security, JWT filter and route rules | Protects API routes and reads the bearer token on each request. |
| `backend/src/main/java/com/skyward/api/FlightController.java` | `GET /api/flights`, `/cities`, `/airlines`, `/{id}/seats` | Provides search results and a fresh occupied/available seat list. |
| `backend/src/main/java/com/skyward/service/BookingService.java` | `@Transactional`, `SELECT ... FOR UPDATE`, seat validation, passenger/ticket/payment inserts | Makes the final reservation atomic so two people cannot claim one seat at the same time. |
| `backend/src/main/java/com/skyward/api/BookingController.java` | Booking creation, payment, passenger booking list, boarding passes, cancellation request | Exposes user booking actions as HTTP endpoints. |
| `backend/src/main/java/com/skyward/api/AdminController.java` | User lookup/profile, flight administration, booking review, cancellation approval | Provides the protected administrator workflow. User typeahead/profile lookups use JPA; some admin reports still use JDBC. |
| `frontend/src/App.jsx` | `api()`, `AuthScreen`, `PassengerHome`, `SeatBookingModal`, `BoardingCard`, `AdminHome` | Main React client and all major screens requested. |
| `frontend/src/styles.css` | Obsidian, teal, pearl colors; input, card, seat styles | Shared Tailwind/CSS theme and readable component styling. |
| `database/schema.sql` | MySQL tables, keys, constraints, and airlines | Creates the web app database schema. It is a separate database from the Swing app. |
| `database/seed_domestic_200_flights.sql` | Domestic flight seed data | Adds the expanded flight list after schema and airline seed scripts. |

### Java / DAO / Swing comparison examples

**Old Swing DAO shape** (simplified):

```java
String sql = "SELECT * FROM users WHERE email = ?";
try (PreparedStatement statement = connection.prepareStatement(sql)) {
    statement.setString(1, email);
    // executeQuery and map ResultSet to a User
}
```

The DAO owns SQL and row mapping. In the web project, common lookups are declared in a repository:

```java
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);
}
```

Spring Data builds that query from the method name. The `UserEntity` tells JPA how a user row maps to Java fields.

**Old Swing event** (simplified):

```java
searchButton.addActionListener(event -> searchFlights());
```

In React, the corresponding event calls an API and updates component state:

```jsx
<button onClick={searchFlights}>Search</button>
```

The browser does not connect directly to MySQL. The REST controller is the boundary between UI and server/database.

## Features and where to trace them

- **Email login / admin passkey:** `AuthScreen` → `AuthController` → `UserDetailsServiceImpl` / `AdminRepository` → `JwtService`.
- **Password recovery:** UI requests a reset link; backend stores only a hash of a short-lived reset token and emails the link. The user sets a new password on the reset screen. This is safer than emailing a temporary password.
- **Flight budget filter:** search form in `PassengerHome` → `FlightController.search` → `FlightRepository.search`.
- **Seat layout:** `FlightController.seats` calculates A–F and cabin class; `SeatBookingModal` renders the buttons. Occupancy is refreshed by the UI and checked again inside `BookingService` at reservation time.
- **Multiple passengers:** each passenger row is paired with one selected seat, then sent in the booking request.
- **Boarding passes:** confirmed issued tickets are returned to the account and rendered by `BoardingCard`.
- **Cancellation/refund:** passenger submits a request; admin approves it; `BookingService` calculates the time tier and saves the refund summary.
- **Admin typeahead:** `AdminHome` debounces typing and requests `/api/admin/users?q=...`; `UserRepository` searches names/emails.

## Random-line practice for group members

For each prompt, first state the purpose of the missing line, then rewrite it. Answer key follows.

1. In `UserEntity`, what do `@Id` and `@GeneratedValue(strategy = GenerationType.IDENTITY)` do?
2. In `FlightEntity`, what does `@ManyToOne` mean for a flight and airline?
3. Why does `UserRepository` extend `JpaRepository<UserEntity, Long>`?
4. What does `@Transactional` protect in `BookingService.create`?
5. Why does booking use `SELECT ... FOR UPDATE` before checking seats?
6. What is the purpose of `passwords.encode(...)` in registration?
7. What does the JWT filter do with `Authorization: Bearer ...`?
8. In React, why does `useEffect` refresh seat data when the selected flight changes?
9. Why does the server check seat availability again after the visual seat map loaded?
10. How is the cancellation refund percentage selected, and where is the explanation stored?
11. What does a repository method such as `findByEmailIgnoreCase` mean?
12. Why should an API controller return DTOs/maps instead of serializing JPA entities directly?

### Answer key

1. `@Id` marks the primary key. `IDENTITY` asks the database to generate the key when inserting a row.
2. Many flight rows can point to one airline row; the `airline_id` foreign key stores that association.
3. It inherits standard CRUD methods and pagination for `UserEntity` whose ID type is `Long`.
4. It groups reservation reads/writes into one commit or rollback. If an insert or validation fails, partial booking rows should not be left behind.
5. It serializes competing reservations for the same flight row. A second transaction waits, then sees the first transaction’s seat assignment before proceeding.
6. It hashes the plaintext password with the configured password encoder before storage. The original password should never be stored.
7. It validates the JWT, builds the authenticated principal, and makes that identity available to protected endpoints.
8. It loads the correct flight’s seat status and avoids showing stale seats from a previously selected flight.
9. UI data can become stale between display and click. The final server-side transaction is authoritative and prevents double booking.
10. The service compares time until departure against the 48h, 12h, and 2h thresholds; it stores percentage, amount, time difference, and reason in the cancellation record.
11. Spring Data derives a case-insensitive email lookup from the method name and entity property.
12. Entities include persistence relationships and internal fields; DTOs let the API control validation and exactly what data is exposed.

## Database and configuration notes

- The checked-in `schema.sql` is for **MySQL 8**. The backend includes both MySQL and PostgreSQL JDBC drivers, but use a database-specific schema and JDBC URL that match the database you actually run.
- Configure `FLIGHT_DB_URL`, `FLIGHT_DB_USER`, `FLIGHT_DB_PASSWORD`, and `JWT_SECRET` in the environment. SMTP variables enable email delivery for password recovery.
- `ADMIN_PASSKEY` defaults to the requested classroom value for local setup. For a deployed service, set it outside source control and change the value.
- Use `schema.sql`, then `seed_airlines_flights.sql`, then `seed_domestic_200_flights.sql` in the `flight_booking_web` database. This does not run against or overwrite the Swing app's `flight_booking` database.
- Password recovery uses an emailed, one-time, expiring link rather than mailing a generated temporary password. This keeps a usable password out of email and follows the reset-token flow implemented in the backend.

## Important implementation detail to explain honestly

The app now has JPA entities and Spring Data repositories, and authentication, flight search, and admin user search use them. `BookingService` still uses `JdbcTemplate` for the critical lock-and-reserve transaction and related reports. This is intentional in the current project: the row lock and final occupied-seat check happen together. It is a mixed JPA/JDBC implementation, not a claim that every query has already been converted to JPA.

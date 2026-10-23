# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 4 / Java 21 REST API for a car-sharing service: JWT auth, cars, rentals, Stripe Checkout payments, Telegram notifications. MySQL schema is managed by Liquibase (`src/main/resources/db/changelog/`; add new numbered changesets and register them in `db.changelog-master.yaml` — never edit applied ones).

## Commands

Use the Maven wrapper (`.\mvnw.cmd` on Windows, `./mvnw` elsewhere).

- Full CI check: `.\mvnw.cmd clean verify` — runs Checkstyle (validate phase), tests, and the JaCoCo gate (build fails below 60% line coverage).
- Single test class: `.\mvnw.cmd test -Dtest=RentalServiceImplTest`
- Single test method: `.\mvnw.cmd test -Dtest=RentalServiceImplTest#createRental_shouldThrowWhenCarDoesNotExist`
- Checkstyle only: `.\mvnw.cmd checkstyle:check` (rules in `config/checkstyle.xml`: no star imports, no unused imports)
- Run locally: `.\mvnw.cmd spring-boot:run` (needs MySQL on localhost:3306 and the env vars from `.env.sample`)
- Run app + MySQL in Docker: copy `.env.sample` to `.env`, fill it in, then `docker compose up --build`

`CarSharingAppApplicationTests` is the only integration test: it starts a MySQL 8.4 Testcontainer (`@ServiceConnection`), so Docker must be running. All other tests are plain Mockito unit tests (`@ExtendWith(MockitoExtension.class)`) that call controllers/services directly — no MockMvc, no Spring context. Test config lives in `src/test/resources/application.properties` with dummy Stripe/Telegram/JWT values.

## Architecture

Package root: `mate.academy.car_sharing_app`. Layering is controller → service interface/`*Impl` → Spring Data repository, with MapStruct mappers (`mapper/`) converting entities ↔ DTOs (`dto/<area>Dto/`). Services live in per-feature subpackages (`service/rentalService/`, `service/paymentService/`, …).

- **Auth**: stateless JWT. `JwtAuthenticationFilter` validates the bearer token; the user's email is the principal. Controllers pass `authentication.getName()` (email) into services, and services load the `User` by email.
- **Authorization is split in two places**: `SecurityConfig` holds the public endpoint list (`/register`, `/login`, `/health`, `GET /cars`, `/payments/success`, `/payments/cancel`, Swagger) and controllers use `@PreAuthorize` for MANAGER-only actions. Per-record ownership (a CUSTOMER only sees their own rentals/payments; MANAGER sees all and can filter by `user_id`) is enforced inside the service implementations by checking `user.getRole()`.
- **Roles**: registration always creates a `CUSTOMER`, and Liquibase seeds no users. Changing roles (`PUT /users/{id}/role`) requires MANAGER, so the first manager must be set directly in the `users` table.
- **Transactions**: `CarServiceImpl`, `RentalServiceImpl` and `UserServiceImpl` are class-level `@Transactional` (the `jakarta.transaction` one); `PaymentServiceImpl` is not, and its methods mix DB writes with Stripe API calls.
- **Errors**: services throw `EntityNotFoundException` (404), `RentalException` / `PaymentException` (400), `RegistrationException` (409); `GlobalExceptionHandler` maps them to `{status, error, message}` JSON.
- **Rental flow**: creating a rental is blocked if the user has any `PENDING` or `EXPIRED` payment; it decrements `Car.inventory` and sends a Telegram notification. Returning sets `actualReturnDate` and increments inventory.
- **External integrations** are behind interfaces so they can be mocked: `StripeService` (Checkout sessions; success/cancel URLs built from `app.base-url`) and `NotificationService` (`TelegramNotificationService`).
- **Scheduled jobs** (`@EnableScheduling` on the main class): `PaymentExpirationScheduler` marks pending payments `EXPIRED` every minute by querying Stripe; `OverdueRentalService` sends overdue-rental Telegram messages daily at 09:00.

## Domain rules

- Rental payment amounts use `ChronoUnit.DAYS.between(rentalDate, returnDate)`, so the rental period is calculated as the number of days between the two dates. Late-return fines use overdue days multiplied by the car's daily fee and `FINE_MULTIPLIER`; a fine cannot be created when there are no overdue days. This logic lives in `PaymentAmountCalculator`.
- Stripe success redirects are not trusted by themselves: `successPay` retrieves the Checkout Session from Stripe and changes the payment to `PAID` only when Stripe reports `payment_status=paid`. Pending payments are checked for expiration every minute. Renewal is allowed only for `EXPIRED` payments and updates the existing payment record with a new Checkout Session.

## Git workflow

Never commit to `main`. Create a branch (`fix/...`, `docs/...`, `feature/...`) first, push it and merge via a GitHub PR. Commit subjects use a tag prefix: `[FIX] ...`, `[DOCS] ...`. CI (`.github/workflows/ci.yml`) runs `./mvnw clean verify` on pushes to `main` and on PRs.

## Configuration

Secrets come from env vars (see `.env.sample`): `MYSQLDB_*`, `JWT_SECRET` (≥32 bytes), `TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHAT_ID`, `STRIPE_SECRET_KEY`, `APP_BASE_URL`. `.env` is gitignored; never commit it. In Docker Compose the app reaches MySQL via `SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/...`.

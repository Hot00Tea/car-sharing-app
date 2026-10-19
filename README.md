# Car Sharing Service

A REST API for managing a car-sharing service. The application supports customer and manager accounts, car inventory, rentals and returns, Stripe payment sessions, and Telegram notifications.

## Stack

- Java 21 and Spring Boot
- Spring Security with JWT authentication
- MySQL and Liquibase
- Stripe Checkout
- Telegram Bot API
- Maven, Checkstyle, JaCoCo, and Testcontainers
- Docker Compose

## Run with Docker Compose

Requirements: Docker Desktop with Docker Compose.

1. Create a local environment file:

   ```powershell
   Copy-Item .env.sample .env
   ```

2. Edit `.env` and set every value. Use Stripe test credentials and your Telegram bot token/chat ID. `JWT_SECRET` must be a random secret of at least 32 bytes. Keep `.env` private; only `.env.sample` belongs in Git.

3. Build and start the app and MySQL:

   ```powershell
   docker compose up --build
   ```

The API is available at `http://localhost:8080`. MySQL data is stored in the `mysql_data` Docker volume and survives container restarts.

Useful Compose commands:

```powershell
docker compose logs -f app
docker compose down
```

`docker compose down -v` also deletes the database volume and its data.

## Demo deployment to AWS EC2

This deployment is intended for a short-lived project demo, not production. It runs the app and MySQL on one EC2 instance. The database is not managed or backed up, and the API uses plain HTTP. AWS charges for the EC2 instance, its storage, and network usage; check the estimate in your selected region before launching and terminate the instance when you are finished.

1. Launch an Ubuntu EC2 instance with enough memory for both containers (at least 2 GiB is recommended). In its security group, allow SSH (`22`) only from your own IP and allow the app port (`8080`) for the users who need to test it. Do not open the MySQL port (`3306`).
2. Connect over SSH, install Docker Engine and the Compose plugin using the [official Ubuntu instructions](https://docs.docker.com/engine/install/ubuntu/), then clone this repository and enter its directory.
3. Create the environment file and set the production callback URL to the instance's reachable public address:

   ```bash
   cp .env.sample .env
   nano .env
   ```

   Set the database values, a random `JWT_SECRET` of at least 32 bytes, Telegram credentials, a Stripe **test** secret key, and `APP_BASE_URL=http://<EC2-public-address>:8080`. Keep `.env` private and never commit it.
4. Start the app and database:

   ```bash
   docker compose up -d --build
   docker compose ps
   ```

5. Check `http://<EC2-public-address>:8080/health` and open `http://<EC2-public-address>:8080/swagger-ui/index.html`.

The compose file keeps MySQL private to the Docker network and stores its data in the `mysql_data` volume on the instance. Replacing or terminating the instance can lose that data. This demo setup does not provide HTTPS, managed database backups, or high availability.

## Run locally

Requirements: JDK 21, Docker Desktop (for Testcontainers), and a reachable MySQL database.

Set the environment variables listed in `.env.sample` in your shell, configure the database URL if it is not `jdbc:mysql://localhost:3306/<MYSQLDB_DATABASE>`, then start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

On Linux or macOS, use `./mvnw spring-boot:run`.

## API and documentation

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health check: `GET http://localhost:8080/health` → `{"status":"UP"}`

### Main endpoints

| Area | Endpoints |
| --- | --- |
| Authentication | `POST /register`, `POST /login` |
| Users | `GET /users/me`, `PATCH /users/me`, `PUT /users/{id}/role` |
| Cars | `GET /cars`, `GET /cars/{id}`, `POST /cars`, `PUT /cars/{id}`, `DELETE /cars/{id}` |
| Rentals | `POST /rentals`, `GET /rentals`, `GET /rentals/{id}`, `POST /rentals/{id}/return` |
| Payments | `POST /payments`, `GET /payments`, `GET /payments/{id}`, `GET /payments/success`, `GET /payments/cancel`, `POST /payments/{id}/renew` |

Most endpoints require `Authorization: Bearer <JWT>`. Car listing and the health check are public. Manager-only operations include changing user roles and creating, updating, or deleting cars. Rental and payment list endpoints support pagination; rentals can be filtered with `user_id` and `is_active` according to the caller's role.

Register with `POST /register`, then exchange credentials at `POST /login` to get a JWT. Open Swagger UI to view request and response schemas.

## Tests and quality checks

Run the same verification command used in CI:

```powershell
.\mvnw.cmd clean verify
```

The build runs Checkstyle, the test suite, and JaCoCo. Verification fails if line coverage falls below 60%. Tests use Testcontainers with MySQL, so Docker Desktop must be running.

## Configuration

The app reads credentials from environment variables. See `.env.sample` for the complete list. Never commit `.env`, Stripe secrets, JWT secrets, or Telegram bot credentials.

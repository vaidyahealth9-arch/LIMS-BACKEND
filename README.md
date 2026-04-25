# LIMS Project

This is a Spring Boot project for a Laboratory Information Management System (LIMS).

## Running the application

To run the application locally, you need to activate the `local` Spring profile. This can be done by setting the `spring.profiles.active` environment variable or by passing a command-line argument.

### Using command-line argument

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=local -Dspring-boot.run.jvmArguments=-Duser.timezone=Asia/Kolkata

or
./mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=local" "-Dspring-boot.run.jvmArguments=-Duser.timezone=Asia/Kolkata"
```

### Using an environment variable

**Windows:**

```bash
set SPRING_PROFILES_ACTIVE=local
./mvnw spring-boot:run
```

**Linux/macOS:**

```bash
export SPRING_PROFILES_ACTIVE=local
./mvnw spring-boot:run
```

**Note:** When running with the `local` profile, the application will use the `application-local.properties` file for configuration. This file is configured to use a local PostgreSQL database and disables Google Cloud SQL integration. Make sure you have a local PostgreSQL instance running and have updated the database credentials in `src/main/resources/application-local.properties` if they differ from the defaults.

## Profile layout

- `application.properties` contains shared defaults.
- `application-local.properties` contains local Docker/PostgreSQL settings and disables GCP integrations for local development.
- `application-gcp.properties` contains Google Cloud SQL, Secret Manager, and GCS-specific settings.

For cloud environments, activate both `prod,gcp` profiles (for example via `SPRING_PROFILES_ACTIVE=prod,gcp`).

## Schema migration strategy (Flyway)

This project now supports a migration-first schema path using Flyway.

- Baseline migration: `src/main/resources/db/migration/V1__baseline.sql`
- Current incremental migration: `src/main/resources/db/migration/V2__add_price_and_code_to_organization_test_analytes.sql`

### Key environment toggles

- `FLYWAY_ENABLED` (default non-local: `true`, local: `false`)
- `FLYWAY_BASELINE_ON_MIGRATE` (default: `true`)
- `FLYWAY_BASELINE_VERSION` (default: `1`)
- `JPA_DDL_AUTO` (default non-local: `validate`, local: `update`)

### Recommended usage

- **Local quick setup**: keep `JPA_DDL_AUTO=update`, optionally `FLYWAY_ENABLED=false`.
- **Migration testing locally**: set `FLYWAY_ENABLED=true` and ensure PostgreSQL is running.
- **Shared/prod-like environments**: use `FLYWAY_ENABLED=true` with `JPA_DDL_AUTO=validate`.

> Note: Current baseline migration is intentionally a marker migration to start Flyway version tracking for existing environments. Add all future schema changes as new `V3+` scripts.

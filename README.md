# LIMS Project

This is a Spring Boot project for a Laboratory Information Management System (LIMS).

## Running the application

To run the application locally, you need to activate the `local` Spring profile. This can be done by setting the `spring.profiles.active` environment variable or by passing a command-line argument.

### Using command-line argument

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=local

or 
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=local
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

LIMS Backend — Configuration Guide

Purpose
- Document env file locations, precedence, and how to run the backend locally.

Env files
- `.env.example` — template (committed)
- `.env` — local non-secret defaults (optional)
- `.env.local` — developer overrides (gitignored)

Precedence (highest → lowest)
1. Process environment variables (e.g., CI/CD or Docker Compose `environment:`)
2. `.env.local` (developer overrides, do not commit)
3. `.env.development` / `.env.production` (team-shared non-secret files)
4. `.env` (base defaults)

Run commands
- Development (hot-run):
  - `./mvnw spring-boot:run`
- Build artifact:
  - `./mvnw clean package`
- Start from built JAR:
  - `java -jar target/<artifact>.jar`

Notes
- Do NOT store secrets in committed files. Use a secret manager or inject via CI/Docker Compose at runtime.
- This project currently reads Spring configuration via Spring Boot conventions (application.properties/yml and environment variables). We recommend centralizing any new runtime parsing under `src/main/java/.../config` for consistency.

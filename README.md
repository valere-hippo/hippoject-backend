# Hippoject Backend

Spring Boot backend for Hippoject, a Jira-like project and issue management platform.

## Current foundation

- Spring Boot REST API
- PostgreSQL + Flyway ready
- Keycloak-ready JWT security
- project API
- issue API
- comment API
- global error handling

## Run locally

```bash
./mvnw spring-boot:run
```

## Test

```bash
./mvnw test
```

Note: local compilation requires a full JDK toolchain.

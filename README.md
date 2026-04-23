# Hippoject Backend

Spring Boot backend for Hippoject, a Jira-like project and issue management platform.

## Current scope

The backend currently provides:

- projects with archive/restore flows
- issues with labels, issue types, epics, sprint assignment, archive/restore flows
- comments and mention notifications
- sprint planning and lifecycle actions
- project members and project-level roles
- saved issue filters
- dashboard summary API
- project activity and persisted audit history
- workspace directory API
- realtime updates over WebSocket
- Keycloak-ready JWT auth and role guards
- optional SMTP email notifications

## Runtime requirements

- Java 21
- PostgreSQL
- optional Keycloak for auth
- optional SMTP server for email notifications

## Important environment variables

### Database

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`

### Security

- `SECURITY_ENABLED=false` for local unsecured dev
- `JWT_JWK_SET_URI` for Keycloak / JWT validation when security is enabled

### Email notifications

- `EMAIL_NOTIFICATIONS_ENABLED`
- `SMTP_SERVER`
- `SMTP_PORT`
- `EMAIL`
- `PASSWORD`

## Local development

```bash
./mvnw spring-boot:run
```

Default local expectations:

- backend: `http://localhost:8080`
- Keycloak: `http://localhost:8081`
- frontend origin allowed via CORS for localhost dev
- WebSocket realtime endpoint: `/ws/realtime`

## Tests

```bash
./mvnw test
```

## Notes

- Flyway manages the database schema.
- Local compilation needs a full Java 21 JDK toolchain.
- In this workspace, frontend builds were used as the main fast sanity check because the local Java toolchain was incomplete for full Maven verification.

# Lab 07
Lab 07 is a group project for tracking employee working hours at a company, consisting of a REST API and a web application (front end and back end). The back end is built with Java and Spring Boot. The project prioritizes clean code and the MVC design pattern, with the model and controller in the API and the view in the front end.

## Problems it solves

This API solves simple problems when recording employee data, such as:

- employee clock-in time
- employee clock-out time
- hour bank (or overtime)
- time declarations (when someone needs to arrive later or leave earlier)
- medical certificates (absences)

## Conceptual model
Class diagram

## Run database

**Everything in Docker** (API at http://localhost:8090):

```bash
docker compose up -d --build
```

**Database in Docker, API from the IDE** (always start the database first):

```bash
docker compose up -d postgres
.\mvnw.cmd spring-boot:run
```

On Linux/macOS, use `./mvnw spring-boot:run`.

**Tests** (H2 in memory, no Docker needed): `.\mvnw.cmd test`

## Database

| Host | Port | Database | User | Password |
|------|------|----------|------|----------|
| `localhost` | `5432` | `ponto` | `postgres` | `root` |

## Migrations (Flyway)

Schema changes live in `src/main/resources/db/migration/` and run on startup. Hibernate validates the schema against the entities and the app won't start on a mismatch.

- Name files `V<number>__<description>.sql` (capital `V`, two underscores).
- Never edit a migration that has already run: create a new one.
- Entity change = new migration in the same commit.

## Tech stack

Java 21 · Spring Boot 4.1.1 · Spring Data JPA · Flyway · PostgreSQL 16 · H2 · Lombok · Docker

## Technologies used

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Boot Starter Web
- Spring Boot DevTools
- Docker
- PostgreSQL

## Endpoints
| Method | Endpoint           | Description                                                     |
|--------|--------------------|------------------------------------------------------------------|
# Lab 07
O lab 07 se trata de um projeto em grupo para o controle de ponto de uma empresa contendo uma API REST e uma aplicação web (Front e back). Construida em java Spring boot via back end.  prioriza codigo limpo e implementação de padrões de projetos MVC sendo model e control pela API e view pelo front end

## Problemas a serem soluciondos

essa api serve para resolver problemas simples na hora de registrar dados sobre funcionários como

- horário de entrada do funcionário
- horário de saida do funcionário
- banco de horas (ou horas extras)
- declaração de horas (caso precise entrar mais tarde ou sair mais cedo)
- atestados (ausências)

## Rodar o projeto

- como o projeto está sido dockerizado só é necessário rodar os comandos

`Docker compose up` (primeira vez subindo os container na maquina)

`Docker compose -f docker-compose.yml up build -d` (conforme atualizações vão surgindo)

## modelo conceitual
Diagrama de classe

## Run database

**Everything in Docker:**

`docker compose up -d --build`   # API at http://localhost:8090

**Database in Docker, API from the IDE:**

`docker compose up -d postgres  # always start the database first`
`.\mvnw.cmd spring-boot:run     # ./mvnw on Linux/macOS`


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

## Stacks utilizadas

- Java 21
- Spring boot
- Spring data JPA
- Spring boot starter web
- Spring book dev tools
- docker
- postgresSQL

## Endpoints
| Método | Endpoint           | Descrição                                                       |
|--------|--------------------|------------------------------------------------------------------|




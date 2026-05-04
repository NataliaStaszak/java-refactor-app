Java Refactoring Test Project
=============================

Hi, My name is Natalia Staszak and this repository contains my solution to a recruitment task.
I have decided to create a separate repository as the amount of changes is huge.
Let me know if you have any questions. I am avaliable on Teams and slack.

## 1. New setup instruction
The new setup is utilising Docker Compose to run a PostgreSQL Database


  ```bash
  cp src/main/resources/secret.properties.example src/main/resources/secret.properties
  cp .env.example .env

  Fill in both files with your database credentials.

  2. Start the database

  docker compose up -d

  3. Start the application

  ./gradlew bootRun

  API is available at http://localhost:8080/users.

  Run tests (no Docker required)

  ./gradlew test
  ```


## 2. Brief summary of what has been changed
1. Replaced the 2-layer design (Controller + DAO) with a standard 3-layer Spring architecture
2. All endpoints changed to correct HTTP semantics
3. All endpoints changed to correct HTTP semantics:

| Operation | Before | After |
|-----------|--------|-------|
| Create user | `GET /add/` | `POST /users` |
| Get all users | `GET /find/` | `GET /users` |
| Get by name | `GET /find/?name=` | `GET /users?name=` |
| Update user | `GET /update/` | `PATCH /users/{email}` |
| Delete user | `GET /delete/` | `DELETE /users/{email}` |

4. Validation & Error Handling added
5. Sample Data inserted
6. Integration Tests have been rewrited
7. Unit Tests has been created for Controller and Service

**Note:** Tests use an in-memory H2 database — no Docker or PostgreSQL required. Flyway is disabled during tests; the schema is created automatically by Hibernate.

## 3. Bonus tasks
1. The application uses PostgreSQL 16 as the persistence layer, running via Docker Compose.
2. All database access is implemented using Spring Data JPA with Hibernate as the ORM provider.
3. Database schema and seed data are managed by Flyway migrations located in `src/main/resources/db/migration`

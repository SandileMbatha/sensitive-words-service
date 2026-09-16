# Sensitive Words Service

A small Spring Boot microservice that:

1. System that manages a list of "sensitive words" (CRUD).
2. Exposes a single business endpoint that takes a free-text message and returns it with every
   sensitive word starred out, e.g.

   ```
   Request:  "You need to create a string"
   Response: "You need to ****** a string"
   ```

## How to run

Requires JDK 21 and Docker.

Build the jar first:

```
mvn clean package -DskipTests   # requires JDK 21
```

Then start the database and the service:

```
docker compose up --build
```

The service will be available at http://localhost:8080. Swagger UI is at
http://localhost:8080/swagger-ui/index.html.

## What would you do to enhance performance of your project?

- The list of sensitive words is kept in memory (cached) instead of reading it from the database
  on every request. The cache is cleared automatically whenever a word is added, changed or
  deleted, so it never goes stale.
- The word column in the database has a unique index, so checking if a word already exists and
  looking up a word is fast, not a full table scan.
- The query used to sanitize a message only reads the word text, not the whole row, so it does
  not load data that is not needed.
- The database connection pool (the Spring Boot default) would be tuned based on real load
  testing rather than guessing at numbers.
- The pattern used to split a message into words is built once and reused, instead of being
  rebuilt on every request.
- If the word list or the traffic grew a lot, I would add pagination to the list endpoint, add a
  read-only copy of the database for read traffic, and run load tests to find the real
  bottleneck before making any more changes.

## What additional enhancements would you add to make it more complete?

- Add authentication and authorization on the CRUD endpoints, since right now anyone who can
  reach the service can add, change or remove words.
- Support matching whole phrases, not just single words. Right now only single words are
  matched, so a phrase made of more than one word is not caught as one unit.
- Add pagination and search to the list endpoint once the word list gets big.
- Keep a record of who added, changed or removed a word, not just when it happened.
- Add a way to upload many words at once instead of adding them one at a time.
- Add integration tests that run against a real SQL Server database so the tests also check the
  database migrations, not just the code.
- Add rate limiting on the sanitize endpoint so it cannot be overloaded by too many requests.
- Add better logging and basic monitoring so it is easier to see what the service is doing once
  it is running in production.

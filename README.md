# ![RealWorld Example App using Kotlin and Spring](example-logo.png)

[![Actions](https://github.com/gothinkster/spring-boot-realworld-example-app/workflows/Java%20CI/badge.svg)](https://github.com/gothinkster/spring-boot-realworld-example-app/actions)

> ### Spring boot + MyBatis codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld-example-apps) spec and API.

This codebase was created to demonstrate a fully fledged full-stack application built with Spring boot + Mybatis including CRUD operations, authentication, routing, pagination, and more.

For more information on how to this works with other frontends/backends, head over to the [RealWorld](https://github.com/gothinkster/realworld) repo.

# *NEW* GraphQL Support  

Following some DDD principles. REST or GraphQL is just a kind of adapter. And the domain layer will be consistent all the time. So this repository implement GraphQL and REST at the same time.

The GraphQL schema is https://github.com/gothinkster/spring-boot-realworld-example-app/blob/master/src/main/resources/schema/schema.graphqls and the visualization looks like below.

![](graphql-schema.png)

And this implementation is using [dgs-framework](https://github.com/Netflix/dgs-framework) which is a quite new java graphql server framework.
# How it works

The application uses Spring Boot (Web, Mybatis).

* Use the idea of Domain Driven Design to separate the business term and infrastructure term.
* Use MyBatis to implement the [Data Mapper](https://martinfowler.com/eaaCatalog/dataMapper.html) pattern for persistence.
* Use [CQRS](https://martinfowler.com/bliki/CQRS.html) pattern to separate the read model and write model.

And the code is organized as this:

1. `api` is the web layer implemented by Spring MVC
2. `core` is the business model including entities and services
3. `application` is the high-level services for querying the data transfer objects
4. `infrastructure`  contains all the implementation classes as the technique details

# Security

Integration with Spring Security and add other filter for jwt token process.

The secret key is stored in `application.properties`.

# Database

It uses a ~~H2 in-memory database~~ sqlite database (for easy local test without losing test data after every restart), can be changed easily in the `application.properties` for any other database.

## Sample Data & Login Credentials

The application includes seed data with sample users, articles, tags, comments, and social interactions. You can log in with any of these accounts:

| Username | Email | Password |
|----------|-------|----------|
| johndoe | john@example.com | password123 |
| janedoe | jane@example.com | password123 |
| bobsmith | bob@example.com | password123 |

**Seed data includes:**
- 3 users with profiles
- 5 articles on Spring Boot, REST APIs, Microservices, Docker, and Testing
- 7 tags (java, spring-boot, web-development, tutorial, best-practices, microservices, api-design)
- 5 comments on articles
- 6 article favorites
- 4 follow relationships between users

# Getting started

## Backend (Spring Boot)

You'll need Java 11 installed.

    ./gradlew bootRun

**Note**: `bootRun` automatically cleans and recreates the database with seed data on each run to avoid Flyway migration conflicts during development.

To test that it works, open a browser tab at http://localhost:8080/tags .  
Alternatively, you can run

    curl http://localhost:8080/tags

## Frontend (Next.js)

You'll need Node.js installed. **Recommended: Node v14-16** (specified in `frontend/.nvmrc`).

If using `nvm`, switch to the correct version:
```bash
cd frontend
nvm use
```

Then install and run:
```bash
npm install
npm run dev
```

The frontend will run on http://localhost:3000 and connect to the backend on port 8080.

**Note**: The `npm run dev` script includes `NODE_OPTIONS=--openssl-legacy-provider` for compatibility with newer Node versions, but Node 14-16 is still recommended for best compatibility.

# Try it out with [Docker](https://www.docker.com/)

You'll need Docker installed.
	
    ./gradlew bootBuildImage --imageName spring-boot-realworld-example-app
    docker run -p 8081:8080 spring-boot-realworld-example-app

# Try it out with a RealWorld frontend

The entry point address of the backend API is at http://localhost:8080, **not** http://localhost:8080/api as some of the frontend documentation suggests.

# Run test

The repository contains a lot of test cases to cover both api test and repository test.

    ./gradlew test

# Code format

Use spotless for code format.

    ./gradlew spotlessJavaApply

# Visual Regression Testing

The project includes a visual regression test suite that captures full-page screenshots and compares them against stored baselines using [ashot](https://github.com/pazone/ashot).

## Running Visual Regression Tests

```bash
./gradlew visualRegressionTest
```

## How Baselines Work

- **First run**: No baselines exist yet. The tests capture screenshots and save them as the initial baselines under `src/test/resources/selenium/baselines/`. All tests pass on the first run.
- **Subsequent runs**: Each test captures a new screenshot and compares it pixel-by-pixel against the stored baseline. If the difference exceeds the configured threshold (default `0.5%`), the test fails.

## Updating Baselines

When UI changes are intentional, update the baselines so future runs pass:

1. **Delete the outdated baseline** PNG from `src/test/resources/selenium/baselines/` and re-run the tests, or
2. **Enable and run the `updateAllBaselines` test** in `VisualRegressionTest.java` to regenerate all baselines at once.

Commit the updated baseline PNGs to the repository.

## Diff Reports

After a test run, review the outputs in:

| Directory | Contents |
|-----------|----------|
| `build/reports/selenium/visual-regression/actual/` | Actual screenshots from the latest run |
| `build/reports/selenium/visual-regression/diffs/` | Diff images highlighting pixel differences in red |

## Configuration

Visual regression settings in `src/test/resources/selenium/config.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `visual.diff.threshold` | `0.5` | Max percentage of differing pixels before a test fails |
| `visual.wait.before.screenshot` | `2000` | Milliseconds to wait after page load before capturing |

# Help

Please fork and PR to improve the project.

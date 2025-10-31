# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.7 web application built for the 10xDevs training certificate program. The project uses:
- Java 21 (Temurin toolchain)
- Gradle Kotlin DSL (build.gradle.kts)
- Spring Boot Starter Web for REST APIs
- Spring Data JPA with PostgreSQL (Supabase)
- JUnit 5 for testing

Base package: `eu.robm15.tenxdevs`

## Build and Test Commands

### Building the project
```bash
./gradlew build
```
On Windows: `gradlew.bat build`

### Running tests
```bash
./gradlew test
```

### Running a single test class
```bash
./gradlew test --tests "eu.robm15.tenxdevs.controller.TenxdevsControllerTest"
```

### Running a single test method
```bash
./gradlew test --tests "eu.robm15.tenxdevs.controller.TenxdevsControllerTest.tenxdevsDefault"
```

### Running the application locally
```bash
./gradlew bootRun
```

### Creating a JAR file
```bash
./gradlew bootJar
```
Output: `build/libs/tenxdevs-0.0.1-SNAPSHOT.jar`

### Building Docker image
The Dockerfile expects the JAR to be built first:
```bash
./gradlew build
docker build -t tenxdevs .
```

## Project Structure

The project follows standard Spring Boot conventions with a layered architecture:

- **Main application entry point**: `src/main/java/eu/robm15/tenxdevs/TenxdevsApplication.java`
- **Controllers** (`controller/`): REST endpoints annotated with `@RestController`
  - Use Spring Web annotations (`@GetMapping`, `@RequestParam`, etc.)
  - Inject repositories via `@Autowired`
- **Models** (`model/`): JPA entities annotated with `@Entity`
  - Use Jakarta Persistence annotations (`@Id`, `@GeneratedValue`, `@Column`)
- **Repositories** (`repository/`): Spring Data JPA repositories
  - Extend `JpaRepository<Entity, ID>` for CRUD operations
- **Tests** (`src/test/java/eu/robm15/tenxdevs/`):
  - Controller tests use `@SpringBootTest` and `@AutoConfigureMockMvc`
  - MockMvc is used for testing REST endpoints
- **Configuration**: `src/main/resources/application.yaml`
  - Database: PostgreSQL on Supabase (jdbc:postgresql://db.qdsvbeviduohtkskblic.supabase.co:5432/postgres)

## CI/CD Pipeline

The project uses GitHub Actions for CI/CD (`.github/workflows/deploy-develop.yml`):
- **Trigger**: On push to `develop` branch
- **Build environment**: Ubuntu with JDK 21 (Temurin distribution)
- **Build steps**:
  1. Checkout code
  2. Run `./gradlew build` (includes compilation)
  3. Run `./gradlew test` separately
  4. Build Docker image and push to GitHub Container Registry (GHCR)
  5. Deploy to VPS via SSH
- **Docker registry**: GitHub Container Registry (`ghcr.io`)
- **Deployment**: Automatically deploys container to VPS with separate dev/prod ports
  - Container name: `tenxdevs_DEV` for develop branch
  - Port mapping: VPS_PORT_NUMBER_DEV:8080

## Docker

### Building locally
```bash
./gradlew build
docker build -t tenxdevs .
```

### Running the container
```bash
docker run -d -p 8080:8080 tenxdevs
```

The Dockerfile:
- Base image: `eclipse-temurin:21-jre-jammy`
- JVM settings: MaxRAMPercentage=75.0 for container memory optimization
- Expects JAR in `build/libs/` directory

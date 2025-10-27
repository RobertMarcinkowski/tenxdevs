# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.7 web application built for the 10xDevs training certificate program. The project uses:
- Java 21 (Temurin toolchain)
- Gradle as the build system
- Spring Boot Starter Web for REST APIs
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

The project follows standard Spring Boot conventions:

- **Main application entry point**: `src/main/java/eu/robm15/tenxdevs/TenxdevsApplication.java`
- **Controllers**: `src/main/java/eu/robm15/tenxdevs/controller/`
  - REST controllers are annotated with `@RestController`
  - Endpoints use Spring Web annotations (`@GetMapping`, etc.)
- **Tests**: `src/test/java/eu/robm15/tenxdevs/`
  - Controller tests use `@SpringBootTest` and `@AutoConfigureMockMvc`
  - MockMvc is used for testing REST endpoints
- **Configuration**: `src/main/resources/application.properties`

## CI/CD

The project uses GitHub Actions for CI/CD (`.github/workflows/cicd-workflow.yml`):
- Triggers on every push
- Uses JDK 21 (Temurin distribution)
- Runs `./gradlew build` which includes compilation and tests

## Docker Deployment

The Dockerfile uses `eclipse-temurin:21-jre-jammy` as the base image and configures:
- MaxRAMPercentage=75.0 for container memory optimization
- Expects JAR file in `build/libs/` directory

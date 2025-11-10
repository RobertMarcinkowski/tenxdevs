# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.7 web application built for the 10xDevs training certificate program. The project uses:
- Java 21 (Temurin toolchain)
- Gradle Kotlin DSL (build.gradle.kts)
- Spring Boot Starter Web for REST APIs
- Spring Data JPA with PostgreSQL (Supabase) and H2 (local development)
- Spring Security with Thymeleaf templates
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

### Running with a specific Spring profile
```bash
./gradlew bootRun --args='--spring.profiles.active=localh2'
```
Set `H2_DB_PATH` environment variable when using localh2 profile (e.g., `./data/testdb`)

For local testing with Supabase:
```bash
./gradlew bootRun --args='--spring.profiles.active=localsupabase'
```

### Creating a JAR file
```bash
./gradlew bootJar
```
Output: `build/libs/tenxdevs-{version}.jar` (e.g., `tenxdevs-0.0.7-SNAPSHOT.jar`)

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
  - Example: `Experiment` entity with auto-generated ID
- **Repositories** (`repository/`): Spring Data JPA repositories
  - Extend `JpaRepository<Entity, ID>` for CRUD operations
  - No custom query methods needed currently
- **Tests** (`src/test/java/eu/robm15/tenxdevs/`):
  - Controller tests use `@SpringBootTest` and `@AutoConfigureMockMvc`
  - MockMvc is used for testing REST endpoints
  - Test pattern: `mockMvc.perform(get(...)).andExpect(...)`

## Environment Configuration

The application uses Spring profiles for environment-specific configuration:

- **localh2** (`application-localh2.yaml`): H2 file-based database for local development
  - Database file path: `${H2_DB_PATH}` (e.g., `./data/testdb`)
  - H2 console enabled at `/h2-console` for debugging
  - Hibernate `ddl-auto: update` for automatic schema updates
- **localsupabase** (`application-localsupabase.yaml`): Local PostgreSQL connection for testing with Supabase
  - Connects to localhost:5432 with hardcoded credentials (for local testing only)
  - SQL logging enabled (`show-sql: true`)
  - Uses `PhysicalNamingStrategyStandardImpl` to preserve exact table/column names
- **develop** (`application-develop.yaml`): PostgreSQL (Supabase) for development environment
  - Credentials provided via environment variables: `SUPABASE_DB_URL_DEVELOP`, `SUPABASE_DB_USERNAME_DEVELOP`, `SUPABASE_DB_PASSWORD_DEVELOP`
  - Hibernate `ddl-auto: update` for automatic schema updates
  - SQL logging enabled (`show-sql: true`)
  - Uses `PhysicalNamingStrategyStandardImpl` to preserve exact table/column names
- **prod** (`application-prod.yaml`): PostgreSQL (Supabase) for production
  - Credentials provided via environment variables: `SUPABASE_DB_URL_PROD`, `SUPABASE_DB_USERNAME_PROD`, `SUPABASE_DB_PASSWORD_PROD`

## CI/CD Pipeline

The project uses GitHub Actions with four workflows:

### Pull Request Testing (`.github/workflows/test-pr.yml`)
- **Trigger**: On pull requests to `develop` or `main` branches
- **Purpose**: Validates code changes before merging
- **Build environment**: Ubuntu with JDK 21 (Temurin distribution)
- **Jobs** (sequential):
  1. **build**: Compiles project with `./gradlew build`, uploads JAR as artifact (7-day retention)
  2. **test**: Runs `./gradlew test` to verify all tests pass
- No deployment or release steps - only validation

### Prepare Release (`.github/workflows/prepare-release.yml`)
- **Trigger**: Manual workflow dispatch
- **Purpose**: Creates a release PR from `develop` to `main`
- **Input**: Optional release version (e.g., `1.0.0`). If not provided, removes `-SNAPSHOT` from current version
- **Pipeline jobs**:
  1. **create-release-pr**: Creates release branch, updates version in `build.gradle.kts`, creates PR to `main`
     - Validates version format (must be `X.Y.Z`)
     - Creates branch `release/vX.Y.Z`
     - Removes `-SNAPSHOT` suffix from version
     - Opens PR with release checklist
- After the PR is merged to `main`, the production deployment workflow triggers automatically

### Development Deployment (`.github/workflows/deploy-develop.yml`)
- **Trigger**: On pull request closure to `develop` branch (only runs if PR is merged)
- **Build environment**: Ubuntu with JDK 21 (Temurin distribution)
- **Pipeline jobs** (sequential):
  1. **build**: Compiles project with `./gradlew build`, extracts version, uploads JAR as artifact
  2. **test**: Runs `./gradlew test` to verify all tests pass
  3. **release**: Creates/updates GitHub Release with JAR artifact as pre-release
     - Deletes existing tag/release if present to enable re-deployment
     - Tags format: `v{version}` (e.g., `v0.0.1-SNAPSHOT`)
  4. **package**: Downloads JAR artifact, builds Docker image, pushes to GHCR
     - Adds custom labels: jar-version, git-commit, git-branch, build-date
  5. **deploy**: Pulls Docker image and deploys to VPS via SSH
- **Docker registry**: GitHub Container Registry (`ghcr.io`)
- **Deployment**: Automatically deploys container to VPS
  - Container name: `tenxdevs_DEV`
  - Port mapping: VPS_PORT_NUMBER_DEV:8080
  - Spring profile: `develop`
  - Supabase credentials passed via environment variables
- **Note**: Merged branches are automatically deleted by GitHub repository setting

### Production Deployment (`.github/workflows/deploy-prod.yml`)
- **Trigger**: On pull request closure to `main` branch (only runs if PR is merged)
- **Build environment**: Ubuntu with JDK 21 (Temurin distribution)
- **Pipeline jobs** (sequential):
  1. **build**: Compiles project, extracts version (strips `-SNAPSHOT`), uploads JAR artifact
  2. **test**: Runs `./gradlew test` to verify all tests pass
  3. **release**: Creates GitHub Release (not pre-release) with JAR artifact
     - Tags format: `vX.Y.Z` (without `-SNAPSHOT`)
     - Marked as latest release
  4. **package**: Builds and pushes Docker image to GHCR with production version tag
  5. **deploy**: Deploys to production VPS
     - Container name: `tenxdevs_PROD`
     - Port mapping: VPS_PORT_NUMBER_PROD:8080
     - Spring profile: `prod`
  6. **bump-version**: After successful deployment
     - Merges `main` into `develop`
     - Increments patch version and adds `-SNAPSHOT`
     - Creates PR to `develop` with version bump (e.g., `0.0.5-SNAPSHOT` → `0.0.6-SNAPSHOT`)

### Release Process Flow

Standard workflow for releases:
1. Develop features on feature branches → merge to `develop` → auto-deploy to DEV environment
2. When ready for production: trigger **Prepare Release** workflow (manual)
3. Review and merge the release PR to `main`
4. **Production Deployment** triggers automatically
5. After deployment, version is auto-bumped on `develop` for next iteration

### Required GitHub Secrets

The following secrets must be configured in GitHub repository settings:
- `SSH_PRIVATE_KEY`: SSH key for VPS deployment
- `VPS_HOST_NAME`: VPS hostname
- `VPS_PORT_NUMBER`: SSH port for VPS
- `VPS_PORT_NUMBER_DEV`: Application port for development environment
- `VPS_PORT_NUMBER_PROD`: Application port for production environment
- `VPS_USER_NAME`: SSH username for VPS
- `SUPABASE_DB_URL_DEVELOP`: Supabase PostgreSQL connection URL for develop
- `SUPABASE_DB_USERNAME_DEVELOP`: Supabase database username for develop
- `SUPABASE_DB_PASSWORD_DEVELOP`: Supabase database password for develop
- `SUPABASE_DB_URL_PROD`: Supabase PostgreSQL connection URL for production
- `SUPABASE_DB_USERNAME_PROD`: Supabase database username for production
- `SUPABASE_DB_PASSWORD_PROD`: Supabase database password for production

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

### Running with environment variables (for develop/prod profiles)
```bash
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=develop \
  -e SUPABASE_DB_URL_DEVELOP="jdbc:postgresql://..." \
  -e SUPABASE_DB_USERNAME_DEVELOP="username" \
  -e SUPABASE_DB_PASSWORD_DEVELOP="password" \
  --name tenxdevs_DEV tenxdevs
```

The Dockerfile:
- Base image: `eclipse-temurin:21-jre-jammy`
- JVM settings: MaxRAMPercentage=75.0 for container memory optimization
- Expects JAR in `build/libs/` directory

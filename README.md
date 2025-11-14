# VibeTravels

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-TBD-blue.svg)](LICENSE)

An AI-powered travel planning application that transforms free-form notes into structured, personalized trip itineraries.

## Table of Contents

- [Project Description](#project-description)
- [Tech Stack](#tech-stack)
- [Getting Started Locally](#getting-started-locally)
- [Available Scripts](#available-scripts)
- [Project Scope](#project-scope)
- [Project Status](#project-status)
- [License](#license)

## Project Description

VibeTravels is a Minimum Viable Product (MVP) application designed to simplify the travel planning process by leveraging artificial intelligence. The application addresses common challenges travelers face:

- **Limited destination knowledge**: Difficulty discovering interesting, lesser-known attractions
- **Time-consuming research**: Overwhelming process of searching and selecting places to visit
- **Complex logistics**: Challenges organizing transportation and accommodation

### Key Features

- **User Account System**: Simple registration and login functionality with secure JWT authentication
- **Note Management (CRUD)**: Create, read, update, and delete travel notes in free-form text
- **User Profile & Preferences**: Manage seven categories of travel preferences:
  - Budget
  - Travel pace
  - Interests
  - Accommodation style
  - Transportation type
  - Food preferences
  - Travel season
- **AI Travel Planner**: Generate personalized trip itineraries based on notes and preferences
- **Plan Rating System**: Rate generated plans on a 1-5 scale to provide feedback
- **Multi-Environment Support**: Local development (H2), staging (Supabase), and production configurations

## Tech Stack

### Backend
- **Java 21** (Eclipse Temurin toolchain)
- **Spring Boot 3.5.7**
  - Spring Boot Starter Web (REST APIs)
  - Spring Data JPA (Data persistence)
  - Spring Security (Authentication & authorization)
  - Thymeleaf (Template engine)

### Database
- **PostgreSQL** via Supabase (development & production)
- **H2** (local development)

### Authentication
- **JWT** with Supabase integration
- JJWT library (0.12.6) for token validation

### Build & Testing
- **Gradle** with Kotlin DSL
- **JUnit 5** (Testing framework)

### DevOps & Deployment
- **Docker** (Containerization)
- **GitHub Actions** (CI/CD pipeline)
- **GitHub Container Registry** (Docker image hosting)
- **VPS Deployment** (SSH-based deployment)

## Getting Started Locally

### Prerequisites

- Java 21 (Temurin distribution recommended)
- Gradle (included via wrapper)
- Docker (optional, for containerization)

### Clone the Repository

```bash
git clone https://github.com/RobertMarcinkowski/tenxdevs.git
cd tenxdevs
```

### Choose Your Environment Profile

The application supports multiple Spring profiles for different environments:

#### Option 1: Local Development with H2 (No Authentication)

This is the quickest way to get started. Uses an in-memory H2 database with all authentication disabled.

1. Set the H2 database path:
   ```bash
   export H2_DB_PATH=./data/testdb
   ```
   On Windows:
   ```bash
   set H2_DB_PATH=./data/testdb
   ```

2. Run the application:
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=localh2'
   ```
   On Windows:
   ```bash
   gradlew.bat bootRun --args='--spring.profiles.active=localh2'
   ```

3. Access the application at `http://localhost:8080`
4. Access H2 console at `http://localhost:8080/h2-console`

#### Option 2: Local Development with Supabase

Requires a local PostgreSQL connection configured for Supabase.

```bash
./gradlew bootRun --args='--spring.profiles.active=localsupabase'
```

#### Option 3: Development Environment

Requires Supabase credentials configured as environment variables.

```bash
export SUPABASE_DB_URL_DEVELOP="jdbc:postgresql://..."
export SUPABASE_DB_USERNAME_DEVELOP="username"
export SUPABASE_DB_PASSWORD_DEVELOP="password"
export SUPABASE_URL_DEVELOP="https://xxx.supabase.co"
export SUPABASE_ANON_KEY_DEVELOP="your-anon-key"
export SUPABASE_JWT_SECRET_DEVELOP="your-jwt-secret"

./gradlew bootRun --args='--spring.profiles.active=develop'
```

#### Option 4: Production Environment

Similar to development, but uses production Supabase credentials (see `CLAUDE.md` for details).

## Available Scripts

### Build the Project

```bash
./gradlew build
```

This compiles the code, runs tests, and creates a JAR file.

### Run Tests

Run all tests:
```bash
./gradlew test
```

Run a specific test class:
```bash
./gradlew test --tests "eu.robm15.tenxdevs.controller.TenxdevsControllerTest"
```

Run a specific test method:
```bash
./gradlew test --tests "eu.robm15.tenxdevs.controller.TenxdevsControllerTest.tenxdevsDefault"
```

### Run the Application

Default profile:
```bash
./gradlew bootRun
```

With specific profile:
```bash
./gradlew bootRun --args='--spring.profiles.active=localh2'
```

### Create JAR File

```bash
./gradlew bootJar
```

Output location: `build/libs/tenxdevs-{version}.jar`

### Docker

Build Docker image:
```bash
./gradlew build
docker build -t tenxdevs .
```

Run Docker container:
```bash
docker run -d -p 8080:8080 tenxdevs
```

Run with environment variables:
```bash
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=develop \
  -e SUPABASE_DB_URL_DEVELOP="jdbc:postgresql://..." \
  -e SUPABASE_DB_USERNAME_DEVELOP="username" \
  -e SUPABASE_DB_PASSWORD_DEVELOP="password" \
  -e SUPABASE_URL_DEVELOP="https://xxx.supabase.co" \
  -e SUPABASE_ANON_KEY_DEVELOP="your-anon-key" \
  -e SUPABASE_JWT_SECRET_DEVELOP="your-jwt-secret" \
  --name tenxdevs_DEV tenxdevs
```

## Project Scope

### In Scope (MVP Features)

✅ **User Account System**
- User registration and login
- Unique accounts with associated data (notes, preferences)

✅ **Note Management (CRUD)**
- Create, read, update, and delete travel notes
- Free-form, unstructured text format

✅ **User Profile & Preferences**
- Dedicated profile page for managing preferences
- Seven preference categories: budget, travel pace, interests, accommodation style, transportation, food, season
- Update preferences at any time

✅ **AI Travel Plan Generator**
- Generate trip plans based on notes and user preferences
- Simple list format with attractions and activities per day
- Swappable AI provider via API key
- Usage limits per user to control costs

✅ **Plan Rating System**
- Rate generated plans on a 1-5 scale
- Provide feedback on plan quality

✅ **Data Persistence**
- Store all data (accounts, notes, preferences, ratings) in database

### Out of Scope (Not in MVP)

❌ **Plan Sharing**
- Sharing travel plans and notes between user accounts

❌ **Multimedia Support**
- Photo, video, or map analysis in notes or plans

❌ **Advanced Logistics Planning**
- Transportation or accommodation booking
- Detailed time scheduling
- Route optimization

## Project Status

**Current Version:** 0.1.2-SNAPSHOT

**Development Stage:** MVP (Minimum Viable Product)

### Success Metrics

The MVP success will be measured by:

- **Profile Adoption**: 90% of registered users have at least 3 out of 7 preference categories filled
- **User Engagement**: 75% of active users generate 3 or more trip plans per year
- **Plan Quality**: Average rating of generated plans maintains 4.0 or higher (on 1-5 scale)

### CI/CD Pipeline

The project uses GitHub Actions with four workflows:

- **Pull Request Testing**: Validates code changes before merging
- **Prepare Release**: Creates release PRs from develop to main
- **Development Deployment**: Automatically deploys to development VPS on merge to `develop`
- **Production Deployment**: Automatically deploys to production VPS on merge to `main`

For detailed workflow information, see [CLAUDE.md](CLAUDE.md).

### Development Roadmap

This is an MVP version. Future enhancements may include:
- Enhanced AI capabilities for more detailed itineraries
- Integration with third-party booking services
- Collaborative trip planning features
- Mobile application support

## License

To be determined.

---

**Project developed as part of the 10xDevs training certificate program.**

For detailed technical documentation, build instructions, and architecture details, see [CLAUDE.md](CLAUDE.md).

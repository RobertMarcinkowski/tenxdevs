# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**VibeTravels** is an AI-powered travel planning application that transforms free-form notes into structured, personalized trip itineraries. Built as part of the 10xDevs training certificate program.

### Tech Stack
- **Java 21** (Temurin toolchain)
- **Spring Boot 3.5.7** with:
  - Spring Boot Starter Web (REST APIs)
  - Spring Data JPA (PostgreSQL/Supabase and H2)
  - Spring Security (JWT authentication)
  - Spring AI (OpenAI integration)
  - Thymeleaf (template engine)
- **Gradle** with Kotlin DSL (build.gradle.kts)
- **JUnit 5** for testing
- **JJWT 0.12.6** for JWT token validation
- **PostgreSQL** (Supabase) for production/development, **H2** for local testing

Base package: `eu.robm15.tenxdevs`

### Core Features (MVP)
- **User Authentication**: Supabase JWT-based authentication with registration/login
- **Note Management (CRUD)**: Create, read, update, delete travel notes
- **Travel Preferences**: Seven categories (budget, pace, interests, accommodation, transport, food, season)
- **AI Trip Planning**: Generate personalized itineraries using Spring AI with OpenAI
- **Trip Plan Management**: Save, view, rate (1-5 scale), and delete generated trip plans
- **AI Usage Limits**: Daily limit (default 10 plans/day) to control costs and prevent abuse
- **Password Reset**: Email-based password recovery via Supabase

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

- **Controllers** (`controller/`): REST endpoints and view controllers
  - `AuthController`: Authentication API (`/api/auth/me`, `/api/auth/status`, `/api/auth/reset-password-request`, `/api/auth/update-password`)
  - `NoteController`: Note CRUD operations (`/api/notes/*`) - requires JWT authentication
  - `PreferencesController`: Travel preferences management (`/api/preferences/*`) - requires JWT authentication
  - `TripPlanController`: Trip plan generation, retrieval, rating, and deletion (`/api/trip-plans/*`) - requires JWT authentication
  - `ConfigController`: Configuration endpoints (`/api/config/*`) - public
  - `ViewController`: Thymeleaf templates (landing, login, register, app, profile, forgot-password, reset-password)
  - `DiagnosticsController`: System diagnostics endpoints
  - `MockAuthController`: Mock authentication for localh2 profile
  - Controllers inject services via `@Autowired`, extract user ID from JWT tokens

- **Models** (`model/`): JPA entities with Jakarta Persistence annotations
  - `Note`: Travel notes with userId, title, content, timestamps
  - `TravelPreferences`: User preferences with userId and seven preference categories
  - `TripPlan`: Generated trip plans with userId, noteId, planContent (TEXT), rating (1-5), createdAt
  - Enums: `Budget`, `Pace`, `Interest`, `AccommodationStyle`, `Transport`, `FoodPreference`, `Season`
  - Multi-value preferences (interests, transport, food) use `@ElementCollection` with separate join tables

- **Repositories** (`repository/`): Spring Data JPA repositories
  - Extend `JpaRepository<Entity, ID>` for CRUD operations
  - `NoteRepository`: Custom query `findByUserId(String userId)`
  - `TravelPreferencesRepository`: Custom queries for `findByUserId`, `existsByUserId`, `deleteByUserId`
  - `TripPlanRepository`: Custom queries for `findByUserId`, `findByNoteId`, `findByUserIdAndNoteId`, `countByUserIdAndCreatedAtAfter`

- **Services** (`service/`): Business logic layer
  - `SupabaseJwtService`: JWT token validation using Supabase JWT secret, extracts user claims
  - `SupabaseAuthService`: Password reset functionality via Supabase Admin API
  - `NoteService`: Note CRUD operations with user ownership validation
  - `TravelPreferencesService`: Preferences management with upsert logic
  - `TripPlanService`: AI trip plan generation, validation (minimum 3 preferences required), rating, deletion
  - `AIUsageLimitService`: Tracks and enforces daily AI usage limits (configurable, default 10/day)

- **Security** (`security/`): Authentication filters and configuration
  - `JwtAuthenticationFilter`: OncePerRequestFilter that extracts JWT from Authorization header, validates, sets Spring Security context
  - Stateless session management (no HTTP sessions)

- **Config** (`config/`): Spring configuration classes
  - `SecurityConfig`: Two security configurations based on profile:
    - `LocalH2SecurityConfig` (@Profile("localh2")): Disables all authentication
    - `JwtSecurityConfig` (@Profile("!localh2")): Configures JWT filter chain
  - `SupabaseConfigProperties`: Binds Supabase configuration (url, anon-key, jwt-secret)
  - `AiConfigProperties`: Binds AI configuration (api-key, model, temperature)
  - `GlobalControllerAdvice`: Global exception handling for controllers

- **Tests** (`src/test/java/eu/robm15/tenxdevs/`):
  - Controller tests use `@SpringBootTest` and `@AutoConfigureMockMvc`
  - MockMvc pattern: `mockMvc.perform(get(...)).andExpect(...)`
  - `JwtTestUtil`: Utility for generating test JWT tokens

## Authentication

The application uses Supabase JWT-based authentication with Spring Security:

### Architecture
- **Stateless authentication**: No server-side sessions, JWT tokens carry all authentication state
- **JWT validation**: Tokens are validated using Supabase JWT secret (HMAC-SHA256)
- **Filter chain**: `JwtAuthenticationFilter` intercepts requests before controller layer
- **Token format**: Expects `Authorization: Bearer <token>` header

### Key Components
- `JwtAuthenticationFilter` (`security/`): OncePerRequestFilter that extracts JWT from Authorization header, validates it, and sets Spring Security context
- `SupabaseJwtService` (`service/`): Validates JWT tokens using Supabase JWT secret, extracts user claims (sub, email, role)
- `SecurityConfig` (`config/`): Configures security filter chain with public/protected endpoints

### Endpoint Security
**Public endpoints (no authentication required):**
- `/`, `/landing`, `/login`, `/register`, `/app`, `/profile` - Thymeleaf views
- `/forgot-password`, `/reset-password` - Password reset views
- `/api/config/**` - Configuration endpoints
- `/api/auth/status` - Auth API health check
- `/api/auth/reset-password-request`, `/api/auth/update-password` - Password reset endpoints
- `/api/status`, `/tenxdevs` - Public API endpoints
- `/h2-console/**` - H2 database console (local profiles only)

**Protected endpoints (JWT required):**
- `/api/auth/me` - Get current user information
- `/api/notes/**` - Note CRUD operations (GET, POST, PUT, DELETE)
- `/api/preferences/**` - Travel preferences management
- `/api/trip-plans/**` - Trip plan generation, retrieval, rating, deletion
- `/api/protected/**` - Protected API endpoints
- `/tenxdevs-ask-ai` - AI-powered trip planning endpoint
- All other endpoints default to authenticated

### Testing Authentication
To test protected endpoints, include JWT token in requests:
```bash
curl -H "Authorization: Bearer <supabase-jwt-token>" http://localhost:8080/api/auth/me
```

### Local Development without Authentication
The **localh2** profile disables all authentication for convenience during local development:
- No Supabase configuration required
- No JWT tokens needed
- All endpoints (including `/api/auth/me` and `/api/protected/**`) are publicly accessible
- Useful for quick prototyping and testing without setting up authentication infrastructure

### User ID Extraction Pattern
Controllers extract user ID from JWT tokens using a common pattern:
```java
private String extractUserId(HttpServletRequest request) {
    // For localh2 profile (mock auth), return a default user ID
    if (jwtService == null) {
        return "mock-user-id";
    }

    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        return jwtService.extractSubject(token);
    }
    return null;
}
```

## AI Integration (Spring AI)

The application uses **Spring AI 1.1.0** with OpenAI for generating personalized trip itineraries.

### Configuration
- **Dependency**: Spring AI BOM manages version, `spring-ai-starter-model-openai` for OpenAI integration
- **API Key**: Configured via `AI_API_KEY` environment variable (supports OpenAI-compatible APIs)
- **Model**: Configurable via `AI_MODEL` (e.g., "gpt-4o-mini", "gpt-4")
- **Temperature**: Configurable via `AI_TEMPERATURE` (0.0-1.0, controls response creativity)
- **Usage Limit**: Configurable via `ai.usage.limit.daily` (default: 10 plans per day per user)

### Implementation Pattern
The AI integration follows Spring AI's OpenAiChatModel pattern:
1. `TripPlanService` validates user has minimum 3 preferences filled
2. Service checks daily AI usage limit via `AIUsageLimitService`
3. Builds comprehensive prompt from note content and user preferences
4. Calls `openAiChatModel.call(prompt)` to generate trip plan
5. Saves generated plan to database with userId, noteId, and timestamp
6. Supports fallback mock generation for localh2 profile without AI configured

### Trip Plan Generation Requirements
- **Minimum preferences**: User must have at least 3 out of 7 preference categories filled
- **Daily limit**: Configurable limit prevents excessive AI usage (default 10 plans/day)
- **Ownership validation**: Plans are linked to both user and note, with ownership checks on all operations

### Endpoints
- **POST `/api/trip-plans/generate`**: Generate AI trip plan for a note (requires min 3 preferences)
- **GET `/api/trip-plans/can-generate?noteId=X`**: Check if user can generate plan (validates preferences and limits)
- **GET `/api/trip-plans?noteId=X`**: Get all plans for a note
- **GET `/api/trip-plans/{id}`**: Get specific plan by ID
- **PUT `/api/trip-plans/{id}/rate`**: Rate a plan (1-5 scale)
- **DELETE `/api/trip-plans/{id}`**: Delete a plan
- **POST `/tenxdevs-ask-ai`**: Legacy AI endpoint (if still in use)

## Environment Configuration

The application uses Spring profiles for environment-specific configuration:

- **localh2** (`application-localh2.yaml`): H2 file-based database for local development **WITHOUT authentication**
  - Database file path: `${H2_DB_PATH}` (e.g., `./data/testdb`)
  - H2 console enabled at `/h2-console` for debugging
  - Hibernate `ddl-auto: update` for automatic schema updates
  - **No Supabase required** - all endpoints accessible without authentication
  - Security components (JWT filter, Supabase services) are disabled for this profile
  - AI configuration: Uses environment variables `AI_API_KEY`, `AI_MODEL`, `AI_TEMPERATURE` (optional for local dev)
  - AI usage limit: `ai.usage.limit.daily` (default 10 if not specified)

- **localsupabase** (`application-localsupabase.yaml`): Local PostgreSQL connection for testing with Supabase
  - Connects to localhost:5432 with hardcoded credentials (for local testing only)
  - SQL logging enabled (`show-sql: true`)
  - Uses `PhysicalNamingStrategyStandardImpl` to preserve exact table/column names
  - AI configuration: Uses environment variables `AI_API_KEY`, `AI_MODEL`, `AI_TEMPERATURE`
  - AI usage limit: `ai.usage.limit.daily` (default 10 if not specified)

- **develop** (`application-develop.yaml`): PostgreSQL (Supabase) for development environment
  - **Database credentials**: `SUPABASE_DB_URL_DEVELOP`, `SUPABASE_DB_USERNAME_DEVELOP`, `SUPABASE_DB_PASSWORD_DEVELOP`
  - **Supabase authentication**: `SUPABASE_URL_DEVELOP`, `SUPABASE_ANON_KEY_DEVELOP`, `SUPABASE_JWT_SECRET_DEVELOP`
  - **AI configuration**: `AI_API_KEY`, `AI_MODEL`, `AI_TEMPERATURE`
  - **AI usage limit**: Configured in YAML (`ai.usage.limit.daily: 10`)
  - Hibernate `ddl-auto: update` for automatic schema updates
  - SQL logging enabled (`show-sql: true`)
  - Uses `PhysicalNamingStrategyStandardImpl` to preserve exact table/column names

- **prod** (`application-prod.yaml`): PostgreSQL (Supabase) for production
  - **Database credentials**: `SUPABASE_DB_URL_PROD`, `SUPABASE_DB_USERNAME_PROD`, `SUPABASE_DB_PASSWORD_PROD`
  - **Supabase authentication**: `SUPABASE_URL_PROD`, `SUPABASE_ANON_KEY_PROD`, `SUPABASE_JWT_SECRET_PROD`
  - **AI configuration**: `AI_API_KEY`, `AI_MODEL`, `AI_TEMPERATURE`
  - **AI usage limit**: Configured in YAML (`ai.usage.limit.daily: 10`)

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

**VPS Deployment:**
- `SSH_PRIVATE_KEY`: SSH key for VPS deployment
- `VPS_HOST_NAME`: VPS hostname
- `VPS_PORT_NUMBER`: SSH port for VPS
- `VPS_PORT_NUMBER_DEV`: Application port for development environment
- `VPS_PORT_NUMBER_PROD`: Application port for production environment
- `VPS_USER_NAME`: SSH username for VPS

**Supabase Database (Develop):**
- `SUPABASE_DB_URL_DEVELOP`: PostgreSQL connection URL for develop
- `SUPABASE_DB_USERNAME_DEVELOP`: Database username for develop
- `SUPABASE_DB_PASSWORD_DEVELOP`: Database password for develop

**Supabase Database (Production):**
- `SUPABASE_DB_URL_PROD`: PostgreSQL connection URL for production
- `SUPABASE_DB_USERNAME_PROD`: Database username for production
- `SUPABASE_DB_PASSWORD_PROD`: Database password for production

**Supabase Authentication (Develop):**
- `SUPABASE_URL_DEVELOP`: Supabase project URL for develop
- `SUPABASE_ANON_KEY_DEVELOP`: Supabase anonymous key for develop
- `SUPABASE_JWT_SECRET_DEVELOP`: JWT secret for token validation in develop

**Supabase Authentication (Production):**
- `SUPABASE_URL_PROD`: Supabase project URL for production
- `SUPABASE_ANON_KEY_PROD`: Supabase anonymous key for production
- `SUPABASE_JWT_SECRET_PROD`: JWT secret for token validation in production

**AI Configuration (Both Environments):**
- `AI_API_KEY`: OpenAI API key (or compatible API key)
- `AI_MODEL`: Model name (e.g., "gpt-4o-mini", "gpt-4")
- `AI_TEMPERATURE`: Temperature setting (e.g., "0.7")

## Data Model Architecture

### User Data Ownership
All user-generated data is linked to Supabase user IDs (UUID strings):
- **Notes**: `userId` column links notes to users (one-to-many relationship)
- **TravelPreferences**: `userId` column with UNIQUE constraint (one-to-one relationship)
- **TripPlans**: `userId` and `noteId` columns link plans to both users and notes (many-to-one with users, many-to-one with notes)
- **User ID source**: Extracted from JWT token's `sub` claim in authenticated requests

### Multi-Value Preferences
Three preference categories allow multiple selections using `@ElementCollection`:
- **interests**: Stored in `travel_preferences_interests` join table
- **transport**: Stored in `travel_preferences_transport` join table
- **foodPreferences**: Stored in `travel_preferences_food` join table

### Enum Management
Seven preference categories are defined as Java enums with `getDisplayName()` methods:
- `Budget`: BUDGET_CONSCIOUS, MODERATE, LUXURY
- `Pace`: RELAXED, MODERATE, FAST_PACED
- `Interest`: CULTURE, NATURE, ADVENTURE, FOOD, NIGHTLIFE, SHOPPING, HISTORY
- `AccommodationStyle`: HOTEL, HOSTEL, APARTMENT, RESORT
- `Transport`: CAR, PUBLIC_TRANSPORT, BICYCLE, WALKING, PLANE
- `FoodPreference`: LOCAL_CUISINE, VEGETARIAN, VEGAN, STREET_FOOD, FINE_DINING
- `Season`: SPRING, SUMMER, AUTUMN, WINTER, ANY

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
  -e SUPABASE_URL_DEVELOP="https://xxx.supabase.co" \
  -e SUPABASE_ANON_KEY_DEVELOP="your-anon-key" \
  -e SUPABASE_JWT_SECRET_DEVELOP="your-jwt-secret" \
  --name tenxdevs_DEV tenxdevs
```

The Dockerfile:
- Base image: `eclipse-temurin:21-jre-jammy`
- JVM settings: MaxRAMPercentage=75.0 for container memory optimization
- Expects JAR in `build/libs/` directory

## API Endpoints Reference

### Notes API (`/api/notes/*`)
All endpoints require JWT authentication (except in localh2 profile).

- **GET `/api/notes`**: Get all notes for authenticated user
  - Returns: `List<Note>`

- **GET `/api/notes/{id}`**: Get specific note by ID
  - Validates note ownership
  - Returns: `Note` or 404

- **POST `/api/notes`**: Create new note
  - Body: `{"title": "string", "content": "string"}`
  - Validates: title and content are required and non-empty
  - Returns: `{"success": true, "message": "...", "note": {...}}`

- **PUT `/api/notes/{id}`**: Update existing note
  - Body: `{"title": "string", "content": "string"}`
  - Validates: note ownership, title and content required
  - Returns: `{"success": true, "message": "...", "note": {...}}` or 404

- **DELETE `/api/notes/{id}`**: Delete note
  - Validates: note ownership
  - Returns: `{"success": true, "message": "..."}` or 404

### Preferences API (`/api/preferences/*`)
All endpoints require JWT authentication (except in localh2 profile).

- **GET `/api/preferences/options`**: Get all available enum options
  - Public endpoint
  - Returns: Map of preference categories to enum options with labels

- **GET `/api/preferences`**: Get current user's preferences
  - Returns: `TravelPreferences` or 404

- **POST `/api/preferences`**: Save or update preferences (upsert)
  - Body: `TravelPreferences` object with any combination of preferences
  - Updates existing if found, creates new otherwise
  - Returns: `{"success": true, "message": "...", "preferences": {...}}`

- **DELETE `/api/preferences`**: Delete user's preferences
  - Returns: `{"success": true, "message": "..."}`

### Authentication API (`/api/auth/*`)

- **GET `/api/auth/status`**: Public health check endpoint
- **GET `/api/auth/me`**: Get current user info from JWT (requires authentication)
- **POST `/api/auth/reset-password-request`**: Request password reset email (public)
- **POST `/api/auth/update-password`**: Update password with reset token (public)

### Trip Plans API (`/api/trip-plans/*`)
All endpoints require JWT authentication (except in localh2 profile).

- **GET `/api/trip-plans/can-generate?noteId={id}`**: Check if user can generate plan for a note
  - Validates: note exists and belongs to user, user has minimum 3 preferences, AI limit not exceeded
  - Returns: `{"can_generate": true/false, "reason": "...", "remaining_usage": X, "daily_limit": Y}`

- **POST `/api/trip-plans/generate`**: Generate AI trip plan for a note
  - Body: `{"noteId": Long}`
  - Validates: note ownership, minimum 3 preferences, AI usage limit
  - Calls OpenAI to generate personalized plan based on note and preferences
  - Returns: `{"success": true, "message": "...", "trip_plan": {...}, "remaining_usage": X}`

- **GET `/api/trip-plans?noteId={id}`**: Get all trip plans for a specific note
  - Validates: note ownership
  - Returns: `List<TripPlan>`

- **GET `/api/trip-plans/{id}`**: Get specific trip plan by ID
  - Validates: plan ownership
  - Returns: `TripPlan` or 403/404

- **PUT `/api/trip-plans/{id}/rate`**: Rate a trip plan
  - Body: `{"rating": Integer}` (1-5)
  - Validates: plan ownership, rating is between 1-5
  - Returns: `{"success": true, "message": "...", "trip_plan": {...}}`

- **DELETE `/api/trip-plans/{id}`**: Delete a trip plan
  - Validates: plan ownership
  - Returns: `{"success": true, "message": "..."}`

## Development Notes

### Common Patterns

**Controller Response Format:**
- Success: `{"success": true, "message": "...", "data": {...}}`
- Error: `{"success": false, "message": "..."}`
- HTTP status codes: 200 (OK), 201 (Created), 400 (Bad Request), 401 (Unauthorized), 404 (Not Found), 500 (Internal Server Error)

**Service Layer Transaction Management:**
- Use `@Transactional` on service methods that modify data
- Repositories handle basic CRUD, services implement business logic
- Services validate ownership before operations

**Testing Protected Endpoints:**
- Use `JwtTestUtil` to generate test tokens
- Mock user IDs in tests match "mock-user-id" for localh2 profile
- Integration tests use `MockMvc` with `@SpringBootTest`

**AI Usage Limits:**
- Daily limits tracked per user using `countByUserIdAndCreatedAtAfter` repository query
- Limit resets at start of each day (00:00:00 local time)
- Configurable via `ai.usage.limit.daily` property (default 10)
- Mock AI generation available for localh2 profile without requiring OpenAI API key
- Usage count and remaining quota returned in API responses

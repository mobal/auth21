# auth21

Java 21 based implementation of authentication service using Spring Boot and AWS DynamoDB.

## Overview

auth21 is a RESTful authentication service built with modern Java 21 features. It provides token-based authentication with JWT support and refresh token capabilities.

## Features

- **User Authentication** - Login, register, and user management
- **JWT Tokens** - JSON Web Token generation and validation
- **Refresh Tokens** - Token refresh mechanism for extended sessions
- **DynamoDB Integration** - AWS DynamoDB for persistent data storage
- **Correlation IDs** - Request tracking with correlation IDs
- **Exception Handling** - Centralized exception handling with structured error responses

## Technology Stack

- **Java 21**
- **Spring Boot**
- **AWS SDK v2** (DynamoDB Enhanced Client)
- **Gradle**
- **JUnit 5** with Mockito
- **Log4j2**

## Project Structure

```
src/main/java/hu/squarelabs/auth21/
├── Application.java
├── controller/
│   └── AuthController.java
├── service/
│   ├── AuthService.java
│   └── TokenService.java
├── repository/
│   ├── UserRepository.java
│   └── TokenRepository.java
├── model/
│   ├── JwtToken.java
│   ├── entity/
│   │   ├── UserEntity.java
│   │   └── TokenEntity.java
│   └── dto/
│       └── response/
│           └── TokenResponse.java
├── config/
│   ├── DynamoDbConfig.java
│   ├── JacksonConfig.java
│   └── filter/
│       └── CorrelationIdFilter.java
├── converter/
│   └── MapAttributeConverter.java
└── exception/
    └── GlobalExceptionHandler.java
```

## API Endpoints

### Authentication

- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/refresh-token` - Refresh access token

## Building

```bash
./gradlew clean build
```

## Testing

```bash
./gradlew test
```

## Running

### Local Development

```bash
./gradlew bootRun
```

### Docker

Build the Docker image:

```bash
docker build -t auth21:latest .
```

Run the container:

```bash
docker run -d \
  -p 8080:8080 \
  -e AWS_REGION=us-east-1 \
  -e JWT_SECRET=your-secret-key \
  -e JWT_TOKEN_LIFETIME=3600 \
  --name auth21 \
  auth21:latest
```

Using Docker Compose:

```bash
docker-compose up -d
```

## Configuration

### Application Properties

Set the following environment variables or update `application.properties`:

- `aws.region` - AWS region (e.g., us-east-1)
- `jwt.secret` - JWT secret key
- `jwt.token.lifetime` - JWT token lifetime in seconds (default: 3600)

## Development

### Prerequisites

- Java 21 JDK
- Gradle 9.x or higher

### Installing Gradle

Gradle can be obtained from:
- **Official Site**: https://gradle.org/releases/
- **Package Managers**:
  - macOS: `brew install gradle`
  - Ubuntu/Debian: `apt-get install gradle`
  - Windows: `choco install gradle`
- **Gradle Wrapper**: This project includes `./gradlew` wrapper script (recommended)

The project includes a Gradle Wrapper that automatically downloads the correct Gradle version.

### Code Style

The project follows Java best practices including:
- Use of `final` keyword for variable immutability
- Strategic use of `var` keyword for readability
- Comprehensive test coverage


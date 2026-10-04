# Piggy-s-Bank

Piggy-s-Bank is a complete banking microservice application built with Java 17 and Spring Boot. The project demonstrates a distributed microservices architecture for user management, account operations, and financial transactions, with centralized configuration and service discovery.

## Overview

This repository contains multiple Spring Boot services that work together to provide a banking platform:

- User/Auth service
- Account service
- Transaction service
- API Gateway
- Eureka service discovery
- Config server

The application is designed around a modular architecture where each service handles a specific domain responsibility and communicates with the others over HTTP.

---

## Architecture

The system follows a microservice pattern:

- API Gateway: single entry point for all external requests
- Services module: user and authentication management
- Account Service: bank account creation, balance updates, account lookup
- Transaction Service: deposits, withdrawals, transfers, history
- Eureka Server: registry for service discovery
- Config Server: centralizes application configuration

Typical flow:

Client → API Gateway → Service(s) → Other microservices

---

## Project Structure

```text
Piggy-s-Bank/
├── README.md
├── .vscode/
├── account-service/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── api-gateway/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── config-server/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── eureka-server/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── services/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── transaction-service/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
└── .gitignore
```

---

## Services

### 1) services
This module is responsible for user registration, login, and JWT-based authentication.

Key responsibilities:
- Register new users
- Login users
- Secure API endpoints with Spring Security
- Generate and validate JWT tokens
- Manage roles such as CUSTOMER and ADMIN

Main endpoints:
- POST /api/auth/register
- POST /api/auth/login
- POST /api/users
- GET /api/users
- GET /api/users/{id}

---

### 2) account-service
This service handles banking account operations.

Key responsibilities:
- Create user accounts
- Fetch account information by user, ID, or account number
- Close an account
- Validate account status
- Credit and debit balances
- Prevent invalid operations like insufficient balance or inactive account use
- Use Feign clients to call the user service

Main endpoints:
- POST /api/accounts
- GET /api/accounts/user/{userId}
- GET /api/accounts/{id}
- GET /api/accounts/number/{accountNumber}
- PATCH /api/accounts/{id}/close
- GET /api/accounts/user/{userId}/has-active
- PUT /api/accounts/{accountNumber}/debit
- PUT /api/accounts/{accountNumber}/credit
- PUT /api/accounts/{accountNumber}/reverse-debit

---

### 3) transaction-service
This service handles all financial transaction logic.

Key responsibilities:
- Deposit money
- Withdraw money
- Transfer funds between accounts
- Store transaction history
- Retrieve transaction details by transaction number
- Support idempotent request handling
- Coordinate transfer workflows through saga-style orchestration

Main endpoints:
- POST /api/transactions/deposit
- POST /api/transactions/withdraw
- POST /api/transactions/transfer
- GET /api/transactions/history/{accountNumber}
- GET /api/transactions/{transactionNumber}

---

### 4) api-gateway
The API gateway acts as the front door for client requests and routes traffic to the relevant microservice.

Configuration:
- Port: 9000

This service integrates with Eureka and Spring Cloud Config, allowing the gateway to discover downstream services dynamically.

---

### 5) config-server
This module provides centralized configuration management for all microservices.

Configuration:
- Port: 8888

The configuration server is set up to fetch properties from a Git-backed config repository.

---

### 6) eureka-server
This module provides service discovery and registration for all services.

Configuration:
- Port: 8761

This allows microservices to register and discover each other without hardcoded addresses.

---

## Technologies Used

- Java 17
- Spring Boot 3
- Spring Cloud
- Spring Cloud Config
- Spring Cloud Gateway
- Netflix Eureka
- Spring Security
- JWT (JSON Web Token)
- Spring Data JPA
- H2 Database
- OpenFeign
- Maven

---

## Configuration

Each service uses Spring Cloud Config and Eureka. The services are configured to connect to:

- Config Server: http://localhost:8888
- Eureka Server: http://localhost:8761

Service-specific configuration files are stored in the respective modules and are loaded via Spring Cloud Config.

---

## Getting Started

### Prerequisites

- Java 17+
- Maven
- Git

### Run the services in order

1. Start the Config Server
2. Start the Eureka Server
3. Start the user/auth service
4. Start the account service
5. Start the transaction service
6. Start the API Gateway

Example:

```bash
cd config-server
./mvnw spring-boot:run
```

```bash
cd eureka-server
./mvnw spring-boot:run
```

```bash
cd services
./mvnw spring-boot:run
```

```bash
cd account-service
./mvnw spring-boot:run
```

```bash
cd transaction-service
./mvnw spring-boot:run
```

```bash
cd api-gateway
./mvnw spring-boot:run
```

---

## Example API Usage

### Register user
```http
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "1234567890",
  "password": "secret123"
}
```

### Login user
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "secret123"
}
```

### Create account
```http
POST /api/accounts
Content-Type: application/json

{
  "userId": 1,
  "accountType": "SAVINGS",
  "initialDeposit": 1000.00
}
```

### Deposit funds
```http
POST /api/transactions/deposit
Content-Type: application/json

{
  "toAccountNumber": "1234567890",
  "amount": 500.00,
  "description": "Initial deposit"
}
```

---

## Key Design Ideas

- Microservice separation by business capability
- Centralized configuration
- Service registration and discovery
- JWT-based security
- Validation at the API layer
- Business rules for account operations
- Transaction logging and history
- Idempotency support for financial requests

---

## Notes

This project is a solid demo/learning project for building a banking microservice platform with Spring Boot and Spring Cloud. It includes core capabilities expected in a banking domain application, but it is not a production-grade financial platform and should be extended further with:
- Docker/Kubernetes deployment
- CI/CD pipelines
- Better observability
- Database persistence strategies
- API versioning
- Rate limiting and throttling
- Advanced monitoring and tracing

---

## License

This project does not currently declare a license in the repository.

---

## Conclusion

Piggy-s-Bank is a multi-service banking application that demonstrates how to build a complete microservices-based financial system using Java and Spring Boot. It covers authentication, user management, account creation, transaction processing, service discovery, and centralized configuration.

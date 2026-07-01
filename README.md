# My Finance - Microservices Project

A modern, robust financial microservices system built using **Java 21**, **Spring Boot 4.x**, **Spring Cloud (2025.x)**, **Docker**, and modern security/database standards.

---

## 📌 Project Overview

This repository implements a backend service architecture designed for financial/customer management. It leverages Spring Cloud to manage service discovery and centralized configuration, alongside containerized PostgreSQL and MongoDB databases.

---

## 🏗️ Architecture & Component Services

The system is split into five primary microservices, a frontend application, and a Dockerized infrastructure layer:

```mermaid
graph TD
    UI[Finance UI: 4200] --> GW[Gateway: 8080]
    CS[Config Server: 8888] --> DS[Discovery Service: 8761]
    CS --> GW
    CS --> AS[Auth Service: 8082]
    CS --> CUST[Customer Service: 8090]

    GW -->|/auth/**| AS
    GW -->|/customers/**| CUST

    AS -->|Register/Validate| DB_PG[(PostgreSQL: 5444)]
    CUST -->|Customer CRUD| DB_MONGO[(MongoDB: 27017)]
```

### 1. Centralized Configuration Server (`config-server`)

- **Port:** `8888`
- **Technology:** Spring Cloud Config Server
- **Purpose:** Acts as a centralized configuration repository for all microservices using the `native` profile. Configuration files are stored under its resources (`classpath:/configurations`) and served dynamically.
- **Key Config Files Managed:**
  - `application.yml` (Common configurations, Eureka zones)
  - `auth-service.yml` (PostgreSQL connection strings, JPA setup, JWT secret & expiration)
  - `customer-service.yml` (MongoDB connection credentials and databases)
  - `discovery-service.yml` (Eureka Registry configuration)
  - `gateway-service.yml` (Gateway routes, JWT security filter configurations)

### 2. Service Discovery Registry (`discovery`)

- **Port:** `8761`
- **Technology:** Netflix Eureka Server
- **Purpose:** Registers all microservice instances dynamically, allowing them to locate and communicate with each other without hardcoding network addresses.

### 3. Authentication Service (`auth-service`)

- **Port:** `8082`
- **Technology:** Spring Security, JWT (JSON Web Tokens), JPA, PostgreSQL
- **Purpose:** Handles application security, user registration, credentials checking, and token issuance/validation.
- **Database:** PostgreSQL (running in Docker container `ms_pg_sql` at port `5444`)
- **Key Endpoints:**
  - `POST /auth/register` - Register a new user (generates JWT token + customer profile)
  - `POST /auth/login` - Logs in a user, returning a JWT token and user name
  - `GET /auth/validate?token=<jwt-token>` - Validates standard JWT tokens and outputs user metadata for gateway verification

### 4. Customer Service (`customer`)

- **Port:** `8090`
- **Technology:** Spring Data MongoDB, OpenAPI/Swagger UI
- **Purpose:** Exposes CRUD operations for customer details.
- **Database:** MongoDB (running in Docker container `mongo_db` at port `27017`)
- **Key Endpoints (`/customers/api/v1`):**
  - `POST /create` - Create a new customer profile
  - `GET /list` - List all registered customers
  - `GET /get/{id}` - Retrieve details of a customer by ID
  - `GET /email/{email}` - Retrieve details of a customer by Email (used by Auth on login)
  - `PUT /update/{id}` - Update a customer's name, email, or phone number
  - `DELETE /delete/{id}` - Delete a customer profile

### 5. API Gateway Service (`gateway`)

- **Port:** `8080`
- **Technology:** Spring Cloud Gateway, Reactive Webflux
- **Purpose:** Serves as the single unified entry point for API calls. Routes public paths (`/auth/**`) directly and secures sensitive paths (`/customers/**`) by passing them through a custom JWT `AuthenticationFilter`.

### 6. Frontend Portal (`finance-ui`)

- **Port:** `4200`
- **Technology:** Angular 19+ (Reactive Signals, Component Architecture), Sass
- **Purpose:** Premium modern dark-themed user portal. Handles user authentication flow, stores session metadata, dynamically renders profile badges/initials, and displays balance, analytics graph, and recent transaction feeds.

---

## 🐳 Infrastructure & Docker Setup

A `docker-compose.yml` file is provided in the `services/` directory to quickly spin up the required databases and their UI administration tools:

| Service           | Container Name  | Host Port | Internal Port | Description                           |
| :---------------- | :-------------- | :-------- | :------------ | :------------------------------------ |
| **PostgreSQL**    | `ms_pg_sql`     | `5444`    | `5432`        | Relational DB for user authentication |
| **pgAdmin4**      | `ms_pgadmin`    | `5050`    | `80`          | Web administration UI for PostgreSQL  |
| **MongoDB**       | `mongo_db`      | `27017`   | `27017`       | Document DB for customer records      |
| **Mongo Express** | `mongo_express` | `8081`    | `8081`        | Web administration UI for MongoDB     |

---

## 🚀 How to Run the Project

### Prerequisites

- Java 21 JDK or higher
- Maven 3.x+
- Node.js (v18+) and npm
- Docker Desktop installed and running

### Step 1: Start the Infrastructure Databases

Navigate to the `services` directory and boot up the Docker containers:

```bash
cd services
docker-compose up -d
```

### Step 2: Start Backend Services in Order

Start the services in the following order to allow config mapping and discovery registration:

1. **Config Server:**
   ```bash
   cd services/config-server
   mvn spring-boot:run
   ```
2. **Discovery Service (Eureka):**
   ```bash
   cd services/discovery
   mvn spring-boot:run
   ```
3. **Auth Service:**
   ```bash
   cd services/auth-service
   mvn spring-boot:run
   ```
4. **Customer Service:**
   ```bash
   cd services/customer
   mvn spring-boot:run
   ```
5. **API Gateway:**
   ```bash
   cd services/gateway
   mvn spring-boot:run
   ```

Verify all running instances register successfully in the Eureka Dashboard at `http://localhost:8761`.

### Step 3: Start the Angular Frontend

Navigate to the `finance-ui` directory, install dependencies, and start the development server:

```bash
cd finance-ui
npm install
npm start
```

Access the UI panel in your browser at `http://localhost:4200`.

---

## 🔒 Security & Configuration Details

- **Spring Cloud Config Import:** All clients import configuration templates from `config-server` on port `8888` via:
  ```yaml
  spring:
    config:
      import: "optional:configserver:http://localhost:8888"
  ```
- **Stateless Security:** `auth-service` uses stateless session management with BCrypt password hashing and JWT token filtration. Gateway applies token verification filter globally on protected endpoints.

# My Finance - Microservices Project

This project is a scalable, modern financial management platform built using Java 21, Spring Boot, Spring Cloud, Kafka, Angular, and Docker. The system encapsulates end-to-end financial workflows in compliance with microservices standards, including user registration and login (JWT-based), profile management, multi-currency wallet creation (TRY/USD/EUR), real-time peer-to-peer transfers with live exchange rate conversions, deposits/withdrawals, and paginated transaction history tracking.

---

## 🏗️ Architecture Diagram

The system is designed with a single API Gateway routing client requests, services dynamically registering and discovering each other using Eureka Discovery, and communications handled via OpenFeign and Apache Kafka:

```mermaid
graph TD
    %% Client and Gateway
    UI["Finance UI (Angular)"] -->|"API Requests"| GW[API Gateway]

    %% Infrastructure Services
    CS[Config Server] -->|"Central Configuration"| GW
    CS -->|"Central Configuration"| AS[Auth Service]
    CS -->|"Central Configuration"| CUST[Customer Service]
    CS -->|"Central Configuration"| WALLET[Wallet Service]
    CS -->|"Central Configuration"| NOTIF[Notification Service]
    
    DS[Discovery Registry] <-->|"Dynamic Registration & Discovery"| GW
    DS <-->|"Dynamic Registration & Discovery"| AS
    DS <-->|"Dynamic Registration & Discovery"| CUST
    DS <-->|"Dynamic Registration & Discovery"| WALLET
    DS <-->|"Dynamic Registration & Discovery"| NOTIF

    %% Gateway Routing
    GW -->|"/auth/**"| AS
    GW -->|"/customers/**"| CUST
    GW -->|"/wallets/**"| WALLET

    %% Databases and External Services
    AS -->|"User Credentials"| DB_PG[(PostgreSQL)]
    CUST -->|"Customer Profiles"| DB_MONGO[(MongoDB)]
    WALLET -->|"Wallets & Transactions"| DB_PG[(PostgreSQL)]
    WALLET -.->|"Scheduled Task"| EXT_API["External Exchange Rate API"]

    %% Feign Communication
    AS -.->|"Feign Client: Open Customer Profile"| CUST

    %% Asynchronous Event Streaming (Kafka)
    AS -->|"Registration/Login Events"| KAFKA{{Kafka Broker}}
    KAFKA -->|"Message Consumption"| NOTIF
    NOTIF -->|"SMTP Protocol"| MAIL[Gmail SMTP Server]
```

---

## 🛠️ Technologies Used

*   **Java 21** & **Spring Boot 3.x/4.x** (Backend Services)
*   **Angular 17+** (Frontend User Interface)
*   **Spring Cloud Gateway** (API Gateway & JWT Authentication)
*   **Spring Cloud Eureka** (Service Registry & Discovery)
*   **Spring Cloud Config Server** (Centralized Configuration Management)
*   **Apache Kafka** (Asynchronous Message Broker & Event Streaming)
*   **PostgreSQL** (User and Wallet/Transaction Data)
*   **MongoDB** (Customer Profile Data)
*   **Docker** & **Docker Compose** (Infrastructure Containerization)
*   **OpenFeign** & **Spring Cloud LoadBalancer** (Synchronous Inter-Service Communication)
*   **Spring Boot Starter Mail & JavaMailSender** (Automated Email Alerts)
*   **Zipkin** (Distributed Tracing & Request Tracking)

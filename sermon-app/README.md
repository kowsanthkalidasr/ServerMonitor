# SerMon — Event-Driven Server Monitoring Platform

> A production-style, event-driven **server & infrastructure monitoring platform** built with **Java 17** and **Spring Boot**. It combines a secure REST API (JWT + Role-Based Access Control) with a real-time streaming data pipeline — **Change Data Capture (CDC) with Debezium → a 3-node Apache Kafka cluster (KRaft) → TimescaleDB** — to ingest, store, and roll up time-series metrics (CPU, memory, disk, and network) for large fleets of servers.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft-black)
![TimescaleDB](https://img.shields.io/badge/TimescaleDB-PostgreSQL-blue)
![MS SQL Server](https://img.shields.io/badge/MS%20SQL%20Server-2022-red)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Data Model](#data-model)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Database Migrations](#database-migrations)
- [Observability & Monitoring](#observability--monitoring)
- [Load Testing](#load-testing)
- [Roadmap](#roadmap)
- [License](#license)

---

## Overview

**SerMon** (Server Monitor) is a backend platform for **DevOps observability** and **infrastructure monitoring**. It lets operators register the servers ("nodes") in their fleet and continuously collect time-series telemetry for each of them.

The system is designed around a clean separation between the **system of record** and the **analytical time-series store**:

- **Microsoft SQL Server** holds the authoritative, relational state — users, roles, permissions, and the node inventory.
- **TimescaleDB** (a time-series extension of PostgreSQL) stores high-volume metrics and pre-computed rollups optimized for analytical dashboards.
- Changes flowing into SQL Server are streamed in near real time to TimescaleDB using **Change Data Capture (CDC)** via **Debezium** and a fault-tolerant **Apache Kafka** cluster, decoupling the write path from the read/analytics path.

This project demonstrates end-to-end backend engineering skills: **secure REST API design, stateless JWT authentication, role-based access control, multi-datasource persistence, database schema design and migrations, event-driven streaming architecture, change data capture, time-series data modeling, and containerized deployment**.

---

## Key Features

- **Stateless JWT Authentication** — token-based login using the JJWT library, HMAC signing, and a custom Spring Security authentication filter.
- **Role-Based Access Control (RBAC)** — a normalized users / roles / permissions model with users-roles, users-permissions, and roles-permissions relationships.
- **Node (Server) Inventory Management** — register and manage monitored servers with type, operating system, IP address, and operational status, including **soft deletes** and JPA auditing (created/updated by & timestamps).
- **Change Data Capture (CDC) Pipeline** — Debezium captures row-level changes from SQL Server and publishes them to Kafka, keeping the analytical store in sync without dual writes.
- **Highly Available Kafka Cluster** — a 3-broker **KRaft** (no ZooKeeper) cluster with replication factor 3 and `min.insync.replicas=2` for durability.
- **Time-Series Metrics Store** — TimescaleDB **hypertables** for CPU, memory, disk, and network metrics, with **continuous aggregates** (hourly & daily rollups) and automatic **data retention policies**.
- **Multi-Datasource Persistence** — a `@Primary` SQL Server datasource (Spring Data JPA + HikariCP) alongside a dedicated TimescaleDB `JdbcTemplate`, with per-engine Flyway migrations.
- **Robust Error Handling** — a centralized `@RestControllerAdvice` global exception handler returning consistent, structured JSON error responses.
- **Input Validation** — Jakarta Bean Validation on all request DTOs (including IP-address validation).
- **Containerized Infrastructure** — a single `docker-compose.yml` spins up the full stack: SQL Server, TimescaleDB, the 3-node Kafka cluster, Debezium Connect, Kafka UI, the Spring Boot app, and k6.
- **Health-Gated Orchestration** — container health checks and `depends_on` conditions ensure services start in the correct order.
- **Load Testing** — a k6 scenario for exercising the node-registration API under load.

---

## Tech Stack

| Layer | Technology |
| --- | --- |
| **Language** | Java 17 |
| **Framework** | Spring Boot 4.1.0 (Spring Web MVC, Spring Data JPA, Spring Security, Spring for Apache Kafka, Spring Boot Actuator) |
| **Security** | JWT (JJWT 0.13), BCrypt, BouncyCastle, Role-Based Access Control |
| **System of Record** | Microsoft SQL Server 2022 (CDC enabled) |
| **Time-Series Store** | TimescaleDB (PostgreSQL) |
| **Streaming / Messaging** | Apache Kafka (KRaft, 3 brokers), Debezium (Kafka Connect, CDC) |
| **Persistence** | Spring Data JPA / Hibernate, JdbcTemplate, HikariCP |
| **Database Migrations** | Flyway (SQL Server & PostgreSQL/TimescaleDB) |
| **Mapping & Boilerplate** | MapStruct 1.6.3, Project Lombok |
| **Build Tool** | Apache Maven (Maven Wrapper included) |
| **Containerization** | Docker, Docker Compose |
| **Load Testing** | Grafana k6 |
| **Observability** | Spring Boot Actuator, Kafka UI |

---

## Architecture

SerMon follows an **event-driven, CDC-based** architecture that decouples the transactional write path from the analytical read path.

```
                    ┌──────────────────────┐
   REST clients ───▶│   Spring Boot API    │  JWT auth, RBAC, validation
   (JWT)            │   (SerMon app :9000) │
                    └──────────┬───────────┘
                               │ Spring Data JPA (HikariCP)
                               ▼
                 ┌─────────────────────────────┐
                 │  MS SQL Server (:1435)       │  System of record:
                 │  users / roles / permissions │  - relational state
                 │  nodes (CDC enabled)         │  - node inventory
                 └──────────────┬──────────────┘
                                │  Change Data Capture
                                ▼
                 ┌─────────────────────────────┐
                 │  Debezium Connect (:8083)    │  SQL Server connector
                 └──────────────┬──────────────┘
                                │  publishes change events
                                ▼
        ┌───────────────────────────────────────────────┐
        │  Apache Kafka Cluster — KRaft (3 brokers)       │  RF=3, minISR=2
        │  kafka-1 / kafka-2 / kafka-3                    │  topic prefix: sermon-cdc
        └───────────────────────┬───────────────────────┘
                                │  sink connector
                                ▼
                 ┌─────────────────────────────┐
                 │  TimescaleDB (:5432)         │  Analytical store:
                 │  nodes read-replica          │  - hypertables
                 │  cpu / memory / disk /       │  - continuous aggregates
                 │  network metrics             │  - retention policies
                 └─────────────────────────────┘

   Kafka UI (:8080)  ── cluster observability
   k6                ── load testing
```

**Flow:**

1. Authenticated clients call the REST API to register/manage nodes; the API persists relational state to **SQL Server**.
2. **Debezium** captures row-level changes on `dbo.nodes` (CDC) and emits change events to **Kafka** under the `sermon-cdc` topic prefix.
3. A **Kafka sink connector** replicates node data into **TimescaleDB**, keeping the analytical store continuously in sync.
4. Metrics are stored in TimescaleDB hypertables and continuously aggregated into hourly/daily rollups for fast dashboard queries, then aged out by retention policies.

---

## Data Model

### System of Record — Microsoft SQL Server

- **`users`** — credentials (BCrypt password hash), email, and an `A`/`D` status with filtered unique indexes for active usernames/emails.
- **`user_info`** — user profile details plus full auditing (`created_by`, `updated_by`, timestamps).
- **`roles`**, **`permissions`** — RBAC catalog.
- **`users_roles`**, **`users_permissions`**, **`roles_permissions`** — many-to-many join tables assigning access.
- **`nodes`** — the monitored-server inventory: `name`, `os_name`, `ip_address`, `type`, `op_status`, with soft-delete (`is_deleted`) and audit columns.

### Time-Series Store — TimescaleDB

Four metric hypertables (`cpu_metrics`, `memory_metrics`, `disk_metrics`, `network_metrics`), each keyed by `(node_key, time)`:

- **Hypertables** partitioned by `time` with 1-day chunks.
- **`(node_key, time DESC)` indexes** for fast per-node range scans.
- **Continuous aggregates** — hourly and daily materialized rollups (AVG/MIN/MAX) refreshed on a schedule.
- **Retention policies** — raw metrics kept 7 days, hourly rollups 90 days, daily rollups 365 days.
- **`CHECK` constraints** — e.g., utilization values constrained to `0.0–1.0`.

---

## Project Structure

```
SerMon/
├── docker/                         # Containerized infrastructure
│   ├── docker-compose.yml          # Full stack: MSSQL, TimescaleDB, Kafka x3, Debezium, Kafka UI, app, k6
│   ├── Dockerfile                  # Spring Boot app image
│   ├── connectors/                 # Debezium source & Timescale sink connector configs
│   ├── docker-init/                # DB bootstrap SQL (MSSQL & TimescaleDB)
│   ├── docker-start/               # MSSQL startup script
│   ├── scripts/                    # Debezium connector registration
│   └── testing/rest-api/           # k6 load-test script
├── src/main/java/com/sermon/
│   ├── auth/                       # Authentication (login endpoint, JWT issuing)
│   ├── config/
│   │   ├── db/                     # Multi-datasource & Flyway configuration
│   │   └── security/               # Spring Security, JWT filter/service, JPA auditing
│   ├── exception/                  # Global exception handling & API error responses
│   ├── monitoring/                 # Node domain: entity, repository, service, controller, mappers
│   └── user_management/            # Users, roles, permissions domain
├── src/main/resources/
│   ├── db/migration/mssql/         # Flyway migrations for SQL Server
│   ├── db/migration/timescale/     # Flyway migrations for TimescaleDB
│   ├── application*.properties     # Profile-based configuration
│   └── Plan and Notes/             # Design notes & planning docs
├── pom.xml
└── README.md
```

---

## Getting Started

### Prerequisites

- **Docker** and **Docker Compose**
- **JDK 17** (only needed for local, non-containerized development)
- **Maven** (or use the bundled `mvnw` / `mvnw.cmd` wrapper)

### Run the full stack with Docker Compose

The entire platform — databases, Kafka cluster, Debezium, Kafka UI, and the Spring Boot app — can be started with one command:

```bash
cd docker
docker compose up --build
```

Optional tooling profile (k6 load tester):

```bash
docker compose --profile tools up
```

Once healthy, the following endpoints are available:

| Service | URL |
| --- | --- |
| SerMon REST API | http://localhost:9000 |
| Actuator health | http://localhost:9000/actuator/health |
| Kafka UI | http://localhost:8080 |
| Debezium Connect REST | http://localhost:8083 |
| SQL Server | localhost:1435 |
| TimescaleDB | localhost:5432 |

### Build & run the application locally

```bash
# Build (skip tests)
./mvnw clean package -DskipTests

# Run
./mvnw spring-boot:run
```

> On Windows, use `mvnw.cmd` instead of `./mvnw`.

---

## Configuration

Configuration is split into profile-specific property files under `src/main/resources`:

- `application.properties` — base configuration and active profile group.
- `application-prod-security.properties` — security & JWT settings.
- Additional profile files for datasource and infrastructure settings.

The Spring Boot container runs with `SPRING_PROFILES_ACTIVE=dev`.

> **Security note:** For local development the compose files use default credentials and a sample JWT. In any real deployment, externalize all secrets (DB passwords, JWT signing key) via environment variables or a secrets manager, and rotate them.

---

## API Reference

All endpoints are versioned under `/api/v1`. Protected endpoints require a `Bearer` JWT obtained from the login endpoint.

### Authentication

**`POST /api/v1/auth/login`** — authenticate and receive a JWT.

```bash
curl -X POST http://localhost:9000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@admin.com",
    "password": "your-password"
  }'
```

Response:

```json
{
  "token": "<jwt>",
  "type": "Bearer",
  "issuedAt": "2026-07-28T13:46:00.000+00:00",
  "expiresIn": "2026-08-01T13:46:00.000+00:00"
}
```

### Nodes

**`POST /api/v1/nodes`** — register a new monitored node. *(Requires JWT.)*

```bash
curl -X POST http://localhost:9000/api/v1/nodes \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "web-server-01",
    "osName": "Ubuntu 22.04",
    "ipAddress": "10.0.0.12",
    "type": "Linux_Server",
    "opStatus": "UP"
  }'
```

Valid `type` values: `Linux_Server`, `Windows_Server`, `Docker_Hosts`, `Database_Server`.
Valid `opStatus` values: `UP`, `DOWN`, `UNREACHABLE`, `ON_MAINTENANCE`.

**`DELETE /api/v1/nodes/{id}`** — soft-delete a node by its UUID. *(Requires JWT.)*

```bash
curl -X DELETE http://localhost:9000/api/v1/nodes/{id} \
  -H "Authorization: Bearer <jwt>"
```

---

## Database Migrations

Flyway manages schema for both databases with per-engine migration folders:

- `src/main/resources/db/migration/mssql` — SQL Server (users, RBAC, nodes).
- `src/main/resources/db/migration/timescale` — TimescaleDB (nodes read-replica, metric hypertables, aggregates, retention).

Migrations can also be run via the Flyway Maven plugin, which targets both the primary SQL Server and the TimescaleDB instances.

---

## Observability & Monitoring

- **Spring Boot Actuator** exposes health and info endpoints used by Docker health checks.
- **Kafka UI** (http://localhost:8080) provides visibility into topics, partitions, consumer groups, and CDC change events.
- **Debezium Connect REST API** (http://localhost:8083) reports connector status.

---

## Load Testing

A **Grafana k6** scenario under `docker/testing/rest-api` drives the node-registration API to validate throughput and stability. Run it via the `tools` compose profile:

```bash
cd docker
docker compose --profile tools up grafana-k6
```

---

## Roadmap

- Metrics ingestion API and Kafka consumer to write CPU/memory/disk/network metrics.
- Read endpoints backed by the TimescaleDB continuous aggregates.
- Full CRUD + pagination/filtering for nodes; richer node lifecycle transitions.
- User update/deactivate flows and role/permission management endpoints.
- Refresh-token flow and shorter access-token lifetimes.
- OpenAPI/Swagger documentation (springdoc).
- Grafana dashboards over TimescaleDB.
- Expanded unit and Testcontainers-based integration test coverage.

---

## License

This project is provided for educational and portfolio purposes.

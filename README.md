# SerMon — Distributed Event-Driven Server & Infrastructure Monitoring Platform

> ** Master Project Context & Learning Companion README**
> This document serves two purposes:
> 1. **Portfolio & Architecture Documentation** for **SerMon**, a production-grade, high-throughput infrastructure observability backend built with **Java 17**, **Spring Boot 4**, **gRPC/Protobuf**, **MS SQL Server 2022 (CDC)**, **Debezium**, a **3-Broker Apache Kafka (KRaft) Cluster**, and **TimescaleDB**.
> 2. **Full-Context Blueprint for AI Pair-Programming / Concept Learning**: You can paste or upload this file directly into **Gemini in the browser** so it has complete visibility into every module, database table, configuration property, Protobuf contract, current progress, and upcoming learning milestone in the repository without needing direct IDE access.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
![gRPC](https://img.shields.io/badge/gRPC-Protobuf%204.35-blue)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft%20(3%20Brokers)-black)
![Debezium](https://img.shields.io/badge/Debezium-CDC%202.6-purple)
![TimescaleDB](https://img.shields.io/badge/TimescaleDB-PostgreSQL-blue)
![MS SQL Server](https://img.shields.io/badge/MS%20SQL%20Server-2022-red)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![k6](https://img.shields.io/badge/Grafana-k6-7D64FF)

---

##  Instructions for Gemini (When Used as a Learning Tutor in Browser)

If I have shared this README with you in the browser, I am actively building and studying this project for my software engineering resume and technical interviews. When helping me:

1. **Teach the Concept First**: Explain *why* a pattern, protocol, or database mechanism works the way it does under the hood (e.g., B-Tree vs. Hypertable chunks, SQL Server transaction log LSNs in Debezium, HTTP/2 multiplexing in gRPC, Kafka ISR/KRaft quorum, Spring Security filter chain order) before giving code.
2. **Ground Everything in My Exact Codebase**: Use the exact package names (`com.sermon.*`), entity names (`Node`, `User`, `UserInfo`), SQL table/column names (`node_key` vs. `id`), and port/container names (`sermon-app:9000`, `simulator:9080`, `mssql-server:1435`, `timescaledb:5432`) documented below.
3. **Highlight Interview Trade-offs**: Point out *why* we chose a design (e.g., why we use a 4-byte `INTEGER IDENTITY` `node_key` in TimescaleDB instead of repeating a 16-byte `UUID` across millions of metric rows; why we rely on `DatabaseConstraintRegistry` instead of a `SELECT` check before `INSERT`).
4. **Guide Step-by-Step**: Let me write and verify small increments rather than generating entire modules at once, unless I explicitly ask for full boilerplate.

---

## Table of Contents

1. [Project Vision & Scale Targets](#1-project-vision--scale-targets)
2. [High-Level Architecture & Data Pipelines](#2-high-level-architecture--data-pipelines)
3. [Tech Stack & Resume Justification ("Why Each Tool?")](#3-tech-stack--resume-justification-why-each-tool)
4. [Repository & Multi-Module Structure](#4-repository--multi-module-structure)
5. [Deep-Dive Technical Context (What Is Built So Far)](#5-deep-dive-technical-context-what-is-built-so-far)
   - [5.1 Security, Authentication, RBAC & JPA Auditing](#51-security-authentication-rbac--jpa-auditing)
   - [5.2 Multi-Datasource Routing & Dual Flyway Migrations](#52-multi-datasource-routing--dual-flyway-migrations)
   - [5.3 Relational Schema — MS SQL Server (System of Record)](#53-relational-schema--ms-sql-server-system-of-record)
   - [5.4 Time-Series Schema — TimescaleDB (Analytical Store)](#54-time-series-schema--timescaledb-analytical-store)
   - [5.5 Change Data Capture (CDC) Pipeline (Debezium + Kafka KRaft)](#55-change-data-capture-cdc-pipeline-debezium--kafka-kraft)
   - [5.6 gRPC & Protobuf Telemetry Contract (`grpc-proto` & `simulator`)](#56-grpc--protobuf-telemetry-contract-grpc-proto--simulator)
   - [5.7 Centralized Exception Handling & Constraint Translation](#57-centralized-exception-handling--constraint-translation)
   - [5.8 Structured Rolling Logging Architecture](#58-structured-rolling-logging-architecture)
6. [Current Status, Known Issues & Immediate Fixes](#6-current-status-known-issues--immediate-fixes)
7. [Step-by-Step Learning Curriculum & Prompts for Gemini](#7-step-by-step-learning-curriculum--prompts-for-gemini)
8. [Infrastructure, Ports & Commands Reference](#8-infrastructure-ports--commands-reference)
9. [REST API Reference](#9-rest-api-reference)

---

## 1. Project Vision & Scale Targets

**SerMon (Server Monitor)** is an enterprise-style DevOps observability and infrastructure monitoring platform. It manages an inventory of heterogeneous servers (**"nodes"**: `Linux_Server`, `Windows_Server`, `Docker_Hosts`, `Database_Server`) and continuously ingests, aggregates, and analyzes high-velocity time-series telemetry (Ping/Reachability, CPU, Memory, Disk I/O, and Network traffic).

### Target Workload & Back-of-the-Envelope Math
- **Monitored Fleet Size**: Initial target of **1,000 nodes** (scalable to **10,000 nodes**; load-tested node registration at 10,000 nodes with Grafana k6).
- **Telemetry Interval**: Every **5 seconds** per node.
- **Ingestion Rate**:
  - At 1,000 nodes: $\frac{1,000 \text{ nodes}}{5 \text{ sec}} = 200 \text{ metric snapshots/sec} = \mathbf{12,000 \text{ writes/min}}$ across 5 metric domains ($60,000 \text{ table inserts/min}$).
  - At 10,000 nodes: $2,000 \text{ metric snapshots/sec} = \mathbf{120,000 \text{ writes/min}}$.
- **Core Architectural Problem**:
  1. A traditional relational database (OLTP) used for user management and server inventory will suffer severe lock contention and B-Tree index fragmentation if hammered with 60,000+ time-series inserts per minute.
  2. Conversely, time-series analytical queries (hourly/daily AVG/MIN/MAX rollups) need node metadata (`name`, `ip_address`, `type`, `op_status`) joined with metrics, without cross-database network joins or application-level dual writes.

---

## 2. High-Level Architecture & Data Pipelines

SerMon solves this by separating the **Transactional Control Plane (OLTP)** from the **Analytical & Time-Series Data Plane (OLAP/TSDB)** across two distinct pipelines:

```
====================================================================================================
PIPELINE 1: CONTROL PLANE & CHANGE DATA CAPTURE (CDC) — [IMPLEMENTED]
====================================================================================================

  REST Clients (Admin / SRE / k6)
        │
        │  HTTPS / JSON (Stateless Bearer JWT, Argon2 Auth, RBAC)
        ▼
┌───────────────────────────────────────┐
│  sermon-app (Spring Boot :9000)       │
│  - AuthController / UserController    │
│  - NodeController (/api/v1/nodes)     │
│  - Primary DataSource: HikariCP (10)  │
└───────────────────┬───────────────────┘
                    │  JPA / Hibernate (Time-based UUID, @SQLDelete Soft Delete, JPA Auditing)
                    ▼
┌───────────────────────────────────────┐
│  MS SQL Server 2022 (:1435)           │  System of Record (OLTP):
│  Database: serMonDB                   │  - users, user_info, roles, permissions
│  - sys.sp_cdc_enable_db               │  - users_roles, roles_permissions, users_permissions
│  - sys.sp_cdc_enable_table (dbo.nodes)│  - nodes (Filtered Unique Index on active IP)
└───────────────────┬───────────────────┘
                    │  Reads SQL Server Transaction Log (CDC LSNs)
                    ▼
┌───────────────────────────────────────┐
│  Debezium Kafka Connect (:8083)       │
│  1. Source: sermondb-nodes-connector  │  SMT: ExtractNewRecordState (unwrap)
└───────────────────┬───────────────────┘
                    │  Publishes unwrapped JSON change events
                    ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│  Apache Kafka Cluster — 3-Broker KRaft Mode (No ZooKeeper)                    │
│  kafka-1 (:9092/:29092) | kafka-2 (:9094/:29094) | kafka-3 (:9096/:29096)     │
│  Replication Factor = 3 | min.insync.replicas = 2                             │
│  Topic: sermon-cdc.serMonDB.dbo.nodes                                         │
└───────────────────┬───────────────────────────────────────────────────────────┘
                    │  Consumes change events
                    ▼
┌───────────────────────────────────────┐
│  Debezium Kafka Connect (:8083)       │
│  2. Sink: timescale-sink-nodes        │  JdbcSinkConnector (insert.mode=upsert, pk=id)
└───────────────────┬───────────────────┘
                    │  Upserts into read-replica table
                    ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│  TimescaleDB / PostgreSQL (:5432) — Database: serMonTimescaleDB               │
│  - public.nodes (Read-Replica): Maps UUID `id` ──► INTEGER IDENTITY `node_key`│
└───────────────────────────────────────────────────────────────────────────────┘


====================================================================================================
PIPELINE 2: HIGH-THROUGHPUT gRPC TELEMETRY & ANALYTICS — [SCHEMAS & PROTO BUILT / WIRING IN PROGRESS]
====================================================================================================

┌───────────────────────────────────────┐         gRPC / HTTP2 (:9010)          ┌───────────────────────────────────────┐
│  simulator (:9080 REST / :9090 gRPC)  │ ◄───────────────────────────────────► │  sermon-app (:9000 REST / :9010 gRPC) │
│  - Simulates 1,000+ monitored nodes   │  1. NodeRegistryService               │  - Streams active node IDs in batches │
│  - Generates CPU, Mem, Disk, Net, Ping│     rpc FetchRegisteredNodes (stream) │  - Receives NodeStatusMetrics stream  │
│                                       │  2. NodeTelemetryService              │  - Buffers via Kafka / Batch Inserts  │
│                                       │     rpc StreamNodeMetrics (stream)    │  - Timescale HikariCP Pool (max=50)   │
└───────────────────────────────────────┘                                       └───────────────────┬───────────────────┘
                                                                                                    │
                                                                          JdbcTemplate Batch Insert │ (keyed by integer node_key)
                                                                                                    ▼
                                                                                ┌───────────────────────────────────────┐
                                                                                │  TimescaleDB (:5432)                  │
                                                                                │  Hypertables (1-day chunks, 7d retain)│
                                                                                │  - ping_metrics, cpu_metrics          │
                                                                                │  - memory_metrics, disk_metrics       │
                                                                                │  - network_metrics                    │
                                                                                │  Continuous Aggregates:               │
                                                                                │  - *_hourly (kept 90 days)            │
                                                                                │  - *_daily  (kept 365 days)           │
                                                                                └───────────────────────────────────────┘
```

---

## 3. Tech Stack & Resume Justification ("Why Each Tool?")

Understanding *why* each technology was chosen over simpler alternatives is critical for technical interviews:

| Component | Technology | Why We Chose It & Interview Talking Point |
| :--- | :--- | :--- |
| **Core Language & Framework** | **Java 17 & Spring Boot 4.1.0** | Java Records for immutable DTOs (`NodeCreationRequest`, `LoginRequest`, `DatabaseConstraintRegistry.ConstraintErrorInfo`), pattern matching for `instanceof`, and Spring Boot's modular starter ecosystem. |
| **System of Record (OLTP)** | **MS SQL Server 2022** | Strong ACID guarantees for RBAC & node inventory, native transaction-log **Change Data Capture (`sys.sp_cdc_enable_table`)**, and **Filtered Unique Indexes** (`WHERE is_deleted = 0`) which allow soft-deleted IP addresses or usernames to be reused without violating uniqueness. |
| **CDC & Event Streaming** | **Debezium 2.6 + Apache Kafka (KRaft, 3 Brokers)** | Avoids the **"Dual-Write Anti-Pattern"** (writing to SQL Server and TimescaleDB in the same HTTP thread, which causes partial failures if one DB is down). Debezium tails SQL Server's transaction log asynchronously and streams changes through a fault-tolerant 3-broker Kafka cluster (`RF=3`, `min.insync.replicas=2`). |
| **Time-Series Store (OLAP)** | **TimescaleDB (PostgreSQL)** | Automatically partitions append-only metric tables into time-based **Hypertable chunks** (1-day intervals) so indexes fit in RAM. Provides **Continuous Aggregates** (background materialized views for hourly/daily rollups) and automated **Retention Policies** (`drop_chunks` instead of expensive `DELETE` queries that bloat vacuuming). |
| **Telemetry Transport** | **gRPC 1.83 + Protobuf 4.35** | Binary serialization (`NodeStatusMetrics`) is 3–10x smaller and faster to parse than JSON. Uses persistent **HTTP/2 streams** (`stream NodeStatusMetrics`) instead of opening thousands of short-lived HTTP/1.1 TCP connections every 5 seconds. |
| **Password & API Security** | **Argon2id + BCrypt (`DelegatingPasswordEncoder`) + JJWT 0.13** | Uses **Argon2id** (winner of the Password Hashing Competition; memory-hard against GPU attacks, backed by BouncyCastle `bcprov-jdk18on`) as default, wrapped in Spring's `DelegatingPasswordEncoder` (`{argon2}`, `{bcrypt}`) for future-proof algorithm rotation, paired with stateless HMAC-SHA256 JWTs. |
| **Persistence & Mapping** | **Spring Data JPA + `JdbcTemplate` + MapStruct 1.6.3** | Uses **JPA/Hibernate** where rich entity graphs, auditing, and soft-deletes shine (Users, Roles, Nodes in MSSQL), and raw **`JdbcTemplate`** with a 50-connection HikariCP pool for TimescaleDB where low-overhead high-throughput batch writes are needed. **MapStruct** generates compile-time, reflection-free DTO mappers. |
| **Schema Management** | **Dual Flyway 12.4** | Separate migration histories for SQL Server (`db/migration/mssql`) and TimescaleDB (`db/migration/timescale`), configured programmatically via two `Flyway` beans. |
| **Load Testing** | **Grafana k6** | Scripted load testing in JavaScript (`register-nodes.js` — 50 VUs, 10,000 iterations, $p(95) < 500\text{ ms}$) to verify connection pool sizing and unique index behavior under concurrency. |

---

## 4. Repository & Multi-Module Structure

The workspace (`ServerMonitor/`) is organized into three code components and an orchestration root:

```text
ServerMonitor/
├── docker/
│   ├── docker-compose.yml              # Orchestrates all 9 containers with healthchecks & volumes
│   ├── libs/
│   │   └── grpc-proto-1.0.0-SNAPSHOT.jar # Pre-built shared gRPC/Protobuf artifact for Docker builds
│   └── proto/                          # Shared gRPC Contract Maven Module (com.serMon:grpc-proto)
│       ├── pom.xml                     # protobuf-maven-plugin (protoc 4.35.1, grpc-java 1.83.0)
│       └── src/main/proto/
│           └── metrics.proto           # NodeRegistryService & NodeTelemetryService definitions
│
├── sermon-app/                         # Core Backend Application (Spring Boot :9000 REST, :9010 gRPC)
│   ├── pom.xml                         # Spring Boot 4.1.0, JPA, Security, Kafka, Flyway, MapStruct, JJWT
│   ├── improvements                    # Graded code review notes & prioritized improvement checklist
│   ├── docker/
│   │   ├── Dockerfile                  # Multi-stage build (installs grpc-proto jar -> builds fat jar)
│   │   ├── connectors/
│   │   │   ├── mssql-debezium-cdc-nodes-connector.json  # Debezium SQL Server Source config
│   │   │   └── timescale-sink-nodes.json                # Debezium JDBC Sink config (upsert)
│   │   ├── docker-init/
│   │   │   ├── 01-init-mssql.sql       # Creates serMonDB, sermon_admin login, enables DB-level CDC
│   │   │   └── 02-init-timescale.sql   # Creates sermon_admin user, enables timescaledb extension
│   │   ├── docker-start/
│   │   │   └── 01-start-mssql.sh       # Starts sqlservr, runs 01-init-mssql.sql, writes /tmp/mssql-initialized
│   │   ├── scripts/
│   │   │   └── register-debezium-connector.sh # Idempotent curl script registering Source & Sink connectors
│   │   └── testing/rest-api/
│   │       └── register-nodes.js       # Grafana k6 load test (10,000 nodes across 50 VUs)
│   └── src/main/
│       ├── java/com/sermon/
│       │   ├── SerMonApplication.java
│       │   ├── auth/                   # AuthenticationController (/api/v1/auth/login), AuthenticationService, DTOs
│       │   ├── config/
│       │   │   ├── db/                 # DataSourceRoutingConfig (MSSQL + Timescale), FlywayConfig
│       │   │   └── security/           # SecurityConfiguration, JwtService, JwtAuthenticationFilter,
│       │   │                           # PasswordConfig (Argon2/BCrypt), CustomUserDetails, JPA Auditing
│       │   ├── exception/              # GlobalExceptionHandler, DatabaseConstraintRegistry, ApiErrorResponse
│       │   ├── logging/                # Loggers enum (USER_LOG, NODE_CDC_LOG, SECURITY_LOG, etc.)
│       │   ├── monitoring/node_mgmt/   # Node entity, NodeRepository, NodeService, NodeController, NodeMapper
│       │   └── user_management/        # User, UserInfo, Role, Permission, UserRole, UserPermission entities,
│       │                               # Repositories, MapStruct mappers, UserService, UserController
│       └── resources/
│           ├── application.properties                  # Profile groups (dev, prod)
│           ├── application-prod-db.properties          # MSSQL Hikari (max=10) & Timescale Hikari (max=50)
│           ├── application-prod-security.properties    # JWT secret key & expiration
│           ├── application-prod-kafka-cdc.properties   # Kafka bootstrap servers, producer & consumer configs
│           ├── application-prod-grpc.properties        # gRPC server properties placeholder
│           ├── application-prod-pagination.properties  # Spring Data web pagination defaults
│           ├── application-prod-flyway.properties      # Flyway validation settings
│           ├── application-dev-flyway.properties       # Dev Flyway overrides
│           ├── logback-spring.xml                      # RollingFileAppenders (50MB chunks, gzipped archives)
│           ├── Plan and Notes/                         # Original architecture & design notes
│           └── db/migration/
│               ├── mssql/
│               │   ├── V1__create_user_management_tables.sql
│               │   ├── V2__populate_user_management_tables.sql
│               │   └── V3__create_nodes_table.sql
│               └── timescale/
│                   ├── beforeClean.sql
│                   ├── V1__create_nodes_read_replica.sql
│                   └── V2__create_metrics_tables.sql
│
└── simulator/                          # Telemetry Simulator Service (Spring Boot :9080 REST, :9090 gRPC)
    ├── pom.xml                         # Spring Boot 4.1.0, spring-boot-starter-grpc-client & server
    ├── docker/Dockerfile               # Multi-stage build installing grpc-proto jar
    └── src/main/
        ├── java/com/sermon/simulator/
        │   ├── SimulatorApplication.java
        │   ├── controller/SimulationController.java    # Stub controller (api/v1/sim/nodes)
        │   └── grpc/config/GrpcNodeClientConfig.java   # Stub gRPC client config
        └── resources/
            └── application.properties  # server.port=9080, grpc.node-details.server=sermon-app:9010
```

---

## 5. Deep-Dive Technical Context (What Is Built So Far)

This section documents the exact implementation details of the existing codebase so you (and Gemini in the browser) can reference real classes, methods, and SQL structures when learning or extending features.

### 5.1 Security, Authentication, RBAC & JPA Auditing

1. **Password Hashing (`PasswordConfig.java`)**:
   - Uses Spring Security's `DelegatingPasswordEncoder` with default prefix `{argon2}`.
   - `Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()` (backed by BouncyCastle `bcprov-jdk18on:1.84`) hashes passwords with Argon2id (`m=16384, t=2, p=1`).
   - Also registers `BCryptPasswordEncoder` under `{bcrypt}` so legacy or migrated hashes can still be verified seamlessly.
   - Seeded Super Admin user in `V2__populate_user_management_tables.sql`:
     - ID: `00000000-0000-0000-0000-000000000001`
     - Username: `admin` | Email: `admin@admin.com` | Status: `'A'`
     - Password Hash: `{argon2}$argon2id$v=19$m=16384,t=2,p=1$...`

2. **Stateless JWT Flow (`JwtService.java`, `JwtAuthenticationFilter.java`, `AuthenticationService.java`)**:
   - **Login (`POST /api/v1/auth/login`)**: `AuthenticationService` authenticates `email` and `password` via `AuthenticationManager` (`DaoAuthenticationProvider`), loads `CustomUserDetails`, and calls `jwtService.generateToken(user)`.
   - **Token Structure**: Subject (`sub`) = user's `email`; Custom Claim `roles` = list of role names (e.g., `["ROLE_ADMIN"]`); signed with HMAC-SHA (`Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))`).
   - **Request Filtering**: `JwtAuthenticationFilter` extends `OncePerRequestFilter`, extracts the `Bearer <token>` header, verifies signature & expiration via `jwtService.validateAndGetClaims(jwt)`, loads `UserDetails` from `CustomUserDetailsService` (`userRepository.findByEmailWithRoles(email)`), and populates `SecurityContextHolder`.
   - **Security Filter Chain (`SecurityConfiguration.java`)**:
     - Disables CSRF, sets `SessionCreationPolicy.STATELESS`.
     - Whitelists `/api/v1/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/actuator/*`.
     - Custom `authenticationEntryPoint` returns HTTP `401 Unauthorized` with JSON `{"error": "Unauthorized access", "message": "..."}`.

3. **Automatic JPA Auditing (`JpaAuditingConfig.java`, `SpringSecurityAuditorAware.java`)**:
   - `@EnableJpaAuditing(auditorAwareRef = "auditorProvider")` automatically populates:
     - `TimeAuditEntity`: `@CreatedDate OffsetDateTime createdTime`, `@LastModifiedDate OffsetDateTime updatedTime`.
     - `CreateUpdateAuditEntity` (extends `TimeAuditEntity`): `@CreatedBy User createdBy`, `@LastModifiedBy User updatedBy` by extracting `((CustomUserDetails) authentication.getPrincipal()).getUser()` from `SecurityContextHolder`.

---

### 5.2 Multi-Datasource Routing & Dual Flyway Migrations

Because `sermon-app` talks to **both** MS SQL Server (via JPA) and TimescaleDB (via JDBC), Spring Boot's default single-datasource autoconfiguration is replaced with explicit bean wiring:

1. **`DataSourceRoutingConfig.java`**:
   - `@Primary @Bean(name = "mssqlDataSource")` bound to `spring.mssql.datasource.*` (`HikariDataSource`, pool name `MSSQL-Pool`, `maximum-pool-size=10`, `minimum-idle=10`). Used by Spring Data JPA / Hibernate.
   - `@Bean(name = "timescaleDataSource")` bound to `spring.timescaledb.datasource.*` (`HikariDataSource`, pool name `Timescale-Pool`, `maximum-pool-size=50` for heavy concurrent batch writes).
   - `@Bean(name = "timescaleJdbcTemplate")` wrapping `timescaleDataSource`.

2. **`FlywayConfig.java`**:
   - `@Bean(initMethod = "migrate") Flyway mssqlFlyWay` runs migrations from `classpath:db/migration/mssql` against `mssqlDataSource`.
   - `@Bean(initMethod = "migrate") Flyway timescaleFlyWay` runs migrations from `classpath:db/migration/timescale` against `timescaleDataSource` with **`.executeInTransaction(false)`**.
   - **Why `executeInTransaction(false)` is required for TimescaleDB**: PostgreSQL/TimescaleDB forbids creating Continuous Aggregates (`CREATE MATERIALIZED VIEW ... WITH (timescaledb.continuous)`) inside a `BEGIN ... COMMIT` transaction block.

---

### 5.3 Relational Schema — MS SQL Server (System of Record)

Managed in `src/main/resources/db/migration/mssql/`:

#### User Management & RBAC (`V1` & `V2`)
- **`users`**: `id (UNIQUEIDENTIFIER PK)`, `username (VARCHAR(50))`, `password_hash (VARCHAR(255))`, `email (VARCHAR(320))`, `status (VARCHAR(1) CHECK IN ('A', 'D'))`.
  - **Filtered Unique Indexes**:
    - `UQ_users_active_username ON users(username) WHERE status = 'A'`
    - `UQ_users_active_email ON users(email) WHERE status = 'A'`
    - `IX_users_jwt_active_check ON users(id) INCLUDE(status) WHERE status = 'A'`
- **`user_info`**: Shared PK `id` (`FK -> users.id`), `first_name`, `last_name`, `description`, `created_by`, `created_time`, `updated_by`, `updated_time`.
- **`roles`**: `id (BIGINT IDENTITY)`, `name (UNIQUE)`, `description`. Seeded with:
  1. `ROLE_ADMIN`: Full CRUD over users, roles, logs, and all SRE capabilities (Permissions 1–10).
  2. `ROLE_SRE`: Manage monitored servers, intervals, and alert thresholds (Permissions 4–10).
  3. `ROLE_VIEWER`: Read-only access to servers, thresholds, and alerts (Permissions 5, 8, 9).
- **`permissions`**: `id (BIGINT IDENTITY)`, `name (UNIQUE)`, `description`. Seeded with 10 permissions:
  - `user:create`, `user:delete`, `user:modify`
  - `server:create`, `server:read`, `server:delete`
  - `config:write`, `config:read`
  - `alert:read`, `alert:delete`
- **Join Tables**:
  - `users_roles (user_id, role_id, assigned_by, assigned_time)`
  - `roles_permissions (role_id, permission_id, assigned_by, assigned_time)`
  - `users_permissions (user_id, permission_id, assigned_by, assigned_time)` — supports granting **exceptional/temporary permissions** directly to a user outside their assigned role!

#### Monitored Nodes & Native CDC (`V3`)
- **`nodes`**:
  - `id (UNIQUEIDENTIFIER PK)` — generated in Java via `@UuidGenerator(style = UuidGenerator.Style.TIME)` (time-ordered UUIDs minimize B-Tree page splits on clustered PK inserts compared to random `UUID.randomUUID()`).
  - `name (NVARCHAR(100))`, `os_name (NVARCHAR(100))`, `ip_address (VARCHAR(45))` (supports IPv4 & IPv6).
  - `type (VARCHAR(30))` — `CHECK (type IN ('Linux_Server', 'Windows_Server', 'Docker_Hosts', 'Database_Server'))`.
  - `op_status (VARCHAR(20))` — `CHECK (op_status IN ('UP', 'DOWN', 'UNREACHABLE', 'ON_MAINTENANCE'))`.
  - `is_deleted (BIT DEFAULT 0)`, plus audit columns (`created_by`, `created_time`, `updated_by`, `updated_time`).
  - **Soft Delete in Hibernate (`Node.java`)**:
    ```java
    @SQLDelete(sql = "UPDATE nodes SET is_deleted = true WHERE id = ?")
    @SQLRestriction("is_deleted= false")
    ```
  - **Filtered Unique Index**: `UQ_nodes_active_ip_address ON nodes (ip_address) WHERE is_deleted = 0` — ensures no two *active* nodes share an IP address, while allowing a re-provisioned server to reuse the IP of a previously soft-deleted node!
  - **SQL Server CDC Enablement**:
    ```sql
    EXEC sys.sp_cdc_enable_table
         @source_schema = N'dbo',
         @source_name = N'nodes',
         @role_name = NULL,
         @supports_net_changes = 1,
         @captured_column_list = N'id,name,os_name,ip_address,type,op_status,is_deleted'
    ```

---

### 5.4 Time-Series Schema — TimescaleDB (Analytical Store)

Managed in `src/main/resources/db/migration/timescale/`:

#### The Surrogate Key Optimization Pattern (`V1__create_nodes_read_replica.sql`)
```sql
CREATE TABLE nodes (
    node_key   INTEGER GENERATED ALWAYS AS IDENTITY,
    id         UUID         NOT NULL,
    name       VARCHAR(100) NOT NULL,
    os_name    VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45)  NOT NULL,
    type       VARCHAR(30)  NOT NULL,
    op_status  VARCHAR(20)  NOT NULL,
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT PK_nodes_id PRIMARY KEY (node_key),
    CONSTRAINT UQ_nodes_source_id UNIQUE (id)
);
```
> **Why `node_key (INTEGER)` instead of `id (UUID)` as PK in TimescaleDB?**
> In SQL Server, `nodes.id` is a 16-byte `UUID`. In TimescaleDB, every metric table (`cpu_metrics`, `memory_metrics`, etc.) has a composite primary key `(node_key, time)` and secondary indexes. Using a **4-byte `INTEGER` (`node_key`)** instead of a 16-byte `UUID` saves **12 bytes per row per index** across 5 hypertables and 10 continuous aggregate views — reducing index and chunk storage overhead by ~40% at hundreds of millions of rows!

#### Hypertables, Continuous Aggregates & Tiered Retention (`V2__create_metrics_tables.sql`)
Five hypertables partitioned by `by_range('time', INTERVAL '1 day')`, all keyed by `PRIMARY KEY (node_key, time)`:

| Hypertable | Columns & Constraints | Continuous Aggregates | Retention Policy |
| :--- | :--- | :--- | :--- |
| **`ping_metrics`** | `rtt (REAL)`, `packet_loss (REAL CHECK 0.0..1.0)`, `is_reachable (BOOLEAN)` | `ping_metrics_hourly` | Raw: `7 days` |
| **`cpu_metrics`** | `total_cpu_util`, `user_cpu_util`, `system_cpu_util`, `io_wait` (all `REAL CHECK 0.0..1.0`) | `cpu_metrics_hourly`, `cpu_metrics_daily` (`AVG`, `MAX`, `MIN`) | Raw: `7 days`<br>Hourly: `90 days`<br>Daily: `365 days` |
| **`memory_metrics`** | `used_memory_bytes (BIGINT)`, `available_memory_bytes (BIGINT)`, `swap_rate_kbps (REAL)` | `memory_metrics_hourly`, `memory_metrics_daily` (`AVG`, `MAX`, `MIN`) | Raw: `7 days`<br>Hourly: `90 days`<br>Daily: `365 days` |
| **`disk_metrics`** | `disk_util (REAL CHECK 0..1)`, `iops (INTEGER)`, `queue_depth (INTEGER)` | `disk_metrics_hourly`, `disk_metrics_daily` (`AVG`, `MAX`, `MIN`) | Raw: `7 days`<br>Hourly: `90 days`<br>Daily: `365 days` |
| **`network_metrics`** | `rx_bps (BIGINT)`, `tx_bps (BIGINT)`, `packet_drop_rate (REAL CHECK 0..1)` | `network_metrics_hourly`, `network_metrics_daily` (`AVG`, `MAX`, `MIN`) | Raw: `7 days`<br>Hourly: `90 days`<br>Daily: `365 days` |

- **Continuous Aggregate Refresh Policies**:
  - Hourly views refresh every `1 hour` covering `start_offset => '3 hours'` to `end_offset => '1 hour'`.
  - Daily views refresh every `1 day` covering `start_offset => '3 days'` to `end_offset => '1 day'`.

---

### 5.5 Change Data Capture (CDC) Pipeline (Debezium + Kafka KRaft)

1. **3-Broker Apache Kafka Cluster (KRaft Mode)**:
   - Containers: `kafka-1`, `kafka-2`, `kafka-3`.
   - Each node runs `KAFKA_PROCESS_ROLES=controller,broker` with quorum voters `1@kafka-1:9093,2@kafka-2:9093,3@kafka-3:9093` (eliminating ZooKeeper).
   - **Durability**: `KAFKA_DEFAULT_REPLICATION_FACTOR=3`, `KAFKA_MIN_INSYNC_REPLICAS=2`. Can survive the loss of any 1 broker without data loss.
   - **Dual Listeners**: Internal Docker network uses `PLAINTEXT://kafka-1:9092`; host machine access uses `EXTERNAL://localhost:29092` (`29094`, `29096`).

2. **Debezium Source Connector (`mssql-debezium-cdc-nodes-connector.json`)**:
   - Connector class: `io.debezium.connector.sqlserver.SqlServerConnector`.
   - Watches `dbo.nodes` in `serMonDB` and publishes to topic `sermon-cdc.serMonDB.dbo.nodes`.
   - Uses Single Message Transform (SMT) `io.debezium.transforms.ExtractNewRecordState` (`unwrap`) to flatten Debezium's complex `before`/`after`/`op` envelope into a clean row payload with schema metadata attached.

3. **Debezium JDBC Sink Connector (`timescale-sink-nodes.json`)**:
   - Connector class: `io.debezium.connector.jdbc.JdbcSinkConnector`.
   - Consumes `sermon-cdc.serMonDB.dbo.nodes` and writes to `jdbc:postgresql://timescaledb:5432/serMonTimescaleDB` table `nodes`.
   - Configured with `"insert.mode": "upsert"`, `"primary.key.mode": "record_key"`, `"primary.key.fields": "id"`, so both `INSERT` (node creation) and `UPDATE` (including soft-delete `is_deleted = true`) automatically upsert into TimescaleDB's `nodes` read-replica while PostgreSQL auto-generates `node_key` on first insert.

---

### 5.6 gRPC & Protobuf Telemetry Contract (`grpc-proto` & `simulator`)

Located in `docker/proto/src/main/proto/metrics.proto` (compiled into `com.serMon:grpc-proto:1.0.0-SNAPSHOT`):

```protobuf
syntax = "proto3";
package com.sermon.node.grpc;

import "google/protobuf/timestamp.proto";

option java_package = "com.sermon.node_metrics.grpc";
option java_outer_classname = "NodeMetricsProto";
option java_multiple_files = true;

service NodeRegistryService {
  rpc FetchRegisteredNodes (FetchNodesRequest) returns (stream NodeIdBatch);
}

service NodeTelemetryService {
  rpc StreamNodeMetrics (NodeStatusRequest) returns (stream NodeStatusMetrics);
}
```

#### Telemetry Semantics & Linux `/proc/stat` Math
The `NodeStatusMetrics` message combines three fundamental metric types (documented in `docker/proto/temp` and `Metrics to monitor`):
1. **Counters (Monotonically Increasing)**:
   - `total_cpu_util`, `user_cpu_util`, `system_cpu_util` (cumulative CPU time ticks/jiffies since boot), `rx_traffic_bytes`, `tx_traffic_bytes`.
   - *Concept*: OS kernels expose CPU usage in cumulative jiffies (`/proc/stat`). To compute instantaneous utilization between poll $t_1$ and $t_2$:
     $$\text{CPU Util Ratio} = \frac{(\text{user}_2 - \text{user}_1) + (\text{sys}_2 - \text{sys}_1)}{\text{total}_2 - \text{total}_1}$$
2. **Gauges (Point-in-Time Snapshots)**:
   - `used_mem_kb`, `available_mem_kb`, `queue_depth`.
3. **Rates & Percentages (0.0 to 1.0 or per-second throughput)**:
   - `io_wait_percent`, `swap_rate_kbps`, `disk_util_percent`, `iops`, `packet_drop_rate_percent`.

---

### 5.7 Centralized Exception Handling & Constraint Translation

Instead of performing a `SELECT EXISTS(...)` query before every `INSERT` (which doubles database round-trips and is still vulnerable to TOCTOU race conditions under high concurrency), SerMon lets the database enforce constraints and translates the resulting SQL exception cleanly:

- **`DatabaseConstraintRegistry.java`**: Thread-safe `ConcurrentHashMap` mapping lowercase SQL constraint names to user-friendly messages and HTTP statuses:
  - `"uq_nodes_active_ip_address"` $\rightarrow$ `"Active node already exists with the same IP address"` (`409 CONFLICT`)
  - `"ck_nodes_type"` $\rightarrow$ `"Invalid Node type"` (`400 BAD_REQUEST`)
  - `"ck_nodes_op_status"` $\rightarrow$ `"Invalid node Op Status"` (`400 BAD_REQUEST`)
- **`GlobalExceptionHandler.java` (`@RestControllerAdvice`)**:
  - Intercepts `DataIntegrityViolationException`, extracts `org.hibernate.exception.ConstraintViolationException#getConstraintName()`, looks it up in `DatabaseConstraintRegistry`, and returns a structured `ApiErrorResponse` (`timestamp`, `status`, `error`, `message`, `path`, optional `validationErrors`).
  - Also handles `MethodArgumentNotValidException` (Jakarta Bean Validation errors, including `@IpAddress` from Hibernate Validator), `HttpMessageNotReadableException`, `BadCredentialsException`, `NoResourceFoundException`, and `ResourceAlreadyExistsException`.

---

### 5.8 Structured Rolling Logging Architecture

Configured in `src/main/resources/logback-spring.xml` and `com.sermon.logging.Loggers`:
- Uses `SizeAndTimeBasedRollingPolicy` (`maxFileSize = 50MB`, compressed `.log.gz` archives) separated by domain so security/audit events are never drowned out by high-volume application logs:
  - `NODE_CDC_LOG` $\rightarrow$ `logs/CDC/cdc-node.log` (30 days, 2GB cap)
  - `USER_CRUD_LOG` / `USER_LOG` $\rightarrow$ `logs/user/user-crud.log` (30 days, 2GB cap)
  - `AUDIT_USER_LOG` $\rightarrow$ `logs/audit/audit-user.log` (180 days, 5GB cap)
  - `SYSTEM_ERROR_LOG` $\rightarrow$ `logs/system-error.log` (180 days, 5GB cap)
  - `SECURITY_LOG` $\rightarrow$ Used by `JwtService` and `JwtAuthenticationFilter` to log token failures and authentication events.

---

## 6. Current Status, Known Issues & Immediate Fixes

Use this checklist to track what is already working and what needs to be fixed or built next.

### ✅ Completed
- [x] Multi-container Docker Compose environment (MSSQL 2022 + CDC, TimescaleDB, 3-node Kafka KRaft, Debezium Connect + auto-registration init container, Kafka UI, Grafana k6).
- [x] Dual Datasource (`mssqlDataSource` + `timescaleDataSource`) & Dual Flyway migration setup.
- [x] MS SQL Server RBAC schema (`users`, `user_info`, `roles`, `permissions`, 3 join tables) + Argon2 super-admin seeding.
- [x] Stateless JWT Authentication (`POST /api/v1/auth/login`), `DelegatingPasswordEncoder` (Argon2 + BCrypt), and JPA Auditing (`CreatedBy`, `LastModifiedBy`).
- [x] Node registration (`POST /api/v1/nodes`), soft delete (`DELETE /api/v1/nodes/{id}`), paginated ID lookup (`GET /api/v1/nodes/ids`), and SQL Server CDC capture into Kafka & TimescaleDB.
- [x] TimescaleDB hypertable schemas, continuous aggregates (hourly & daily), and retention policies (`V2__create_metrics_tables.sql`).
- [x] Protobuf contract (`metrics.proto`) & multi-stage Docker builds for `sermon-app` and `simulator`.
- [x] Grafana k6 load test script (`register-nodes.js` for 10,000 nodes).

### 🔧 Known Bugs & Code Review Fixes Needed (Do These First!)
1. **SQL Syntax Error in `V2__create_metrics_tables.sql` (Line 37–38)**:
   - In `ping_metrics_hourly`, line 37 is missing a trailing comma and line 38 has an incomplete `ROUND()` call (`ROUND(AVG(is_reachable::int)::numeric,4)::real AS uptime` followed by `ROUND()`).
2. **`UserController` Leaks JPA `User` Entity (`UserController.java`)**:
   - `GET /api/users` and `POST /api/users` return the raw `User` entity, which serializes `password_hash` and triggers lazy-loading issues on `roles`/`permissions`. Must return a `UserResponseDto` / `Page<UserSummaryDto>`.
3. **RBAC Defined in DB & JWT, But Not Enforced on Endpoints (`SecurityConfiguration.java`)**:
   - Add `@EnableMethodSecurity` and annotate controllers with `@PreAuthorize("hasRole('ADMIN')")` or authority checks so `ROLE_ADMIN`, `ROLE_SRE`, and `ROLE_VIEWER` are enforced.
4. **User Soft-Delete & Active Status Verification (`TO DO LATER` note)**:
   - Add `@SQLDelete(sql = "UPDATE users SET status = 'D' WHERE id = ?")` and `@SQLRestriction("status = 'A'")` to `User.java`, and verify user active status during JWT authentication.
5. **API Consistency**:
   - Change `UserController` path from `/api/users` to `/api/v1/users`.
   - Update `NodeController.createNode` to return `ResponseEntity` with HTTP `201 Created` instead of a raw `200 OK` String.
   - Update `GlobalExceptionHandler.handleResourceAlreadyExistsException` to return `ApiErrorResponse` (and fix `"error": "NotFound"` typo on `409 Conflict`).
   - Add missing user constraints (`UQ_users_active_username`, `UQ_users_active_email`) to `DatabaseConstraintRegistry`.
6. **Actuator Wildcard in `SecurityConfiguration.java`**:
   - Change `/actuator/*` to explicitly permit only `/actuator/health` and `/actuator/info`.

---

## 7. Step-by-Step Learning Curriculum & Prompts for Gemini

When you open **Gemini in the browser**, upload or paste this `README.md` and use the prompts below as you work through each module.

---

### Module 1: Hardening Security, RBAC, DTO Projections & Soft Deletes
**Concepts to Learn:**
- Why returning JPA `@Entity` objects from `@RestController` causes security leaks (Jackson serializing `password`) and `LazyInitializationException` / N+1 query problems.
- How Spring Security's `@EnableMethodSecurity` and `@PreAuthorize` use AOP proxies to check `GrantedAuthority` objects in `SecurityContextHolder`.
- How to map both `Role` (`ROLE_ADMIN`) and granular `Permission` (`server:create`, `user:delete`) into `CustomUserDetails.getAuthorities()`.
- Hibernate `@SQLDelete` and `@SQLRestriction` vs. SQL Server Filtered Unique Indexes.

> **Prompt to paste into Gemini:**
> *"I have shared my SerMon README. We are working on Module 1 (Security, RBAC & User Management hardening). First, explain how Spring Security evaluates `@PreAuthorize` under the hood and how I should combine both `user.getRoles()` and `user.getPermissions()` (plus `roles_permissions`) into `CustomUserDetails.getAuthorities()`. Then guide me step-by-step to fix `UserController` entity leakage using MapStruct DTOs and implement soft-delete on `User.java`."*

---

### Module 2: Mastering the CDC Pipeline & TimescaleDB Internals
**Concepts to Learn:**
- How SQL Server Change Data Capture (`sys.sp_cdc_enable_table`) reads the transaction log (`.ldf`) and populates `cdc.dbo_nodes_CT` change tables without slowing down OLTP writes.
- How Debezium's `ExtractNewRecordState` SMT (`unwrap`) transforms CDC events, and how Kafka Connect tracks offsets in internal topics (`debezium_offsets`).
- Why `V2__create_metrics_tables.sql` uses TimescaleDB `create_hypertable`, how chunk exclusion speeds up queries, and how Continuous Aggregates materialize `time_bucket()` rollups.

> **Prompt to paste into Gemini:**
> *"Based on my SerMon README, walk me through the exact lifecycle of a row when `POST /api/v1/nodes` inserts a node into MS SQL Server until it appears in TimescaleDB's `nodes` table with an auto-generated `node_key`. Also help me inspect and fix the SQL syntax issue in `ping_metrics_hourly` inside `V2__create_metrics_tables.sql`."*

---

### Module 3: gRPC Node Discovery & Telemetry Streaming (`simulator` $\leftrightarrow$ `sermon-app`)
**Concepts to Learn:**
- How HTTP/2 streams, channels, and `StreamObserver<T>` work in gRPC Java (`io.grpc`).
- Server-streaming RPC vs. Client-streaming / Bidirectional-streaming RPC, and why `metrics.proto` defines `NodeRegistryService` and `NodeTelemetryService`.
- Passing metadata (such as node `UUID` or auth headers) via gRPC `Metadata` and `ServerInterceptor` / `ClientInterceptor`.
- Simulating realistic Linux/Windows telemetry: generating cumulative CPU counters (jiffies) and computing deltas (`user`, `sys`, `io_wait`), memory gauges, disk IOPS, and network traffic.

> **Prompt to paste into Gemini:**
> *"Look at Section 5.6 of my SerMon README (`metrics.proto`, `sermon-app`, and `simulator`). Explain how `StreamObserver` works in gRPC-Java for streaming RPCs. Let's design and implement step-by-step: (1) `NodeRegistryService` in `sermon-app` to stream active node IDs to `simulator`, and (2) `NodeTelemetryService` so `simulator` can continuously stream `NodeStatusMetrics` for registered nodes every 5 seconds."*

---

### Module 4: High-Throughput Kafka Buffering & Micro-Batch Ingestion into TimescaleDB
**Concepts to Learn:**
- Why inserting 12,000–120,000 metrics/min row-by-row via JPA `save()` exhausts connection pools and network round-trips.
- How to decouple gRPC/REST telemetry ingestion from database writes using a Kafka topic (`server-telemetry`) and batch consumers (`@KafkaListener(batch = "true")` or Reactor Kafka `bufferTimeout(2000, Duration.ofMillis(500))`).
- Using an in-memory cache (Caffeine or Redis) in `sermon-app` to map incoming `UUID nodeId` $\rightarrow$ `INTEGER node_key` so batch inserts into `cpu_metrics`, `memory_metrics`, `disk_metrics`, and `network_metrics` via `timescaleJdbcTemplate.batchUpdate()` don't need a subquery join on every insert!
- PostgreSQL `reWriteBatchedInserts=true` JDBC driver optimization.

> **Prompt to paste into Gemini:**
> *"We are on Module 4 of my SerMon README. Explain how `JdbcTemplate.batchUpdate()` combined with PostgreSQL's `reWriteBatchedInserts=true` optimizes network I/O when inserting thousands of metric rows into TimescaleDB hypertables. Also explain how we should cache the `UUID id -> INTEGER node_key` mapping in `sermon-app` and wire a batched `@KafkaListener` to flush metrics into `cpu_metrics`, `memory_metrics`, `disk_metrics`, and `network_metrics`."*

---

### Module 5: Analytical Query APIs, Redis Latest-State Caching & Alerting Engine
**Concepts to Learn:**
- Writing analytical read endpoints in `sermon-app` that query raw hypertables for short time windows (last 1 hour) and automatically switch to `*_metrics_hourly` or `*_metrics_daily` continuous aggregates for longer windows (last 7–90 days).
- Using **Redis** (Write-Through / Cache-Aside) with keys like `node:latest:{nodeId}` so dashboards fetch current node health in $<2\text{ ms}$.
- Implementing an **Alert Rules Engine** (e.g., `cpu_util > 0.85` for 3 consecutive checks) and handling concurrent status transitions on `Node` in MS SQL Server using **Optimistic Locking (`@Version`)** or **Pessimistic Locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`)**.

> **Prompt to paste into Gemini:**
> *"We are on Module 5 of my SerMon README. First, teach me how to design REST query endpoints in `sermon-app` using `timescaleJdbcTemplate` that dynamically query either the raw hypertable or the `_hourly` / `_daily` continuous aggregates based on the requested time range. Second, explain Optimistic vs. Pessimistic locking in JPA when multiple consumer threads try to update a Node's `op_status` or trigger an alert simultaneously."*

---

### Module 6: Testing Strategy (Unit, Slice, Testcontainers & k6 Load Testing)
**Concepts to Learn:**
- Unit testing `JwtService`, `NodeService`, and `UserService` with JUnit 5 and Mockito.
- Controller slice testing with `@WebMvcTest` and `MockMvc` (testing security filters, `@Valid` Bean Validation, and `GlobalExceptionHandler` responses).
- Integration testing with **Testcontainers** (spinning up ephemeral SQL Server / TimescaleDB / Kafka containers in JUnit).
- Interpreting Grafana k6 latency percentiles ($p(90), p(95), p(99)$) and tuning HikariCP & Tomcat/gRPC thread pools.

> **Prompt to paste into Gemini:**
> *"We are on Module 6 of my SerMon README. Help me design a comprehensive test suite for SerMon: starting with unit tests for `JwtService` and `NodeService`, then `@WebMvcTest` tests verifying `GlobalExceptionHandler` and `DatabaseConstraintRegistry`, and finally a Testcontainers integration test."*

---

## 8. Infrastructure, Ports & Commands Reference

### Service Port & Credential Matrix (`docker/docker-compose.yml`)

| Container Name | Service Role | Internal Address | Host Port(s) | Default Local Credentials |
| :--- | :--- | :--- | :--- | :--- |
| **`sermon-app`** | Core Spring Boot Backend | `sermon-app:9000` (REST)<br>`sermon-app:9010` (gRPC) | `9000`, `9010`, `9011` | Seeded Login: `admin@admin.com` |
| **`simulator`** | Telemetry Simulator App | `simulator:9080` (REST)<br>`simulator:9090` (gRPC) | `9080`, `9090`, `9091` | N/A |
| **`mssql-server`** | MS SQL Server 2022 (CDC) | `mssqlserver:1435` | `1435` | DB: `serMonDB`<br>SA: `sa` / `Admin@123`<br>CDC User: `sermon_admin` / `Admin@123` |
| **`timescaledb`** | TimescaleDB (PostgreSQL) | `timescaledb:5432` | `5432` | DB: `serMonTimescaleDB`<br>Superuser: `postgres` / `Admin@123`<br>App User: `sermon_admin` / `Admin@123` |
| **`kafka-1`** | Kafka KRaft Broker & Controller 1 | `kafka-1:9092` | `9092`, `29092` | Plaintext (No Auth in Dev) |
| **`kafka-2`** | Kafka KRaft Broker & Controller 2 | `kafka-2:9094` | `9094`, `29094` | Plaintext (No Auth in Dev) |
| **`kafka-3`** | Kafka KRaft Broker & Controller 3 | `kafka-3:9096` | `9096`, `29096` | Plaintext (No Auth in Dev) |
| **`debezium-connect`** | Debezium Kafka Connect 2.6 | `debezium:8083` | `8083` | REST API: `http://localhost:8083/connectors` |
| **`kafka-ui`** | Provectus Kafka Web UI | `kafka-ui:8080` | `8080` | Web UI: `http://localhost:8080` |
| **`grafana-k6`** | k6 Load Testing (`tools` profile) | `grafana-k6` | — | Targets `http://sermon-app:9000/api/v1/nodes` |

### Common Build & Run Commands

```bash
# 1. Recompile the shared gRPC/Protobuf JAR (if you edit docker/proto/src/main/proto/metrics.proto)
cd docker/proto
mvn clean package
cp target/grpc-proto-1.0.0-SNAPSHOT.jar ../libs/

# 2. Start the entire distributed stack via Docker Compose
cd docker
docker compose up --build

# 3. Check Debezium CDC connectors status
curl http://localhost:8083/connectors?expand=status

# 4. Run the Grafana k6 10,000-node registration load test
cd docker
docker compose --profile tools up grafana-k6
```

---

## 9. REST API Reference

### 1. Authenticate (`POST /api/v1/auth/login`)
- **Request Body (`LoginRequest`)**:
  ```json
  {
    "email": "admin@admin.com",
    "password": "your-password"
  }
  ```
- **Response (`200 OK` — `AuthResponse`)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "issuedAt": "2026-10-25T21:00:00.000+00:00",
    "expiresIn": "2026-10-29T21:00:00.000+00:00"
  }
  ```

### 2. Register a Monitored Node (`POST /api/v1/nodes`)
- **Headers**: `Authorization: Bearer <jwt>`
- **Request Body (`NodeCreationRequest`)**:
  ```json
  {
    "name": "prod-k8s-worker-01",
    "osName": "Ubuntu 22.04 LTS",
    "ipAddress": "10.0.14.22",
    "type": "Linux_Server",
    "opStatus": "UP"
  }
  ```
  - **Valid `type` enum (`NodeType`)**: `Linux_Server`, `Windows_Server`, `Docker_Hosts`, `Database_Server`
  - **Valid `opStatus` enum (`NodeOpStatus`)**: `UP`, `DOWN`, `UNREACHABLE`, `ON_MAINTENANCE`
- **What Happens Under the Hood**:
  1. Validated via Jakarta Bean Validation (`@NotBlank`, `@Size`, `@IpAddress`).
  2. Mapped via MapStruct (`NodeMapper`), assigned a time-based `UUID`, and audited with `created_by`/`updated_by` from the JWT principal.
  3. Saved to MS SQL Server (`dbo.nodes`); if an active node (`is_deleted = 0`) already uses `10.0.14.22`, `UQ_nodes_active_ip_address` triggers and `DatabaseConstraintRegistry` returns `409 Conflict`.
  4. SQL Server CDC captures the insert $\rightarrow$ Debezium publishes to Kafka topic `sermon-cdc.serMonDB.dbo.nodes` $\rightarrow$ JDBC Sink upserts into TimescaleDB `public.nodes` with a new `INTEGER node_key`.

### 3. Soft-Delete a Node (`DELETE /api/v1/nodes/{id}`)
- **Headers**: `Authorization: Bearer <jwt>`
- **Path Variable**: UUID of the node (validated via regex `[0-9a-fA-F]{8}-...`).
- **Behavior**: Hibernate `@SQLDelete` executes `UPDATE nodes SET is_deleted = true WHERE id = ?`, which is also replicated via CDC to TimescaleDB (`is_deleted = true`). Returns `204 No Content`.

### 4. Fetch Paginated Active Node IDs (`GET /api/v1/nodes/ids?page=0&size=20`)
- **Headers**: `Authorization: Bearer <jwt>`
- **Response**: Spring `Page<UUID>` of all active (`is_deleted = false`) node IDs.

### 5. Create / List Users (`POST /api/users`, `GET /api/users`)
- **Headers**: `Authorization: Bearer <jwt>`
- **Request Body (`UserCreationRequest`)**:
  ```json
  {
    "username": "sre_alice",
    "password": "StrongPassword!123",
    "email": "alice@sermon.io",
    "roleIds": [2],
    "firstName": "Alice",
    "lastName": "Smith",
    "description": "Primary On-Call SRE"
  }
  ```


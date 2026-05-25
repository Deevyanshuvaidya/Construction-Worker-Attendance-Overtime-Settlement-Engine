# Construction Worker Attendance & Overtime Settlement Engine

An enterprise-grade, production-ready backend engine for managing construction worker attendance, tracking active worker sessions, calculating tiered overtime payouts, and settling monthly overtime.

---

## 🏗️ Core Architecture Overview

The system is built on **Modular Monolith** principles (package-by-feature) using Spring Boot 3.4.x, Hibernate (JPA), PostgreSQL (Supabase), and Redis.



## 📋 Prerequisites
Ensure you have the following installed locally:
* **Java 17** (or 21) LTS
* **Docker Desktop** (for running local Redis)

---

## ⚙️ Stepwise Environment Configuration (`.env`)

To connect the application to the database and caching layers, we use an environment configuration file. 

Create a file named **`.env`** in the root directory (or use the configured values from `.env.example`). Here is the step-by-step description of the credentials:

### Step 1: Database Connections (Supabase PostgreSQL)
* **`DB_URL`**: The JDBC connection string to your Supabase PostgreSQL cluster.
  * *Value:* `jdbc:postgresql://db.xxxxxxxxxxxx.supabase.co:5432/postgres`
* **`DB_USERNAME`**: The master database user username.
  * *Value:* `postgres`
* **`DB_PASSWORD`**: The secure password for your database.
  * *Value:* `your_password`

### Step 2: Caching Connections (Redis)
* **`REDIS_HOST`**: Host name where Redis is running.
  * *Value:* `localhost` (when running local Docker)
* **`REDIS_PORT`**: Port where Redis is listening.
  * *Value:* `6379`

### Step 3: Hibernate DDL & Profiles
* **`JPA_DDL_AUTO`**: Auto schema generation level.
  * *Value:* `update` (automatically creates/updates tables on startup)
* **`SPRING_PROFILES_ACTIVE`**: The active Spring profile.
  * *Value:* `dev`

#### Complete `.env` file template:
```properties
# PostgreSQL (Supabase)
DB_URL=jdbc:postgresql://db.xxxxxxxxxxxx.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JPA
JPA_DDL_AUTO=update

# Spring Profile
SPRING_PROFILES_ACTIVE=dev
```

---

## 🚀 How to Run the Project (Simple Way)

We have provided a unified Windows batch script **`run.bat`** in the root directory. This makes starting the entire environment incredibly simple:

### Option A: Windows (Simplest)
Open your terminal in the project directory and run:
```cmd
run
```
*(Or simply double-click **`run.bat`** in your File Explorer!)*

**What it does automatically:**
1. Starts the **Redis** container (`docker-compose up -d redis`).
2. Reads the **`.env`** credentials stepwise and exports them into the session.
3. Launches the Spring Boot server using Maven.

---

### Option B: macOS / Linux
Open your terminal and run:
```bash
# 1. Start Redis
docker-compose up -d redis

# 2. Export environment variables and start the server
export $(grep -v '^#' .env | xargs) && ./mvnw spring-boot:run
```

---

## 🌐 API Interaction & Documentation

Once the server boots successfully, it will start listening on **port 8080**.

* 👉 **Interactive Swagger Sandbox**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* 👉 **Raw OpenAPI Contracts**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Primary API Routes

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/workers` | Register a new construction worker |
| `PUT` | `/api/v1/workers/{id}` | Update worker info |
| `DELETE` | `/api/v1/workers/{id}` | Soft-deletes a worker |
| `PATCH` | `/api/v1/workers/{id}/status` | Activate/deactivate a worker |
| `POST` | `/api/v1/sites` | Register a new construction site |
| `POST` | `/api/v1/attendance/clock-in` | Clocks in worker (cached in Redis, saved to DB) |
| `POST` | `/api/v1/attendance/clock-out` | Clocks out worker, calculates tiered overtime |
| `GET` | `/api/v1/attendance/active` | Get currently active clocked-in workers (from Redis) |
| `GET` | `/api/v1/overtime/summary/{workerId}` | Monthly overtime payout aggregates and daily logs |
| `POST` | `/api/v1/overtime/settle/{workerId}` | Settle pending overtime logs for completed months |

---

## 🧪 Running Automated Tests

The codebase comes equipped with a comprehensive suite of **52 unit and integration tests** verifying all business rules, calculations, and endpoints. Tests run fully in-memory against H2 database configurations.

To run the full test suite, execute:
```bash
./mvnw clean test
```

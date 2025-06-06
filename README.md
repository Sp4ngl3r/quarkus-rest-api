# Student Management API (Quarkus + Redis)

A RESTful Student Management application built using [Quarkus](https://quarkus.io/) with integrated **Redis caching**, *
*H2 in-memory database** (for testing), and layered architecture.

---

## Features

- Create, update, delete, and fetch students via RESTful endpoints
- Pagination support for fetching student lists
- Redis caching for paginated student results
- Integration-tested with Quarkus Test and RestAssured
- Layered architecture (Resource → Service → Repository)
- ️ H2 in-memory database for development and testing

---

## 🏗️ Tech Stack

| Layer      | Tech                              |
|------------|-----------------------------------|
| Backend    | Java + Quarkus (RESTEasy)         |
| Database   | PostgreSQL, H2 (in-memory)        |
| Cache      | Redis                             |
| Testing    | JUnit 5, RestAssured, QuarkusTest |
| Build Tool | Maven                             |

---

## 🔧 Getting Started

### ✅ Prerequisites

- Java 17+
- Maven
- Docker (for Redis)
- RedisInsight (optional, for visualization)

### ▶️ Running Redis Locally

```bash
docker run --name redis -p 6379:6379 -d redis
```

### ▶️ Start the Application

```bash
./mvnw quarkus:dev
```

App will be available at: [http://localhost:8080](http://localhost:8080)

---

## 📬 API Endpoints

| Method | Endpoint                | Description                   |
|--------|-------------------------|-------------------------------|
| GET    | `/api/v1/students`      | List all students (paginated) |
| GET    | `/api/v1/students/{id}` | Get student by ID             |
| POST   | `/api/v1/students`      | Create a student              |
| PUT    | `/api/v1/students/{id}` | Update a student              |
| DELETE | `/api/v1/students/{id}` | Delete a student              |

### 🔍 Pagination Example

```http
GET /api/v1/students?page=0&size=10
```

---

## 💾 Redis Caching

- Paginated results are cached using keys like:  
  `students:page:0:size:10`

- On **create/update/delete**, the related cache is invalidated to avoid stale data.

### Visualizing Redis

```bash
docker run -d -p 8001:8001 --name redis-insight redis/redisinsight
```

Then visit: [http://localhost:8001](http://localhost:8001)

---

## 🧪 Running Tests

```bash
./mvnw test
```

### ✅ Test Coverage Includes:

- CRUD operations
- Redis cache population
- Redis cache invalidation
- REST API integration with `RestAssured`

---

## 📄 License

This project is licensed under the MIT License.
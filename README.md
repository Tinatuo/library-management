# 📚 Library Management System

A **Modular Monolith** library management backend built with **Spring Boot 4** and **Java 21**, featuring JWT-based authentication, role-based authorization, book reservations with a FIFO queue, and automated late-return fines.

---

## ✨ Features

- **Book catalog management** — CRUD operations, pagination, and cover image upload/download
- **Member management** — registration and profile management
- **Loan lifecycle** — borrowing, returning, and renewing books
- **Reservation queue** — FIFO waitlist for books that are currently unavailable, with automatic promotion when a book is returned
- **Fine system** — automatic fine calculation on late returns, with payment tracking
- **Redis caching** — DTO-level caching for books, members, and completed loans via `@Cacheable`/`@CacheEvict`, each with its own TTL and eviction rules
- **JWT authentication** — stateless access tokens (15 min expiry) + rotating refresh tokens stored in the database
- **Role-based access control** — `ADMIN`, `LIBRARIAN`, and `MEMBER` roles enforced via `@PreAuthorize`
- **Ownership-based authorization** — members can only access their own loans, reservations, and fines
- **Dockerized setup** — one-command startup with PostgreSQL and Redis

---

## 🏗️ Architecture

This project follows a **Modular Monolith** architecture. The codebase is organized into independent business modules that live in a single deployable application but are structurally isolated from one another.

```
com.example.library
├── book           # Book catalog & cover storage
├── member         # Library members
├── loan           # Borrowing & returning books
├── fine           # Late-return fines
├── reservation    # FIFO reservation queue
├── auth           # Authentication, JWT, refresh tokens
├── security       # Cross-cutting ownership checks (LoanSecurity, ReservationSecurity)
└── common         # Shared exceptions, DTOs, base entities, security infrastructure
```

Each module follows the same internal layout:

```
<module>
├── controller   # REST endpoints
├── service      # Business logic (interface + implementation)
├── repository   # Spring Data JPA repositories
├── entity       # JPA entities
├── dto          # Request/response DTOs
└── mapper       # Entity ↔ DTO mapping
```

### Core architectural rule

> **A module must never inject another module's `Repository` directly.**
> All cross-module communication happens exclusively through **Service interfaces**.

For example, when the `loan` module needs book data, it depends on `BookService`, never on `BookRepository`. This keeps module boundaries enforceable, makes each module independently testable, and leaves the door open for extracting a module into its own microservice later without touching its public contract.

---

## 🛠️ Tech Stack

| Layer            | Technology                                  |
|------------------|----------------------------------------------|
| Language         | Java 21                                      |
| Framework        | Spring Boot 4 (Web MVC, Data JPA, Validation, Security) |
| Database         | PostgreSQL                                   |
| Caching          | Redis                                        |
| Auth             | Spring Security + JJWT (JSON Web Tokens)     |
| Build tool       | Maven                                        |
| Containerization | Docker & Docker Compose                      |
| Boilerplate      | Lombok                                       |

---

## 🔐 Authentication & Authorization

- Authentication is **stateless**, based on JWT access tokens sent as `Authorization: Bearer <token>`.
- **Access tokens** are short-lived (default: 15 minutes).
- **Refresh tokens** are UUID-based, persisted in the database, and **rotated** on every use (a used refresh token is invalidated and replaced).
- Three roles are supported:
  | Role        | Description                                   |
  |-------------|------------------------------------------------|
  | `ADMIN`     | Full access, including staff registration and deletions |
  | `LIBRARIAN` | Manage books, members, loans, reservations, and fines |
  | `MEMBER`    | Access limited to their own loans/reservations/fines |
- Ownership checks (e.g. "is this loan mine?") are handled by dedicated security beans — `LoanSecurity`, `ReservationSecurity`, and `CurrentUserService` — referenced directly inside `@PreAuthorize` expressions.
- An `AdminSeeder` creates a default admin account on startup so the API is usable immediately.

---

## ⚡ Caching Strategy

Redis backs three independent cache regions, each configured in `RedisConfig` with its own TTL:

| Cache region | TTL     | Used in                          |
|--------------|---------|------------------------------------|
| `books`      | 10 min  | `BookServiceImpl#getBookById`      |
| `members`    | 5 min   | `MemberServiceImpl#getMemberById`  |
| `loans`      | 60 min  | `LoanServiceImpl#getLoanById`      |

> **Only DTOs are cached — never JPA-managed entities.** Caching an entity that's shared and mutated across modules risks serving stale state once another module updates it outside of Hibernate's session. For this reason, `getBookEntityById` (used internally by other modules for write operations) is deliberately excluded from caching, while the public-facing DTO-returning methods are cached safely via `@Cacheable`/`@CacheEvict`.

A notable refinement is on the `loans` cache: `getLoanById` only caches a loan **once it's no longer `ACTIVE`** (`unless = "#result.status.name() == 'ACTIVE'"`). An active loan's state can still change (renewal, return), so caching it would risk staleness; a returned loan is effectively immutable, so it's safe — and worthwhile, given the long 60-minute TTL — to cache.

---

## 🚀 Getting Started

### Prerequisites

- Java 21
- Maven (or use the included wrapper — see [note below](#-known-issue-maven-wrapper))
- Docker & Docker Compose (recommended, easiest way to run everything)

### Option 1 — Run with Docker (recommended)

```bash
docker compose up --build
```

This will spin up:
- A PostgreSQL 18 container on port `5433`
- A Redis 7 container on port `6379`
- The Spring Boot application on port `8080`, waiting for both to pass their health checks before starting

All sensitive configuration (DB credentials, JWT secret, admin credentials) is injected via environment variables in `docker-compose.yml`.

### Option 2 — Run locally (e.g. from IntelliJ)

1. Start a local PostgreSQL instance matching the settings in `application.properties`.
2. Run `LibraryApplication`. In the absence of environment variables, sensible local defaults are used automatically (see `application.properties`).

### ⚠️ Known issue: Maven wrapper

The `.mvn/wrapper/` directory is currently missing from this repository, so `./mvnw` cannot be invoked directly. Until this is fixed, either:
- Use a locally installed Maven (`mvn spring-boot:run`), or
- Use the Docker setup, which builds with the official Maven image and works around this.

---

## ⚙️ Configuration

Configuration lives in `src/main/resources/application.properties`. Every sensitive value is read from an environment variable, with a hardcoded fallback for convenient local development:

| Property                          | Environment Variable       | Default (local dev)   |
|------------------------------------|-----------------------------|------------------------|
| Database host/port/name            | `DB_HOST`, `DB_PORT`, `DB_NAME` | `127.0.0.1` / `5432` / `library_db` |
| Database credentials               | `DB_USERNAME`, `DB_PASSWORD` | `postgres` / *(local dev password)* |
| JWT secret                         | `JWT_SECRET`                | *(local dev key)*      |
| Access token expiry                | `JWT_EXPIRATION_MS`         | `900000` (15 min)      |
| Refresh token expiry               | `JWT_REFRESH_EXPIRATION_MS` | `604800000` (7 days)   |
| Default admin credentials          | `ADMIN_USERNAME`, `ADMIN_PASSWORD` | `admin` / *(local dev password)* |
| Book cover storage directory       | `BOOK_COVERS_DIR`           | `uploads/book-covers`  |
| Redis host/port                    | `REDIS_HOST`, `REDIS_PORT`  | `127.0.0.1` / `6379`   |
| Book cache TTL (seconds)           | `BOOK_CACHE_TTL_SECONDS`    | `600`                  |
| Flyway enabled                     | `FLYWAY_ENABLED`            | `false`                |

> **Note:** Flyway migration scripts exist under `src/main/resources/db/migration`, but Flyway is currently disabled in favor of `spring.jpa.hibernate.ddl-auto=update`. This is a known inconsistency to be resolved as the project matures — one of the two approaches will eventually be adopted as the single source of truth for schema management.

---

## 📡 API Overview

Base path: `/api`

### Auth — `/api/auth`
| Method | Endpoint             | Access        | Description                         |
|--------|-----------------------|---------------|--------------------------------------|
| POST   | `/register`           | Public        | Register a new member account        |
| POST   | `/register-staff`     | `ADMIN`       | Register a librarian/admin account   |
| POST   | `/login`              | Public        | Authenticate and receive tokens      |
| POST   | `/refresh`            | Public        | Rotate refresh token for a new access token |
| POST   | `/logout`             | Public        | Invalidate a refresh token           |
| GET    | `/me`                 | Authenticated | Get the currently authenticated user |

### Books — `/api/books`
| Method | Endpoint             | Access                    | Description               |
|--------|-----------------------|---------------------------|----------------------------|
| POST   | `/`                   | `ADMIN`, `LIBRARIAN`      | Create a book              |
| GET    | `/{id}`               | Authenticated             | Get a book by ID           |
| GET    | `/`                   | Authenticated             | List books (paginated)     |
| PUT    | `/{id}`               | `ADMIN`, `LIBRARIAN`      | Update a book               |
| DELETE | `/{id}`               | `ADMIN`                   | Delete a book               |
| POST   | `/{id}/cover`         | `ADMIN`, `LIBRARIAN`      | Upload a cover image        |
| GET    | `/{id}/cover`         | Authenticated             | Download a cover image      |
| DELETE | `/{id}/cover`         | `ADMIN`, `LIBRARIAN`      | Delete a cover image         |

### Members — `/api/members`
| Method | Endpoint    | Access                                       | Description         |
|--------|-------------|-----------------------------------------------|-----------------------|
| POST   | `/`         | `ADMIN`, `LIBRARIAN`                          | Create a member       |
| GET    | `/{id}`     | `ADMIN`, `LIBRARIAN`, or the member themself   | Get a member          |
| GET    | `/`         | `ADMIN`, `LIBRARIAN`                          | List members (paginated) |
| PUT    | `/{id}`     | `ADMIN`, `LIBRARIAN`, or the member themself   | Update a member       |

### Loans — `/api/loans`
| Method | Endpoint                    | Access                                          | Description             |
|--------|------------------------------|--------------------------------------------------|---------------------------|
| POST   | `/`                          | `ADMIN`, `LIBRARIAN`, or the borrowing member      | Borrow a book              |
| POST   | `/{id}/return`               | `ADMIN`, `LIBRARIAN`                              | Return a book               |
| POST   | `/{id}/renew`                | `ADMIN`, `LIBRARIAN`, or the loan's owner          | Renew a loan                |
| GET    | `/{id}`                      | `ADMIN`, `LIBRARIAN`, or the loan's owner          | Get a loan                  |
| GET    | `/`                          | `ADMIN`, `LIBRARIAN`                              | List all loans (paginated) |
| GET    | `/member/{memberId}`         | `ADMIN`, `LIBRARIAN`, or the member themself       | List a member's loans      |

### Reservations — `/api/reservations`
| Method | Endpoint                     | Access                                          | Description                  |
|--------|-------------------------------|---------------------------------------------------|---------------------------------|
| POST   | `/`                           | `ADMIN`, `LIBRARIAN`, or the reserving member       | Reserve a book (joins the queue) |
| POST   | `/{id}/cancel`                | `ADMIN`, `LIBRARIAN`, or the reservation's owner    | Cancel a reservation             |
| GET    | `/member/{memberId}`          | `ADMIN`, `LIBRARIAN`, or the member themself        | List a member's reservations    |
| GET    | `/book/{bookId}/queue`        | `ADMIN`, `LIBRARIAN`                               | View the wait queue for a book  |

### Fines — `/api/fines`
| Method | Endpoint                          | Access                                       | Description               |
|--------|-------------------------------------|-------------------------------------------------|-----------------------------|
| GET    | `/member/{memberId}`               | `ADMIN`, `LIBRARIAN`, or the member themself      | List a member's fines       |
| GET    | `/member/{memberId}/unpaid-summary`| `ADMIN`, `LIBRARIAN`, or the member themself      | Get total unpaid fine amount |
| POST   | `/{id}/pay`                         | `ADMIN`, `LIBRARIAN`                              | Mark a fine as paid          |

---

## 🔄 Domain Workflows

**Borrowing & Reservations**
1. A member borrows an available book → its status becomes `BORROWED`.
2. If a book is unavailable, a member can reserve it, joining a **FIFO queue** (`WAITING` → `READY` → `FULFILLED`/`CANCELLED`).
3. When the book is returned, the next reservation in queue is automatically promoted to `READY`.

**Fines**
- Returning a book after its due date automatically generates a `Fine`, tracked as `PAID` or `UNPAID`.
- Members (or staff on their behalf) can query a member's total unpaid balance and settle fines.

---

## 🗺️ Roadmap

- [ ] Resolve the `.mvn/wrapper/` issue so `./mvnw` works out of the box
- [ ] Decide between Flyway migrations and `ddl-auto` as the single schema management strategy
- [ ] Expand automated test coverage across modules

---

## 📄 License

This project is currently unlicensed — add a license file if you plan to distribute it.

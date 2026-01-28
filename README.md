# Theatre
# Backend System Documentation

## 1. System Overview

This project represents a production-oriented backend system designed for a movie booking platform. The system focuses on correct business flow, data consistency, performance optimization, and secure access control.

The architecture is built to handle real-world scenarios such as user authentication, role-based authorization, movie and show management, seat booking, and high read/write traffic.

---

## 2. High-Level Application Flow

### Step 1: User Registration and Authentication

* Every user must sign up and log in before interacting with the system.
* Authentication is mandatory for all protected endpoints.
* After successful authentication, the user is assigned a role.


The system follows Role-Based Access Control (RBAC).

### Role-Based Access Control (RBAC) Hierarchy
The system enforces security through three distinct tiers of authority:

* SUPER_ADMIN : 
The highest authority level. This role is responsible for User Management (creating or promoting other ADMIN accounts), managing global system properties, and performing sensitive database operations.

* ADMIN : 
The operational manager. This role focuses on Resource Management, specifically handling Movies, Theatres, and Shows. They have full CRUD (Create, Read, Update, Delete) permissions over the theater infrastructure but cannot modify other admin accounts.

* USER : 
The standard customer role. This role is restricted to Consumer Actions, such as browsing the movie catalog, viewing showtimes, and managing their own Reservations and seat selections.

Authorization is enforced at the API level to ensure users can only perform actions allowed by their role.

---

## 3. Core Business Workflow

### 3.1 Movie Management

* Admins can create, update, and delete movies.
* A movie can have multiple associated shows.

Cascade Deletion Logic :

* When a movie is deleted:

  * All related shows are automatically deleted
  * All dependent records such as seat mappings or schedules are cleaned up

This ensures:

* No orphan records
* Strong referential integrity
* Predictable system behavior

---

### 3.2 Show Management

* Shows are linked to a specific movie and theatre
* Each show has its own seat configuration

Any update or deletion respects the parent-child relationship between movies and shows.

---

### 3.3 Seat Booking Flow

1. User selects a movie
2. User selects an available show
3. User views available seats
4. User books seats

Concurrency and consistency are handled to prevent double booking.

---

## 4. API Design (Sample Endpoints)

Authentication:

* POST /auth/signup : http://localhost:8080/auth/signup
* POST /auth/login : http://localhost:8080/auth/authenticate

Movies (Admin Only):

* POST /movies : http://localhost:8080/api/v1/movies/create
* DELETE /movies/{movieId} : http://localhost:8080/api/v1/movies/movie/delete/{movieId}
* GET /movies : http://localhost:8080/api/v1/movies/all

Shows:

* POST /shows (Admin) : http://localhost:8080/api/v1/shows/show/create
* GET /shows?movieId : http://localhost:8080/api/v1/movies/movie/show/{movieId}

Booking:

* POST /bookings : http://localhost:8080/api/v1/reservations/reserve
* GET /bookings/user : http://localhost:8080/api/v1/reservations/reservation/{reservationId}

All endpoints are protected based on role and authentication state.

---

## 5. Redis – Performance Optimization Layer

Redis is introduced after the core business flow is established, strictly as an optimization layer.

Where Redis Is Used:

* Caching movie listings
* Caching show details for high-traffic endpoints

Why Redis Is Needed:

* Multiple users frequently access the same movie and show data
* Without caching, the database would be hit repeatedly

How Redis Helps:

* Frequently accessed data is served from memory
* Database load is reduced significantly
* Faster response times for end users

Cache Strategy:

* Read-through caching
* TTL-based eviction
* Cache invalidation on write and update operations

---

## 6. Apache Kafka – Event-Driven Communication

Kafka is used to handle asynchronous workflows and system decoupling.

Kafka Use Cases:

* Publishing events when:

  * A movie is created or deleted
  * A booking is confirmed
* Enabling downstream services to react without tight coupling

Benefits:

* Non-blocking request handling
* Better scalability
* Reliable event delivery

Kafka ensures that business events are processed independently without slowing down core APIs.

---

## 7. Security Design

Authentication:

* Token-based authentication
* All sensitive endpoints require a valid token

Authorization (RBAC):

* Access decisions are made based on role
* Prevents privilege escalation

This layered security approach ensures both identity verification and permission enforcement.

---

## 8. Technical Requirements & Stack
This project is built using a modern, event-driven architecture to handle high-concurrency ticket bookings.

### Core Backend
Java 17+: Utilizes the Streams API for efficient seat filtering and data transformation.

Spring Boot 3.x: The foundation of the microservice, leveraging Spring Data JPA for database interactions.

Spring Security: Implements Role-Based Access Control (RBAC) with JWT (JSON Web Tokens) for secure stateless authentication.

### Data & Messaging
PostgreSQL: The primary relational database used for storing persistent entities like Movies, Theatres, and Reservations.

Apache Kafka: Acts as the message broker, using a Producer-Consumer model to handle real-time booking and cancellation events via the theatre-activity topic.

Redis: Used as a high-speed caching layer to store and evict Seat Structure data, ensuring lightning-fast availability checks.


---

## 9. Conclusion

This project is structured to reflect real-world backend engineering practices, prioritizing:

* Correct business logic
* Clean data relationships
* Performance under load
* Secure and maintainable access control

Redis and Kafka are used deliberately where they add measurable value, not as premature optimizations.

---
https://roadmap.sh/projects/movie-reservation-system

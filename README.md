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

### Step 2: Role Assignment and Authorization

The system follows Role-Based Access Control (RBAC).

Roles and Responsibilities:

* ADMIN: Manages movies, shows, and system-level operations
* USER: Browses movies, views shows, and books seats

Authorization is enforced at the API level to ensure users can only perform actions allowed by their role.

---

## 3. Core Business Workflow

### 3.1 Movie Management

* Admins can create, update, and delete movies.
* A movie can have multiple associated shows.

Cascade Deletion Logic:

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

* POST /auth/signup
* POST /auth/login

Movies (Admin Only):

* POST /movies
* DELETE /movies/{movieId}
* GET /movies

Shows:

* POST /shows (Admin)
* GET /shows?movieId=

Booking:

* POST /bookings
* GET /bookings/user

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

## 8. Complexity and Scalability Considerations

Time Complexity:

* Redis cache access: O(1) average
* Kafka message append: O(1)

System-Level Benefits:

* Reduced database load
* Horizontal scalability
* Fault tolerance via message replay and cache expiration

---

## 9. Conclusion

This project is structured to reflect real-world backend engineering practices, prioritizing:

* Correct business logic
* Clean data relationships
* Performance under load
* Secure and maintainable access control

Redis and Kafka are used deliberately where they add measurable value, not as premature optimizations.

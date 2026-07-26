# AGENTS.md - System Design & Coding Guidelines for Feed Service

## 📌 Project Overview
This repository contains a high-throughput, distributed **Feed Generation Service** built with **Java 23** and **Spring Boot 3.x**.
The primary goal of this project is to implement and demonstrate core system design paradigms for massive-scale newsfeed systems, including:
- **Hybrid Fan-out Architecture** (Fan-out on Write + Fan-out on Read)
- **Redis Caching & Timeline Management**
- **Kafka Event-Driven Async Processing**
- **Cursor-based Pagination**
- **Celebrity & Hot-Key Handling Strategies**

---

## 🏗️ Architectural Guardrails & Core Paradigms

### 1. Hybrid Fan-out Strategy
- **Regular Users (Followers < Threshold, e.g., 10,000)**: Use **Fan-out on Write (Push Model)**.
  - When a regular user posts, publish a `PostCreatedEvent` to Kafka.
  - Workers consume the event and inject the `postId` directly into all followers' Redis timelines.
- **Celebrity Users (Followers ≥ Threshold)**: Use **Fan-out on Read (Pull Model)**.
  - When a celebrity posts, do NOT push to followers' Redis feeds (prevents write amplification & hot Redis writes).
  - Save post to Celebrity Timeline.
  - On feed read request, merge the follower's Redis timeline with posts pulled from celebrities they follow.

### 2. Caching & Data Structures
- **Redis Timelines**: Store user timelines using Redis **Sorted Sets (`ZSET`)**.
  - `Key`: `feed:{userId}`
  - `Score`: Epoch timestamp (or composite score for ranking)
  - `Member`: `postId`
- **Cache Eviction & Bounds**: Set a maximum length (e.g., top 800-1000 posts per timeline) using `ZREMRANGEBYRANK`.
- **Cache Miss Fallback**: If Redis cache misses, rebuild the timeline from the persistent database asynchronously or on-demand.

### 3. Cursor-Based Pagination
- **No Offset Pagination**: Never use SQL `OFFSET` or Redis `ZRANGE` with numerical offsets (avoids $O(N)$ performance degradation and duplicate/skipped posts).
- **Cursor Structure**: Base cursors on immutable sort attributes (`last_evaluated_timestamp` + `last_evaluated_post_id`).
- **Query Pattern**: Fetch items where `score < cursor.score` or `(score == cursor.score AND post_id < cursor.post_id)`.

### 4. Kafka Messaging & Event Consistency
- **Partitioning Strategy**: Partition Kafka topics (`post-events`, `follow-events`) by `userId` or `authorId` to preserve per-user event ordering.
- **Idempotent Consumers**: Ensure Redis pipeline inserts and fan-out updates are idempotent (e.g., using `ZADD` which naturally updates without duplicating).
- **Consumer Concurrency**: Configure Kafka consumer groups to scale out fan-out workers horizontally.

---

## 💻 Code Style & Implementation Rules

### 1. Modern Java (Java 23) Standards
- Use **Java Records** for immutable DTOs, Event payloads, and Value Objects.
- Prefer **Pattern Matching** (switch expressions, instance-of) and `Sealed Classes` where applicable.
- Avoid unnecessary mutable boilerplate; leverage Lombok annotations cleanly (`@Getter`, `@Builder`, `@RequiredArgsConstructor`).

### 2. Layered Architecture
Maintain clean separation of concerns across packages:
- `config/`: Redis, Kafka, Async, Docker Compose configurations.
- `controller/`: REST endpoints & payload validation.
- `service/`: Core domain logic (FeedService, FanoutService, PostService, FollowService).
- `repository/`: Spring Data JPA / Redis Repositories.
- `model/`: Entities, Records, Enums, DTOs.
- `kafka/`: Producers, Consumers, and Event definitions.

### 3. Error Handling & Observability
- **Centralized Exception Handling**: Use `@RestControllerAdvice` for API response consistency.
- **Structured Logging**: Use MDC (Mapped Diagnostic Context) for `traceId` propagation across HTTP & Kafka events.
- **Metrics**: Instrument fan-out execution latency, Redis cache hit/miss rates, and Kafka consumer lag via Micrometer metrics.

---

## 🧪 Testing & Verification Rules
- **No Swallowed Exceptions**: Never catch and ignore exceptions in test or production code.
- **Testcontainers for Integration Tests**: Use `Testcontainers` for PostgreSQL, Redis, and Kafka in integration tests to ensure tests run against real instances.
- **Unit Testing**: Focus unit tests on ranking algorithms, cursor encoding/decoding, and hybrid threshold routing logic.

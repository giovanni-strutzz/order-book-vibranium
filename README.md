# Vibranium Order Book

A high-performance order book engine built with Java and Spring Boot, designed to handle up to 5,000 trades per second. The platform allows users to place buy and sell orders for Vibranium, with full wallet management, trade matching, and transaction traceability.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Design Decisions](#design-decisions)

---

## Overview

The Vibranium Order Book is an MVP trading platform that implements a real order book engine with the following capabilities:

- Place **buy and sell** orders for Vibranium
- **Price-time priority** matching engine (best price wins; equal prices resolved by arrival time)
- **Wallet management** with available and reserved balance separation
- **Asynchronous settlement** via Kafka events
- **Full traceability** of every transaction through an immutable audit log
- **Idempotency** via `X-Correlation-Id` header — duplicate orders are safely rejected

---

## Architecture

The project follows **Hexagonal Architecture** (Ports & Adapters) combined with **CQRS** and **Domain-Driven Design**.

```
┌─────────────────────────────────────────────────────────────┐
│                        Inbound Adapters                     │
│              REST Controllers  ·  Kafka Consumers           │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                      Application Layer                      │
│         Command Handlers (write)  ·  Query Handlers (read)  │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                       Domain Core                           │
│   Order · Wallet · Trade · OrderBook · WalletService        │
│              Ports In  ·  Ports Out                         │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                     Outbound Adapters                       │
│         MongoDB  ·  Redis  ·  Kafka  ·  Redisson            │
└─────────────────────────────────────────────────────────────┘
```

### CQRS

| Side | Responsibility | Storage |
|---|---|---|
| **Command** | Place order, Cancel order | MongoDB (source of truth) |
| **Query** | Get order book, Trade history, Wallet | Redis (cache) + MongoDB |

### Concurrency Strategy

- The `OrderBook` matching engine uses `ReentrantReadWriteLock` — multiple concurrent reads, exclusive writes
- Wallet operations use **Redisson distributed locks** (`wallet-lock:{userId}`) to prevent race conditions across multiple instances
- Kafka producer is configured with `enable.idempotence=true` to prevent duplicate event delivery

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Database | MongoDB 7 |
| Cache | Redis 7 |
| Messaging | Apache Kafka (Confluent 7.5) |
| Distributed Lock | Redisson |
| API Docs | SpringDoc OpenAPI 3.0 |
| Resilience | Resilience4j (Circuit Breaker, Rate Limiter) |
| Build | Maven |

---

## Project Structure

```
src/main/java/com/br/strutz/order_book/
│
├── domain/                          # Pure domain — no framework dependencies
│   ├── model/                       # Aggregates and Value Objects
│   │   ├── Order.java
│   │   ├── Wallet.java
│   │   ├── Trade.java
│   │   └── vo/                      # OrderId, UserId, Money, WalletId...
│   ├── port/
│   │   ├── in/                      # Use case interfaces
│   │   └── out/                     # Repository and publisher interfaces
│   ├── service/
│   │   ├── OrderBook.java           # Matching engine
│   │   └── WalletService.java       # Balance management
│   ├── event/                       # Domain events
│   └── exception/                   # Domain exceptions
│
├── application/                     # Orchestrates use cases
│   ├── command/                     # PlaceOrder, CancelOrder
│   └── query/                       # GetOrderBook, GetTradeHistory, GetWallet
│
└── adapter/
    ├── in/
    │   └── rest/                    # REST Controllers + DTOs + Mappers
    └── out/
        ├── mongo/                   # MongoDB repositories and adapters
        ├── redis/                   # Redis order book cache
        └── kafka/                   # Kafka event publisher
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker and Docker Compose

### 1. Clone the repository

```bash
git clone https:
cd order-book-vibranium
```

### 2. Start the infrastructure

```bash
docker-compose up -d
```

This starts the following services:

| Service | Port | UI |
|---|---|---|
| MongoDB | 27017 | — |
| Redis | 6379 | — |
| Zookeeper | 2181 | — |
| Kafka | 9092 | — |
| Kafka UI | 8080 | http:

### 3. Seed the database

Before placing orders, create the wallets for your test users directly in MongoDB:

```javascript
docker exec -it mongodb mongosh -u admin -p admin --authenticationDatabase admin

use appdb

db.wallets.insertOne({
    _id: "wallet-seller-001",
    user_id: "user-seller-001",
    available_balance: NumberDecimal("500.00000000"),
    reserved_balance: NumberDecimal("0.00000000"),
    transactions: [],
    created_at: new Date(),
    updated_at: new Date(),
    lock_touched_at: new Date()
})

db.wallets.insertOne({
    _id: "wallet-buyer-001",
    user_id: "user-buyer-001",
    available_balance: NumberDecimal("10000.00000000"),
    reserved_balance: NumberDecimal("0.00000000"),
    transactions: [],
    created_at: new Date(),
    updated_at: new Date(),
    lock_touched_at: new Date()
})
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The application starts on port **8081** (port 8080 is reserved for Kafka UI).

### 5. Access the API docs

Open [http:

---

## Configuration

The main configuration file is `src/main/resources/application.yml`.

Key properties:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb:
    redis:
      host: localhost
      port: 6379
  kafka:
    bootstrap-servers: localhost:9092

vibranium:
  cache:
    order-book-ttl: 5s      # Redis TTL for the order book snapshot

resilience4j:
  ratelimiter:
    instances:
      orderService:
        limit-for-period: 5000      # 5000 req/s per instance
        limit-refresh-period: 1s
```

---

## API Reference

Full interactive documentation is available at `/swagger-ui.html`. Below is a quick reference.

### Orders

#### Place an order

```http
POST /api/v1/orders
X-Correlation-Id: {unique-request-id}
Content-Type: application/json

{
  "userId": "user-buyer-001",
  "type": "BUY",
  "price": 150.00,
  "quantity": 10.0
}
```

> The `X-Correlation-Id` header is **required** and must be unique per request. It guarantees idempotency — sending the same header twice safely rejects the duplicate.

**Response `202 Accepted`**

```json
{
  "orderId": "uuid",
  "userId": "user-buyer-001",
  "status": "ACCEPTED",
  "message": "Order successfully received"
}
```

#### Cancel an order

```http
DELETE /api/v1/orders/{orderId}
X-Correlation-Id: {unique-request-id}
Content-Type: application/json

{
  "userId": "user-buyer-001"
}
```

#### Get order book

```http
GET /api/v1/orders/book?depth=20
```

**Response `200 OK`**

```json
{
  "bids": [
    { "price": 150.00, "quantity": 10.0, "orderCount": 1 }
  ],
  "asks": [],
  "spread": 0,
  "generatedAt": "2024-01-01T00:00:00Z"
}
```

### Wallet

#### Get wallet

```http
GET /api/v1/wallets/{userId}
```

**Response `200 OK`**

```json
{
  "userId": "user-buyer-001",
  "availableBalance": 8500.00,
  "reservedBalance": 1500.00,
  "totalBalance": 10000.00,
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### Trades

#### Get trade history

```http
GET /api/v1/trades/{userId}?page=0&size=20
```

---

## Design Decisions

### Why CQRS + Circuit Breaker instead of just one?

They solve different problems. CQRS separates the write model (order matching, wallet settlement) from the read model (order book snapshot, trade history), allowing each side to scale independently. Circuit Breaker (Resilience4j) adds resilience to external calls — if Redis is unavailable, the system degrades gracefully by rebuilding the snapshot from the in-memory order book instead of failing.

### Why is the Order Book kept in memory?

A `TreeMap` with `ReentrantReadWriteLock` provides sub-microsecond matching latency that no database can match. MongoDB stores the source of truth and is used to rebuild the in-memory book on startup via `OrderBookInitializer`. Redis caches the read snapshot with a 5-second TTL to serve the thousands of concurrent read requests without touching the matching engine.

### Why Redisson for wallet locks instead of MongoDB transactions?

MongoDB multi-document transactions add significant latency and complexity. Redisson distributed locks are faster, simpler, and work correctly across multiple application instances — which is exactly what is needed when two trades for the same user arrive simultaneously on different pods.

### Wallet balance model

Each wallet maintains two separate balances:

- **Available balance** — free to use for new orders
- **Reserved balance** — locked for pending orders (buy: BRL reserved; sell: Vibranium reserved)

When a trade is matched, the reserved amount is debited and the received asset is credited. When an order is cancelled, the reserved amount is released back to available. This prevents double-spending without requiring database-level locks on every read.

### Traceability

Every domain event carries a `correlationId` that flows from the HTTP request header through Kafka topics and into the `audit_log` collection. A full transaction lifecycle — from order placement to wallet settlement — can be reconstructed by querying `db.audit_log.find({ correlationId: "..." })`.
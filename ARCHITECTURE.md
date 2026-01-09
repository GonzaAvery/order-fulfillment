# System Architecture

This document describes in detail the architecture of the Order Fulfillment system.

## Overview

The system implements a **modular monolith** architecture with **event-driven** communication using Apache Kafka. Although all services run in the same application, they are clearly separated by domain and communicate asynchronously through events.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Client / API Consumer                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ HTTP/REST
                         │
┌────────────────────────▼────────────────────────────────────┐
│              Spring Boot Application                         │
│                                                              │
│  ┌──────────────┐         ┌──────────────┐                │
│  │   Order      │         │  Delivery     │                │
│  │  Controller  │         │  Controller   │                │
│  └──────┬───────┘         └──────┬────────┘                │
│         │                        │                          │
│  ┌──────▼───────┐         ┌──────▼────────┐                │
│  │   Order      │         │   Delivery    │                │
│  │   Service    │         │   Service     │                │
│  └──────┬───────┘         └──────┬────────┘                │
│         │                        │                          │
│  ┌──────▼────────────────────────▼────────┐                │
│  │      Fulfillment Service                │                │
│  │      (Orchestrator)                     │                │
│  └──────┬──────────────────────────────────┘                │
│         │                                                   │
│  ┌──────▼──────────────────────────────────┐                │
│  │      Event Publisher                    │                │
│  └──────┬──────────────────────────────────┘                │
│         │                                                   │
└─────────┼───────────────────────────────────────────────────┘
          │
          │ Kafka Producer
          │
┌─────────▼───────────────────────────────────────────────────┐
│                    Apache Kafka                              │
│  ┌────────────────────────────────────────────┐            │
│  │  Topic: order-events (3 partitions)       │            │
│  │  Topic: order-events-dlq (1 partition)    │            │
│  └────────────────────────────────────────────┘            │
└─────────┬───────────────────────────────────────────────────┘
          │
          │ Kafka Consumer
          │
┌─────────▼───────────────────────────────────────────────────┐
│              Spring Boot Application                         │
│                                                              │
│  ┌──────────────────────────────────────────────┐          │
│  │         Event Consumer                        │          │
│  │  - Deserialization                            │          │
│  │  - Idempotency (ProcessedEvent)              │          │
│  │  - Routing                                     │          │
│  └──────┬───────────────────────────────────────┘          │
│         │                                                   │
│  ┌──────▼──────────┐    ┌──────────────┐                 │
│  │  Fulfillment     │    │ Notification │                 │
│  │  Event Router    │    │ Event Router │                 │
│  └──────┬───────────┘    └──────┬───────┘                 │
│         │                       │                          │
│  ┌──────▼──────────┐    ┌──────▼──────────┐               │
│  │  Fulfillment    │    │  Notification   │               │
│  │  Service        │    │  Service       │               │
│  └──────┬──────────┘    └────────────────┘               │
│         │                                                  │
└─────────┼───────────────────────────────────────────────────┘
          │
          │ JPA
          │
┌─────────▼───────────────────────────────────────────────────┐
│                    PostgreSQL                                │
│  ┌────────────────────────────────────────────┐            │
│  │  Tables:                                   │            │
│  │  - orders                                  │            │
│  │  - deliveries                              │            │
│  │  - couriers                                │            │
│  │  - processed_events                        │            │
│  └────────────────────────────────────────────┘            │
└──────────────────────────────────────────────────────────────┘
```

## Main Components

### 1. Order Service

**Responsibility:** Order lifecycle management.

**Components:**
- `OrderController`: REST endpoints
- `OrderService`: Business logic
- `Order`: Domain entity with state validation
- `OrderRepository`: Persistence

**States:**
- PLACED → ACCEPTED → PICKED_UP → IN_TRANSIT → DELIVERED
- Failure states: CANCELED, FAILED

**Events:**
- Publishes: `OrderPlaced`
- Consumes: (indirectly through FulfillmentService)

---

### 2. Delivery Service

**Responsibility:** Delivery management and courier assignment.

**Components:**
- `DeliveryController`: REST endpoints
- `DeliveryService`: Business logic
- `Delivery`: Domain entity
- `Courier`: Domain entity
- `DeliveryRepository`, `CourierRepository`: Persistence

**States:**
- PENDING → COURIER_ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED

**Events:**
- Publishes: `CourierAssigned`, `DeliveryDelayed`
- Consumes: (indirectly through FulfillmentService)

---

### 3. Fulfillment Service

**Responsibility:** Complete order flow orchestration.

**Components:**
- `FulfillmentService`: Main orchestrator
- `FulfillmentEventRouter`: Event routing

**Flow:**
1. Consumes `OrderPlaced` → Accepts order → Creates delivery → Assigns courier
2. Consumes `CourierAssigned` → Updates states to PICKED_UP and IN_TRANSIT
3. Completes order when delivery is completed

**Events:**
- Consumes: `OrderPlaced`, `CourierAssigned`
- Publishes: `OrderAccepted`, `OrderCompleted`, `OrderFailed`

---

### 4. Notification Service

**Responsibility:** User notification generation.

**Components:**
- `NotificationService`: Event processing
- `NotificationEventRouter`: Event routing

**Events:**
- Consumes: All system events
- Action: Generates notifications (simulated in logs, ready for email/SMS/push)

---

### 5. Event System

**Components:**
- `EventPublisher`: Interface for publishing events
- `KafkaEventPublisher`: Kafka implementation
- `EventConsumer`: Kafka consumer
- `IdempotencyService`: Idempotency handling
- `ProcessedEvent`: Entity for tracking processed events

**Features:**
- Persistent idempotency in database
- CorrelationId for traceability
- Automatic routing to corresponding services

## Data Flow

### Order Creation Flow

```
1. Client → POST /api/orders
2. OrderController → OrderService.createOrder()
3. OrderService:
   - Creates Order (PLACED)
   - Saves to PostgreSQL
   - Publishes OrderPlaced to Kafka
4. Kafka → EventConsumer
5. EventConsumer:
   - Verifies idempotency (ProcessedEvent)
   - Routes to FulfillmentEventRouter
6. FulfillmentService.handleOrderPlaced():
   - Updates Order to ACCEPTED
   - Publishes OrderAccepted
   - Creates Delivery (PENDING)
   - Assigns courier (if available)
   - Publishes CourierAssigned (if assigned)
7. FulfillmentService.handleCourierAssigned():
   - Updates Order to PICKED_UP → IN_TRANSIT
   - Updates Delivery
```

### Delivery Completion Flow

```
1. Client → POST /api/deliveries/{id}/complete
2. DeliveryController → FulfillmentService.completeOrder()
3. FulfillmentService:
   - Updates Order to DELIVERED
   - Updates Delivery to DELIVERED
   - Publishes OrderCompleted to Kafka
4. NotificationService consumes OrderCompleted
   - Generates completion notification
```

## Design Patterns

### 1. Event-Driven Architecture
- Decoupling through events
- Horizontal scalability
- Resilience against failures

### 2. Domain-Driven Design (DDD)
- Separation by bounded contexts
- Rich domain entities
- Validation in the domain

### 3. Repository Pattern
- Persistence abstraction
- Facilitates testing
- Separation of concerns

### 4. Idempotency Pattern
- Prevents duplicate processing
- Unique constraint in database
- "Insert or ignore" strategy

### 5. State Machine Pattern
- Explicit transition validation
- Clear terminal states
- Prevents invalid states

## Infrastructure Decisions

### Database: PostgreSQL
- **Reason:** ACID, relationships, unique constraint for idempotency
- **Alternatives considered:** MongoDB (less suitable for relationships), Redis (cache only)

### Messaging: Apache Kafka
- **Reason:** Scalability, persistence, guaranteed order
- **Alternatives considered:** RabbitMQ (less scalable), Redis Pub/Sub (no persistence)

### Architecture: Modular Monolith
- **Reason:** Simplicity for MVP, easy future migration
- **Alternative:** Microservices (higher operational complexity)

## Scalability

### Horizontal Scalability
- **Kafka:** Partitions allow parallelism
- **Application:** Multiple instances can consume from the same topic
- **Database:** Read replicas for queries

### Vertical Scalability
- **Application:** More CPU/RAM to process more events
- **Database:** More resources for more transactions

## Security (Future)

### Considerations:
- JWT authentication
- Role-based authorization
- Sensitive data encryption
- Rate limiting
- Input validation

## Monitoring and Observability

### Metrics
- `orders_created_total`
- `events_processed_total`
- `events_failed_total`
- Spring Actuator metrics

### Logs
- Structured with CorrelationId
- Appropriate levels (INFO, WARN, ERROR)
- JSON format (ready)

### Traceability
- CorrelationId in all events
- MDC in logs
- Ready for distributed tracing

## Testing

### Strategy
- **Unit Tests:** Business logic, validations
- **Integration Tests:** Complete flow with Testcontainers
- **E2E Tests:** (Future) Tests against complete environment

### Coverage
- State transition validation
- Complete order flow
- Idempotency
- Error handling

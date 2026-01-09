# Event Contracts

This directory contains JSON Schemas that define the structure of system events.

## Event Structure

All events follow the same base structure:

```json
{
  "eventId": "uuid",
  "correlationId": "uuid",
  "occurredAt": "ISO 8601 timestamp",
  "eventType": "string",
  "payload": { ... }
}
```

### Required Fields

- **eventId**: Unique event identifier to guarantee idempotency
- **correlationId**: Correlation identifier for end-to-end traceability (generally the orderId)
- **occurredAt**: ISO 8601 timestamp of when the event occurred
- **eventType**: Event type (OrderPlaced, OrderAccepted, etc.)
- **payload**: Event-specific content according to its type

## Available Events

### OrderPlaced.v1.json
Emitted when a user creates an order.

**Payload:**
- `orderId`: Order UUID
- `customerId`: Customer UUID
- `deliveryAddress`: Delivery address
- `totalAmount`: Total order amount

### OrderAccepted.v1.json
Emitted when the system accepts an order.

**Payload:**
- `orderId`: Accepted order UUID

### CourierAssigned.v1.json
Emitted when a courier is assigned to a delivery.

**Payload:**
- `deliveryId`: Delivery UUID
- `orderId`: Associated order UUID
- `courierId`: Assigned courier UUID
- `courierName`: Courier name

### OrderCompleted.v1.json
Emitted when an order is completed successfully.

**Payload:**
- `orderId`: Completed order UUID
- `deliveryId`: Completed delivery UUID

### OrderFailed.v1.json
Emitted when an order fails.

**Payload:**
- `orderId`: Failed order UUID
- `failureReason`: Failure reason

## Versioning

Events are explicitly versioned in the filename (`.v1.json`). When incompatible changes are needed, a new version will be created (`.v2.json`).

## Validation

These schemas can be used for:
- Validating events before publishing
- Generating automatic documentation
- Validating events in tests
- Generating types/classes in different languages

## Usage in Code

Events in Java code must comply with these contracts. Schemas serve as documentation and contract between services.

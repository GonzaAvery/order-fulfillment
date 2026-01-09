# Quick Start Guide

Quick guide to start the system in 3 commands.

## Prerequisites

- Docker and Docker Compose
- Java 21
- Maven 3.6+

## Demo in 3 Commands

```bash
# 1. Start infrastructure (PostgreSQL + Kafka)
docker-compose up -d

# 2. Start the application
mvn spring-boot:run

# 3. Create an order and see the flow
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"550e8400-e29b-41d4-a716-446655440000","deliveryAddress":"Av. Corrientes 1234, CABA","totalAmount":1500.50}'
```

## Verify It Works

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Query the Order
```bash
# Replace {orderId} with the ID from the previous response
curl http://localhost:8080/api/orders/{orderId}
```

### View Events in Kafka UI
Open `http://localhost:8081` → Topics → `order-events` → Messages

### View API Documentation
Open `http://localhost:8080/swagger-ui.html`

## Stop the System

```bash
# Stop the application (Ctrl+C in the terminal where it's running)

# Stop infrastructure
docker-compose down

# Stop and remove volumes (clean data)
docker-compose down -v
```

## Troubleshooting

### Kafka is not available

If you see Kafka connection errors:
1. Verify Kafka is running: `docker-compose ps`
2. Wait a few seconds for Kafka to finish initializing
3. Check logs: `docker-compose logs kafka`

### PostgreSQL is not available

If you see PostgreSQL connection errors:
1. Verify PostgreSQL is running: `docker-compose ps`
2. Verify the database exists: `docker-compose exec postgres psql -U postgres -l`
3. If it doesn't exist, create it: `docker-compose exec postgres psql -U postgres -c "CREATE DATABASE order_fulfillment;"`

### Application doesn't start

1. Verify Java 21 is installed: `java -version`
2. Verify Maven is installed: `mvn -version`
3. Check application logs for specific errors

## Next Steps

Once the system is running, you can:
- Explore the API at `http://localhost:8080/actuator`
- View metrics at `http://localhost:8080/actuator/metrics`
- Explore events in Kafka UI at `http://localhost:8081`
- Review structured logs with `correlationId` to follow an order flow

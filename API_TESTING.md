# Testing Guide with Swagger and Postman

This guide shows you how to test the API using Swagger UI and Postman.

## Swagger UI

Swagger UI provides an interactive web interface to test the API directly from the browser.

### Access Swagger UI

1. Make sure the application is running:
   ```bash
   mvn spring-boot:run
   ```

2. Open your browser and navigate to:
   ```
   http://localhost:8080/swagger-ui.html
   ```

3. You can also access the OpenAPI documentation in JSON format:
   ```
   http://localhost:8080/api-docs
   ```

### Using Swagger UI

1. **Explore Endpoints**: You'll see all endpoints organized by tags:
   - **Orders**: Endpoints for order management
   - **Deliveries**: Endpoints for delivery management

2. **Test an Endpoint**:
   - Click on an endpoint to expand it
   - Click "Try it out"
   - Fill in the required parameters
   - Click "Execute"
   - You'll see the response with status code, headers, and body

3. **Example: Create an Order**:
   - Expand `POST /api/orders`
   - Click "Try it out"
   - Modify the JSON body with your data:
     ```json
     {
       "customerId": "550e8400-e29b-41d4-a716-446655440000",
       "deliveryAddress": "Av. Corrientes 1234, CABA",
       "totalAmount": 1500.50
     }
     ```
   - Click "Execute"
   - Copy the `id` from the response to use in other endpoints

## Postman

Postman allows you to create organized collections and automate tests.

### Import the Collection

1. Open Postman
2. Click "Import" (top left)
3. Select the file `postman/Order-Fulfillment.postman_collection.json`
4. The collection will be imported with all preconfigured endpoints

### Configure Environment Variables

1. In Postman, click the "Environments" icon (eye) in the top right corner
2. Create a new environment called "Local"
3. Add the following variables:
   - `baseUrl`: `http://localhost:8080`
   - `orderId`: (will be filled automatically after creating an order)
   - `deliveryId`: (will be filled automatically after creating an order)

4. Select the "Local" environment from the dropdown

### Recommended Test Flow

#### 1. Verify Health Check

```
GET /actuator/health
```

You should receive a `200 OK` with the application status.

#### 2. Create an Order

```
POST /api/orders
Body:
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "deliveryAddress": "Av. Corrientes 1234, CABA",
  "totalAmount": 1500.50
}
```

**Expected response:**
- Status: `201 Created`
- Body contains the order with status `PLACED`
- The `id` is automatically saved in the `orderId` variable (if using the Postman collection)

#### 3. Query the Order

```
GET /api/orders/{orderId}
```

**Expected response:**
- Status: `200 OK`
- The status should have changed to `ACCEPTED` (processed by FulfillmentService)
- Should have a `deliveryId` assigned

#### 4. Query the Delivery

```
GET /api/deliveries/order/{orderId}
```

**Expected response:**
- Status: `200 OK`
- Delivery information with status and assigned courier (if available)

#### 5. Wait for Asynchronous Processing

After creating an order, wait a few seconds and query the order again. You should see:
- Status changed from `PLACED` → `ACCEPTED` → `PICKED_UP` → `IN_TRANSIT`
- A courier assigned to the delivery

#### 6. Complete the Delivery

```
POST /api/deliveries/{deliveryId}/complete
```

**Expected response:**
- Status: `200 OK`
- Order and delivery are in `DELIVERED` status

#### 7. Verify Metrics

```
GET /actuator/metrics/orders_created_total
GET /actuator/metrics/events_processed_total
GET /actuator/metrics/events_failed_total
```

### Request Examples

#### Create Order with Different Data

```json
{
  "customerId": "123e4567-e89b-12d3-a456-426614174000",
  "deliveryAddress": "Av. Santa Fe 1234, Palermo, CABA",
  "totalAmount": 2500.75
}
```

#### Query Order (replace {orderId})

```
GET http://localhost:8080/api/orders/550e8400-e29b-41d4-a716-446655440000
```

### Verify Events in Kafka UI

After creating an order, you can verify that events were published:

1. Open `http://localhost:8081` in your browser
2. Navigate to **Topics** → `order-events`
3. Click **Messages**
4. You should see events like:
   - `OrderPlaced`
   - `OrderAccepted`
   - `CourierAssigned` (if courier available)
   - `OrderCompleted` (after completing delivery)

### Troubleshooting

#### Error: "Connection refused"

- Verify the application is running: `mvn spring-boot:run`
- Verify it's on the correct port: `http://localhost:8080`

#### Error: "Order not found"

- Make sure you're using a valid `orderId`
- Verify you created the order first

#### Order status doesn't change

- Changes are asynchronous, wait a few seconds
- Verify Kafka is running: `docker-compose ps`
- Check application logs for errors

#### Swagger UI doesn't load

- Verify the application is running
- Try accessing directly: `http://localhost:8080/swagger-ui/index.html`
- Verify SpringDoc dependency is in `pom.xml`

### Available Endpoints

#### Orders
- `POST /api/orders` - Create a new order
- `GET /api/orders/{orderId}` - Query an order by ID

#### Deliveries
- `GET /api/deliveries/order/{orderId}` - Query delivery by order ID
- `POST /api/deliveries/{deliveryId}/complete` - Complete a delivery

#### Actuator
- `GET /actuator/health` - Health check
- `GET /actuator/metrics` - Metrics list
- `GET /actuator/metrics/{metricName}` - Specific metric
- `GET /actuator/prometheus` - Prometheus format metrics

#### Swagger
- `GET /swagger-ui.html` - Swagger UI interface
- `GET /api-docs` - OpenAPI JSON documentation

### Tips

1. **Use Variables**: In Postman, save IDs in variables to reuse them
2. **Review Logs**: Application logs show the complete flow with `correlationId`
3. **Monitor Metrics**: Use `/actuator/metrics` to see system behavior
4. **Kafka UI**: Use Kafka UI to see events in real time
5. **CorrelationId**: All events have a `correlationId` you can use to trace the complete flow
